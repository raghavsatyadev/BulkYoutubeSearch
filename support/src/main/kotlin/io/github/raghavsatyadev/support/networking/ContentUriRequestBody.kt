package io.github.raghavsatyadev.support.networking

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.FileNotFoundException
import java.io.IOException

class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val contentUri: Uri,
) : RequestBody() {
    override fun contentType(): MediaType? {
        val contentType = contentResolver.getType(contentUri)
        return contentType?.toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return contentUri.length(contentResolver)
    }

    override fun writeTo(sink: BufferedSink) {
        val inputStream = contentResolver.openInputStream(contentUri)
            ?: throw IOException("Couldn't open content URI for reading")
        inputStream.source().use { source ->
            sink.writeAll(source)
        }
    }
}

fun Uri.length(contentResolver: ContentResolver)
        : Long {

    val assetFileDescriptor = try {
        contentResolver.openAssetFileDescriptor(this, "r")
    } catch (e: FileNotFoundException) {
        null
    }
    // uses ParcelFileDescriptor#getStatSize underneath if failed
    val length = assetFileDescriptor?.use { it.length } ?: -1L
    if (length != -1L) {
        return length
    }

    // if "content://" uri scheme, try contentResolver table
    if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
        return contentResolver.query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex == -1) {
                    return@use -1L
                }
                cursor.moveToFirst()
                return try {
                    cursor.getLong(sizeIndex)
                } catch (_: Throwable) {
                    -1L
                }
            } ?: -1L
    } else {
        return -1L
    }
}