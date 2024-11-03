package io.github.raghavsatyadev.support.networking

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import io.github.raghavsatyadev.support.core.CoreApp

@Suppress("MemberVisibilityCanBePrivate")
object FileChooserUtil {

    fun getMimeType(fileLink: String?): String? {
        val type: String? = CoreApp.instance.contentResolver.getType(Uri.parse(fileLink))
        if (!TextUtils.isEmpty(type)) return type
        val mime = MimeTypeMap.getSingleton()
        return mime.getMimeTypeFromExtension(getFileExtension(fileLink))
    }

    fun getFileExtension(fileLink: String?): String? {
        val extension: String?
        val uri = Uri.parse(fileLink)
        val scheme = uri.scheme
        extension = if (scheme != null && scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(CoreApp.instance.contentResolver.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(fileLink)
        }
        return extension
    }
}