package com.northlight.streaming.torrent

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

/**
 * Minimal local HTTP server (NanoHTTPD) that serves a single file
 * to allow ExoPlayer/VLC to stream from it via HTTP range requests.
 *
 * Range request support is critical for seeking in video players.
 */
class TorrentHttpServer(
    private val file: File,
    port: Int
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val rangeHeader = session.headers["range"]

        val fileLength = file.length()
        val mimeType = when (file.extension.lowercase()) {
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            else  -> "video/mp4"
        }

        return if (rangeHeader != null) {
            // Handle byte range requests for seeking
            val range = parseRange(rangeHeader, fileLength)
            val start = range.first
            val end = range.second
            val contentLength = end - start + 1

            val stream = FileInputStream(file).also { it.skip(start) }
            val response = newFixedLengthResponse(
                Response.Status.PARTIAL_CONTENT,
                mimeType,
                stream,
                contentLength
            )
            response.addHeader("Content-Range", "bytes $start-$end/$fileLength")
            response.addHeader("Accept-Ranges", "bytes")
            response.addHeader("Content-Length", contentLength.toString())
            response
        } else {
            val stream = FileInputStream(file)
            val response = newFixedLengthResponse(
                Response.Status.OK,
                mimeType,
                stream,
                fileLength
            )
            response.addHeader("Accept-Ranges", "bytes")
            response
        }
    }

    private fun parseRange(rangeHeader: String, fileLength: Long): Pair<Long, Long> {
        // Format: "bytes=start-end" or "bytes=start-"
        val rangeSpec = rangeHeader.removePrefix("bytes=")
        val parts = rangeSpec.split("-")
        val start = parts[0].trim().toLong()
        val end = if (parts.size > 1 && parts[1].trim().isNotEmpty()) {
            parts[1].trim().toLong()
        } else {
            fileLength - 1
        }
        return Pair(start, end)
    }
}
