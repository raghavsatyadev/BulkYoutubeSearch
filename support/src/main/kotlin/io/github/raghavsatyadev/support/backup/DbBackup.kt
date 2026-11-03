package io.github.raghavsatyadev.support.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import androidx.annotation.Keep
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.models.db.song_detail.SongRetrievalMode
import io.github.raghavsatyadev.support.models.db.song_review.SongReview
import io.github.raghavsatyadev.support.models.db.song_review.SongReviewDataUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/** Everything the app cannot get back on its own: the found songs and their reviews. */
@Keep
@Serializable
data class DbBackupFile(
  val savedAt: Long,
  val songs: List<SongDetail>,
  val reviews: List<SongReview>,
)

/**
 * Keeps a copy of the database in the picked videos folder. That folder is shared storage, so the
 * copy survives an uninstall; picking the folder again after a reinstall brings the data back.
 */
object DbBackup {
  const val FILE_NAME = "bys_backup.json"
  private const val MIME = "application/json"

  /**
   * Writes the backup into [treeUri]. Returns false when there are no reviews yet, so an empty
   * database never replaces a good backup. Throws [SecurityException] without write access.
   */
  suspend fun write(context: Context, treeUri: Uri): Boolean =
    withContext(Dispatchers.IO) {
      val reviews = SongReviewDataUtil.dao.getAll()
      if (reviews.isEmpty()) return@withContext false
      val songs = SongDetailDataUtil.getInstance().getAllSorted(SongRetrievalMode.ALL)
      val json = DbBackupFile(System.currentTimeMillis(), songs, reviews).toJsonString()
      val resolver = context.contentResolver
      val file =
        find(resolver, treeUri)
          ?: DocumentsContract.createDocument(resolver, root(treeUri), MIME, FILE_NAME)
          ?: error("Could not create $FILE_NAME")
      // "wt" truncates; plain "w" can leave old bytes after a shorter file.
      requireNotNull(resolver.openOutputStream(file, "wt")).use { it.write(json.toByteArray()) }
      true
    }

  /**
   * Merges the backup in [treeUri] into the database: songs through the normal upsert, reviews only
   * where the backup's is newer. Returns how many reviews it brought back.
   */
  suspend fun restore(context: Context, treeUri: Uri): Int =
    withContext(Dispatchers.IO) {
      val resolver = context.contentResolver
      val file = find(resolver, treeUri) ?: return@withContext 0
      val backup =
        requireNotNull(resolver.openInputStream(file))
          .use { it.readBytes().decodeToString() }
          .toKotlinObject<DbBackupFile>()
      SongDetailDataUtil.getInstance().upsert(ArrayList(backup.songs))
      val dao = SongReviewDataUtil.dao
      val existing = dao.getAll().associateBy { it.oldTitle }
      val newer =
        backup.reviews.filter { it.reviewedAt > (existing[it.oldTitle]?.reviewedAt ?: -1) }
      newer.forEach { dao.upsert(it) }
      newer.size
    }

  private fun root(treeUri: Uri): Uri =
    DocumentsContract.buildDocumentUriUsingTree(
      treeUri,
      DocumentsContract.getTreeDocumentId(treeUri),
    )

  private fun find(resolver: ContentResolver, treeUri: Uri): Uri? {
    val children =
      DocumentsContract.buildChildDocumentsUriUsingTree(
        treeUri,
        DocumentsContract.getTreeDocumentId(treeUri),
      )
    val projection = arrayOf(Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME)
    resolver.query(children, projection, null, null, null)?.use { cursor ->
      while (cursor.moveToNext()) {
        if (cursor.getString(1) == FILE_NAME) {
          return DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0))
        }
      }
    }
    return null
  }

  /** True when the app may write into [treeUri]; folders picked before backups were read-only. */
  fun canWrite(context: Context, treeUri: Uri): Boolean =
    context.contentResolver.persistedUriPermissions.any {
      it.uri == treeUri && it.isWritePermission
    }
}
