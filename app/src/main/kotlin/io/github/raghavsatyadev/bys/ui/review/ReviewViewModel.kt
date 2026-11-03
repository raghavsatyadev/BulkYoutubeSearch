package io.github.raghavsatyadev.bys.ui.review

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.support.StorageUtils
import io.github.raghavsatyadev.support.backup.DbBackup
import io.github.raghavsatyadev.support.backup.DbBackupWorker
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.models.db.song_review.ReviewVerdict
import io.github.raghavsatyadev.support.models.db.song_review.SongReview
import io.github.raghavsatyadev.support.models.db.song_review.SongReviewDataUtil
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.getPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.savePref
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/** A found song with its review and its local video, if any. */
data class ReviewItem(val song: SongDetail, val review: SongReview?, val local: LocalVideo?) {
  val key: String
    get() = song.oldTitle
}

enum class ReviewFilter {
  ALL,
  NOT_REVIEWED,
  REVIEWED,
  MATCH,
  REJECTED,
  MISMATCH;

  fun accepts(item: ReviewItem): Boolean =
    when (this) {
      ALL -> true
      NOT_REVIEWED -> item.review == null
      REVIEWED -> item.review != null
      MATCH -> item.review?.verdict == ReviewVerdict.MATCH
      REJECTED -> item.review?.verdict == ReviewVerdict.REJECTED
      MISMATCH -> item.review?.verdict == ReviewVerdict.MISMATCH
    }
}

/** One row of the exported review file; stands on its own without the app's database. */
@Keep
@Serializable
data class ReviewExportRow(
  val title: String,
  val artist: String,
  val youtubeUrl: String,
  val oldTitle: String,
  val localPath: String,
  val verdict: String,
  val reviewedAt: String,
)

/** State for the review list and the review player. */
class ReviewViewModel : ViewModel() {
  private val app = CoreApp.instance
  private val reviewDao = SongReviewDataUtil.dao

  private fun <T> Flow<T>.state(initial: T): StateFlow<T> =
    stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initial)

  /** The picked folder of local videos; empty until one is picked. */
  val folderUri: StateFlow<String?> =
    app.getPrefs(AppPrefsUtil.Keys.REVIEW_FOLDER_URI, "").state(null)

  private val localIndex = MutableStateFlow<Map<String, LocalVideo>>(emptyMap())
  private val _scanning = MutableStateFlow(false)
  val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

  private val reviews = reviewDao.getAllLive()

  private val _backupBlocked = MutableStateFlow(false)

  /** True when the picked folder is read-only, so the daily backup cannot be written. */
  val backupBlocked: StateFlow<Boolean> = _backupBlocked.asStateFlow()

  /** Every found song, sorted by title, with its review and local video; null while loading. */
  val items: StateFlow<List<ReviewItem>?> =
    combine(SongDetailDataUtil.getInstance().getAllSortedLive(), reviews, localIndex) {
        songs,
        reviews,
        local ->
        val byTitle = reviews.associateBy { it.oldTitle }
        songs.map { song ->
          ReviewItem(song, byTitle[song.oldTitle], local[LocalVideoIndex.key(song.oldTitle)])
        }
      }
      .state(null)

  private val _filter = MutableStateFlow(ReviewFilter.NOT_REVIEWED)
  val filter: StateFlow<ReviewFilter> = _filter.asStateFlow()

  val visible: StateFlow<List<ReviewItem>?> =
    combine(items, _filter) { all, filter -> all?.filter(filter::accepts) }.state(null)

  /**
   * The songs the player steps through: the list as it was when a song was opened, so marking a
   * song does not drop it from under the player when the list is filtered.
   */
  private val _queue = MutableStateFlow<List<String>>(emptyList())
  val queue: StateFlow<List<String>> = _queue.asStateFlow()

  init {
    DbBackupWorker.schedule(app)
    viewModelScope.launch {
      folderUri.collect { uri ->
        if (!uri.isNullOrEmpty()) {
          _backupBlocked.value = !DbBackup.canWrite(app, Uri.parse(uri))
          rescan(Uri.parse(uri))
        }
      }
    }
  }

  fun setFilter(filter: ReviewFilter) {
    _filter.value = filter
  }

  /**
   * Keeps access to the picked folder across restarts, brings back the reviews from a backup in it
   * (after a reinstall), then saves a fresh backup and indexes the videos.
   */
  fun onFolderPicked(uri: Uri) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    runCatching { app.contentResolver.takePersistableUriPermission(uri, flags) }
    viewModelScope.launch {
      // Restore before anything writes, so an empty database never replaces the backup.
      val restored = runCatching { DbBackup.restore(app, uri) }.getOrDefault(0)
      if (restored > 0) {
        Toast.makeText(app, app.getString(R.string.review_restored, restored), Toast.LENGTH_LONG)
          .show()
      }
      runCatching { DbBackup.write(app, uri) }
      app.savePref(AppPrefsUtil.Keys.REVIEW_FOLDER_URI, uri.toString())
    }
  }

  /**
   * Saves the backup file into the picked folder now, on top of the daily one. Reports a message id
   * through [onDone].
   */
  fun backupNow(onDone: (Int) -> Unit) {
    val uri = folderUri.value
    if (uri.isNullOrEmpty()) return onDone(R.string.review_backup_no_folder)
    viewModelScope.launch {
      val message = runCatching {
        DbBackup.write(app, Uri.parse(uri))
      }
        .fold(
          onSuccess = { saved ->
            if (saved) R.string.review_backup_saved else R.string.review_backup_empty
          },
          onFailure = { e ->
            if (e is SecurityException) R.string.review_backup_blocked
            else R.string.review_backup_failed
          },
        )
      onDone(message)
    }
  }

  /** Scans the picked folder again, to pick up videos copied since the last scan. */
  fun refresh() {
    val uri = folderUri.value
    if (!uri.isNullOrEmpty()) viewModelScope.launch { rescan(Uri.parse(uri)) }
  }

  private suspend fun rescan(uri: Uri) {
    if (_scanning.value) return
    _scanning.value = true
    localIndex.value =
      withContext(Dispatchers.IO) {
        runCatching { LocalVideoIndex.scan(app.contentResolver, uri) }.getOrDefault(emptyMap())
      }
    _scanning.value = false
  }

  /** Opens the player on [key] with the list as it is shown now. */
  fun startQueue(key: String) {
    val shown = visible.value.orEmpty().map { it.key }
    _queue.value = if (key in shown) shown else listOf(key)
  }

  /** Saves [verdict] for [item]; the same verdict again clears the review. */
  fun mark(item: ReviewItem, verdict: ReviewVerdict) {
    viewModelScope.launch(Dispatchers.IO) {
      if (item.review?.verdict == verdict) {
        reviewDao.delete(item.key)
      } else {
        reviewDao.upsert(
          SongReview(
            oldTitle = item.key,
            link = item.song.link,
            verdict = verdict,
            reviewedAt = System.currentTimeMillis(),
            localPath = item.local?.path.orEmpty(),
          )
        )
      }
    }
  }

  /** Writes every review, joined with its song, to a JSON file and hands it to [onReady]. */
  fun exportReviews(onReady: (File) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val songs = SongDetailDataUtil.getInstance().getAllSorted().associateBy { it.oldTitle }
      val rows =
        reviewDao.getAll().map { review ->
          val song = songs[review.oldTitle]
          ReviewExportRow(
            title = song?.title.orEmpty(),
            artist = song?.artist.orEmpty(),
            youtubeUrl = review.link,
            oldTitle = review.oldTitle,
            localPath = review.localPath,
            verdict = review.verdict.name,
            reviewedAt = Instant.ofEpochMilli(review.reviewedAt).toString(),
          )
        }
      val file = StorageUtils.createFile("Videos", EXPORT_FILE, true)
      file.writeText(rows.toJsonString())
      withContext(Dispatchers.Main) { onReady(file) }
    }
  }

  private companion object {
    const val STOP_TIMEOUT_MILLIS = 5_000L
    const val EXPORT_FILE = "song_reviews.json"
  }
}

/** Counts for the dashboard card. */
fun List<ReviewItem>.reviewedCount(): Int = count { it.review != null }
