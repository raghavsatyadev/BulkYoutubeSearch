package io.github.raghavsatyadev.support.extensions

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import io.github.raghavsatyadev.support.core.CoreApp
import java.io.File

object FileExtensions {
    fun File.getMimeType(): String? {
        val type: String? = CoreApp.instance.contentResolver.getType(Uri.parse(this.absolutePath))
        if (!TextUtils.isEmpty(type)) return type
        val mime = MimeTypeMap.getSingleton()
        return mime.getMimeTypeFromExtension(getFileExtension())
    }

    fun File.getFileExtension(): String? {
        val extension: String?
        val uri = Uri.parse(absolutePath)
        val scheme = uri.scheme
        extension = if (scheme != null && scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(CoreApp.instance.contentResolver.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(absolutePath)
        }
        return extension
    }
}