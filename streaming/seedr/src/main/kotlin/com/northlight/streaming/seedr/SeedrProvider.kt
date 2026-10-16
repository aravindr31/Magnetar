package com.northlight.streaming.seedr

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
import javax.inject.Named
import javax.inject.Singleton

/**
 * Seedr streaming provider using Seedr's unofficial OAuth API.
 * Credentials are stored in EncryptedSharedPreferences.
 *
 * Seedr flow:
 * 1. Authenticate with email/password → get access token
 * 2. POST magnet link → Seedr fetches the torrent
 * 3. Poll folder contents until file appears
 * 4. Return the direct streaming URL
 *
 * NOTE: Seedr free tier has 2GB limit. We auto-delete after cleanup().
 */
@Singleton
class SeedrProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("streaming") private val okHttpClient: OkHttpClient
) : StreamingProvider {

    override val name = "Seedr"

    companion object {
        private const val BASE_URL = "https://www.seedr.cc"
        private const val TOKEN_URL = "$BASE_URL/oauth_test/token.php"
        private const val API_URL = "$BASE_URL/api"
        private const val PREFS_FILE = "seedr_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_DEVICE_CODE = "device_code"
        private const val POLL_INTERVAL_MS = 3000L
        private const val MAX_POLL_ATTEMPTS = 60 // 3 minutes max wait
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, PREFS_FILE, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun isConfigured(): Boolean =
        prefs.getString(KEY_ACCESS_TOKEN, null) != null

    /**
     * Authenticates with Seedr using username/password.
     * Stores the access token securely.
     */
    suspend fun authenticate(username: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val body = FormBody.Builder()
                    .add("grant_type", "password")
                    .add("client_id", "seedr_chrome")
                    .add("username", username)
                    .add("password", password)
                    .build()

                val request = Request.Builder().url(TOKEN_URL).post(body).build()
                val response = okHttpClient.newCall(request).execute()
                val json = JSONObject(response.body!!.string())

                if (json.has("access_token")) {
                    prefs.edit()
                        .putString(KEY_ACCESS_TOKEN, json.getString("access_token"))
                        .apply()
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception(json.optString("error_description", "Authentication failed")))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun resolveStream(magnetLink: String): Result<StreamResult> =
        withContext(Dispatchers.IO) {
            try {
                val token = prefs.getString(KEY_ACCESS_TOKEN, null)
                    ?: return@withContext Result.Error(Exception("Not authenticated with Seedr. Please login in Settings."))

                // Step 1: Add magnet link
                val torrentId = addMagnet(token, magnetLink)
                    ?: return@withContext Result.Error(Exception("Failed to add magnet to Seedr"))

                // Step 2: Poll until file is ready
                val fileInfo = pollForFile(token)
                    ?: return@withContext Result.Error(Exception("Seedr timed out preparing the file"))

                Result.Success(
                    StreamResult(
                        url = fileInfo.first,
                        title = fileInfo.second,
                        headers = mapOf("Authorization" to "Bearer $token")
                    )
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    private fun addMagnet(token: String, magnetLink: String): String? {
        val body = FormBody.Builder()
            .add("access_token", token)
            .add("magnet", magnetLink)
            .build()
        val request = Request.Builder()
            .url("$API_URL/transfer/add_magnet")
            .post(body)
            .build()
        val response = okHttpClient.newCall(request).execute()
        val json = JSONObject(response.body!!.string())
        return if (json.optString("result") == "true") json.optString("user_torrent_id") else null
    }

    private suspend fun pollForFile(token: String): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            repeat(MAX_POLL_ATTEMPTS) {
                delay(POLL_INTERVAL_MS)
                val result = getStreamableFile(token)
                if (result != null) return@withContext result
            }
            null
        }

    private fun getStreamableFile(token: String): Pair<String, String>? {
        val request = Request.Builder()
            .url("$API_URL/folder?access_token=$token")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()
        val json = JSONObject(response.body!!.string())
        val folders = json.optJSONArray("folders") ?: return null

        for (i in 0 until folders.length()) {
            val folder = folders.getJSONObject(i)
            val files = folder.optJSONArray("files") ?: continue
            for (j in 0 until files.length()) {
                val file = files.getJSONObject(j)
                val name = file.optString("name", "")
                if (name.endsWith(".mp4") || name.endsWith(".mkv")) {
                    val streamUrl = file.optString("stream_link")
                    if (streamUrl.isNotEmpty()) return Pair(streamUrl, name)
                }
            }
        }
        return null
    }

    override suspend fun cleanup(streamResult: StreamResult) {
        // Optionally clear Seedr storage to avoid hitting the 2GB limit
        val token = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("$API_URL/folder?access_token=$token")
                    .get()
                    .build()
                val response = okHttpClient.newCall(request).execute()
                val json = JSONObject(response.body!!.string())
                val folders = json.optJSONArray("folders") ?: return@runCatching
                for (i in 0 until folders.length()) {
                    val folderId = folders.getJSONObject(i).optInt("id")
                    val deleteBody = FormBody.Builder()
                        .add("access_token", token)
                        .add("folder_id", folderId.toString())
                        .build()
                    okHttpClient.newCall(
                        Request.Builder()
                            .url("$API_URL/folder/delete")
                            .post(deleteBody)
                            .build()
                    ).execute()
                }
            }
        }
    }
}

