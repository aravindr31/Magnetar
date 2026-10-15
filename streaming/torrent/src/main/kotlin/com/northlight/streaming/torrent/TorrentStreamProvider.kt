package com.northlight.streaming.torrent

import android.content.Context
import com.northlight.common.result.Result
import com.northlight.streaming.StreamResult
import com.northlight.streaming.StreamingProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.libtorrent4j.AlertListener
import org.libtorrent4j.AndroidPlatform
import org.libtorrent4j.MagnetUri
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SettingsPack
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.PieceFinishedAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import java.io.File
import java.net.ServerSocket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Streaming provider that downloads and streams torrents directly on device.
 * Uses libtorrent4j for sequential (streaming-first) downloading.
 *
 * Flow:
 * 1. Add magnet to session
 * 2. Enable sequential download (prioritise first + last pieces)
 * 3. Wait until enough pieces are buffered (configurable threshold)
 * 4. Start local HTTP server pointing to the partial file
 * 5. Return localhost stream URL to the player
 */
@Singleton
class TorrentStreamProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : StreamingProvider {

    override val name = "Local Torrent"

    private var session: SessionManager? = null
    private var localServer: TorrentHttpServer? = null
    private var currentHandle: TorrentHandle? = null

    // Minimum percentage of the file downloaded before playback starts
    private val BUFFER_THRESHOLD_PERCENT = 2

    override fun isConfigured(): Boolean = true // always available, no credentials needed

    override suspend fun resolveStream(magnetLink: String): Result<StreamResult> = withContext(Dispatchers.IO) {
        try {
            initSessionIfNeeded()

            val downloadDir = File(context.filesDir, "torrents").also { it.mkdirs() }
            val session = requireNotNull(session)

            // Fetch torrent metadata from magnet
            val torrentInfo = fetchTorrentInfo(session, magnetLink)
                ?: return@withContext Result.Error(Exception("Failed to fetch torrent metadata. Check your connection."))

            // Find the largest video file in the torrent
            val videoFileIndex = findVideoFileIndex(torrentInfo)
                ?: return@withContext Result.Error(Exception("No playable video file found in torrent."))

            // Add torrent with sequential download enabled
            val handle = session.find(torrentInfo.infoHash()) ?: run {
                val params = org.libtorrent4j.AddTorrentParams()
                params.torrentInfo = torrentInfo
                params.savePath = downloadDir.absolutePath
                session.download(torrentInfo, downloadDir)
                session.find(torrentInfo.infoHash())
            } ?: return@withContext Result.Error(Exception("Failed to start torrent download."))

            currentHandle = handle

            // Enable sequential download — critical for streaming
            handle.setFlags(handle.flags().and_(org.libtorrent4j.TorrentFlags.SEQUENTIAL_DOWNLOAD))

            // Prioritise video file, deprioritise everything else
            val priorities = Array(torrentInfo.numFiles()) {
                if (it == videoFileIndex) org.libtorrent4j.Priority.NORMAL else org.libtorrent4j.Priority.IGNORE
            }
            handle.prioritizeFiles(priorities)

            // Wait until buffer threshold is reached
            waitForBuffer(handle, torrentInfo, videoFileIndex)

            // Get actual file path and start local HTTP server
            val videoFile = File(downloadDir, torrentInfo.files().filePath(videoFileIndex))
            val port = startLocalServer(videoFile)

            val mimeType = if (videoFile.name.endsWith(".mkv")) "video/x-matroska" else "video/mp4"

            Result.Success(
                StreamResult(
                    url = "http://127.0.0.1:$port/${videoFile.name}",
                    title = torrentInfo.name()
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun fetchTorrentInfo(session: SessionManager, magnetLink: String): TorrentInfo? =
        withContext(Dispatchers.IO) {
            // libtorrent4j's blocking fetch with 30s timeout
            runCatching {
                val data = session.fetchMagnet(magnetLink, 30)
                data?.let { TorrentInfo.bdecode(it) }
            }.getOrNull()
        }

    private fun findVideoFileIndex(info: TorrentInfo): Int? {
        val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "m4v")
        var maxSize = 0L
        var bestIndex: Int? = null
        for (i in 0 until info.numFiles()) {
            val path = info.files().filePath(i)
            val ext = path.substringAfterLast('.').lowercase()
            val size = info.files().fileSize(i)
            if (ext in videoExtensions && size > maxSize) {
                maxSize = size
                bestIndex = i
            }
        }
        return bestIndex
    }

    private suspend fun waitForBuffer(handle: TorrentHandle, info: TorrentInfo, fileIndex: Int) {
        val fileSize = info.files().fileSize(fileIndex)
        val neededBytes = (fileSize * BUFFER_THRESHOLD_PERCENT / 100).coerceAtLeast(5 * 1024 * 1024) // min 5MB

        while (true) {
            val status = handle.status()
            val downloadedBytes = (status.progress() * fileSize).toLong()
            if (downloadedBytes >= neededBytes) break
            delay(500)
        }
    }

    private fun startLocalServer(file: File): Int {
        localServer?.stop()
        val port = findFreePort()
        localServer = TorrentHttpServer(file, port).also { it.start() }
        return port
    }

    private fun findFreePort(): Int {
        ServerSocket(0).use { return it.localPort }
    }

    private fun initSessionIfNeeded() {
        if (session != null) return
        AndroidPlatform.setup()
        session = SessionManager().also {
            val settings = SettingsPack()
            settings.setString(
                org.libtorrent4j.swig.settings_pack.string_types.user_agent.swigValue(),
                "NorthLight/1.0"
            )
            it.start(settings)
        }
    }

    override suspend fun cleanup(streamResult: StreamResult) {
        localServer?.stop()
        currentHandle?.let { handle ->
            if (handle.isValid) {
                session?.remove(handle)
            }
        }
        currentHandle = null
        // Clean up downloaded files
        File(context.filesDir, "torrents").deleteRecursively()
    }
}
