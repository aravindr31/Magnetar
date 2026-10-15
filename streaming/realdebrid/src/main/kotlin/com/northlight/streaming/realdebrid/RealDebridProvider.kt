package com.northlight.streaming.realdebrid

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.northlight.common.result.Result
import com.northlight.streaming.StreamResult
import com.northlight.streaming.StreamingProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real-Debrid streaming provider using Real-Debrid REST API v2.
 * Offers instant streaming for cached torrents via high-speed CDN.
 *
 * Flow:
 * 1. Add magnet link via /torrents/addMagnet
 * 2. Select files via /torrents/selectFiles
 * 3. Poll /torrents/info until status is 'downloaded' (often instant if cached)
 * 4. Unrestrict link via /unrestrict/link to get direct streaming CDN URL
 */
@Singleton
class RealDebridProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) : StreamingProvider {

    override val name = "Real-Debrid"

    companion object {
        private const val BASE_URL = "https://api.real-debrid.com/rest/1.0"
        private const val PREFS_FILE = "realdebrid_prefs"
        private const val KEY_API_TOKEN = "api_token"
        private const val POLL_INTERVAL_MS = 2000L
        private const val MAX_POLL_ATTEMPTS = 45 // 90 seconds max wait
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun isConfigured(): Boolean =
        !prefs.getString(KEY_API_TOKEN, null).isNullOrBlank()

    fun setApiToken(token: String) {
        prefs.edit().putString(KEY_API_TOKEN, token.trim()).apply()
    }

    fun getApiToken(): String? = prefs.getString(KEY_API_TOKEN, null)

    override suspend fun resolveStream(magnetLink: String): Result<StreamResult> =
        withContext(Dispatchers.IO) {
            try {
                val token = getApiToken()
                    ?: return@withContext Result.Error(Exception("Real-Debrid API token not configured. Please set it in Settings."))

                // 1. Add magnet
                val addBody = FormBody.Builder()
                    .add("magnet", magnetLink)
                    .build()
                val addReq = Request.Builder()
                    .url("$BASE_URL/torrents/addMagnet")
                    .addHeader("Authorization", "Bearer $token")
                    .post(addBody)
                    .build()

                val addRes = okHttpClient.newCall(addReq).execute()
                val addJsonStr = addRes.body?.string() ?: ""
                if (!addRes.isSuccessful) {
                    return@withContext Result.Error(Exception("Real-Debrid add magnet failed: $addJsonStr"))
                }
                val addJson = JSONObject(addJsonStr)
                val torrentId = addJson.optString("id")
                if (torrentId.isEmpty()) {
                    return@withContext Result.Error(Exception("Invalid torrent ID returned by Real-Debrid"))
                }

                // 2. Select all files
                val selectBody = FormBody.Builder()
                    .add("files", "all")
                    .build()
                val selectReq = Request.Builder()
                    .url("$BASE_URL/torrents/selectFiles/$torrentId")
                    .addHeader("Authorization", "Bearer $token")
                    .post(selectBody)
                    .build()
                okHttpClient.newCall(selectReq).execute()

                // 3. Poll for completion / cache hit
                var downloadLinks: List<String> = emptyList()
                var torrentTitle = "Movie"

                for (attempt in 0 until MAX_POLL_ATTEMPTS) {
                    val infoReq = Request.Builder()
                        .url("$BASE_URL/torrents/info/$torrentId")
                        .addHeader("Authorization", "Bearer $token")
                        .get()
                        .build()
                    val infoRes = okHttpClient.newCall(infoReq).execute()
                    val infoJson = JSONObject(infoRes.body?.string() ?: "{}")
                    val status = infoJson.optString("status")
                    torrentTitle = infoJson.optString("filename", "Movie")

                    if (status == "downloaded") {
                        val linksArray = infoJson.optJSONArray("links")
                        if (linksArray != null && linksArray.length() > 0) {
                            val linksList = mutableListOf<String>()
                            for (i in 0 until linksArray.length()) {
                                linksList.add(linksArray.getString(i))
                            }
                            downloadLinks = linksList
                            break
                        }
                    } else if (status == "error" || status == "dead") {
                        return@withContext Result.Error(Exception("Real-Debrid reported torrent status: $status"))
                    }
                    delay(POLL_INTERVAL_MS)
                }

                if (downloadLinks.isEmpty()) {
                    return@withContext Result.Error(Exception("Torrent download timed out on Real-Debrid. Please try local torrent or another provider."))
                }

                // 4. Unrestrict the first video link
                val chosenLink = downloadLinks.first()
                val unrestrictBody = FormBody.Builder()
                    .add("link", chosenLink)
                    .build()
                val unrestrictReq = Request.Builder()
                    .url("$BASE_URL/unrestrict/link")
                    .addHeader("Authorization", "Bearer $token")
                    .post(unrestrictBody)
                    .build()

                val unrestrictRes = okHttpClient.newCall(unrestrictReq).execute()
                val unrestrictJson = JSONObject(unrestrictRes.body?.string() ?: "{}")
                val streamUrl = unrestrictJson.optString("download")

                if (streamUrl.isNullOrEmpty()) {
                    return@withContext Result.Error(Exception("Failed to get unrestrict stream URL from Real-Debrid"))
                }

                Result.Success(
                    StreamResult(
                        url = streamUrl,
                        title = torrentTitle
                    )
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}

