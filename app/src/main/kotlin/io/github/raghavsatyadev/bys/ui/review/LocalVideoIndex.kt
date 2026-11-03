package io.github.raghavsatyadev.bys.ui.review

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document

/**
 * A video file in the picked folder. [path] is its full path on the device, such as
 * "/storage/emulated/0/Movies/BYS/Hollywood/x.mp4".
 */
data class LocalVideo(val uri: Uri, val path: String)

/** Finds the local video of a song: the file name without extension is the song's oldTitle. */
object LocalVideoIndex {
  private val VIDEO_EXTENSIONS = setOf("mp4", "m4v", "mkv", "webm", "avi", "wmv", "mov", "3gp")

  /** The key both sides are compared by: no extension, no case, single spaces. */
  fun key(name: String): String = name.trim().lowercase().replace(WHITESPACE, " ")

  private fun stripExtension(name: String): String {
    val ext = name.substringAfterLast('.', "")
    return if (ext.lowercase() in VIDEO_EXTENSIONS) name.substringBeforeLast('.') else name
  }

  /** Walks the picked folder tree and maps [key] of each video's base name to the video. */
  fun scan(resolver: ContentResolver, treeUri: Uri): Map<String, LocalVideo> {
    val result = HashMap<String, LocalVideo>()
    walk(resolver, treeUri, DocumentsContract.getTreeDocumentId(treeUri), "", result)
    return result
  }

  private fun walk(
    resolver: ContentResolver,
    treeUri: Uri,
    parentId: String,
    prefix: String,
    out: MutableMap<String, LocalVideo>,
  ) {
    val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
    val projection =
      arrayOf(Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE)
    val folders = mutableListOf<Pair<String, String>>()
    // One unreadable folder must not hide the videos in all the others.
    runCatching { resolver.query(children, projection, null, null, null) }
      .getOrNull()
      ?.use { cursor ->
        while (cursor.moveToNext()) {
          val id = cursor.getString(0)
          val name = cursor.getString(1) ?: continue
          val mime = cursor.getString(2).orEmpty()
          if (mime == Document.MIME_TYPE_DIR) {
            folders += id to "$prefix$name/"
          } else if (mime.startsWith("video/") || stripExtension(name) != name) {
            out.putIfAbsent(
              key(stripExtension(name)),
              LocalVideo(
                DocumentsContract.buildDocumentUriUsingTree(treeUri, id),
                fullPath(id) ?: "$prefix$name",
              ),
            )
          }
        }
      }
    folders.forEach { (id, path) -> walk(resolver, treeUri, id, path, out) }
  }

  /**
   * Turns an external-storage document id ("primary:Movies/x.mp4" or "1A2B-3C4D:Movies/x.mp4") into
   * a file path. Null for ids from other providers.
   */
  private fun fullPath(documentId: String): String? {
    val volume = documentId.substringBefore(':', "")
    if (volume.isEmpty()) return null
    val root = if (volume == "primary") "/storage/emulated/0" else "/storage/$volume"
    return "$root/${documentId.substringAfter(':')}"
  }

  private val WHITESPACE = Regex("\\s+")
}
