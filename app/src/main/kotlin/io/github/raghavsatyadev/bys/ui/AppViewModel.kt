package io.github.raghavsatyadev.bys.ui

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.StorageUtils
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.models.db.song_detail.SongRetrievalMode
import io.github.raghavsatyadev.support.models.general.APIKeyDetail
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchUtil
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchWorker
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** One state holder for the app: the screens are small and share the same data. */
class AppViewModel : ViewModel() {
  private val songs = SongDetailDataUtil.getInstance()

  private fun <T> kotlinx.coroutines.flow.Flow<T>.state(initial: T): StateFlow<T> =
    stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initial)

  /** Found songs, sorted by title; null until the first read arrives. */
  val foundSongs: StateFlow<List<SongDetail>?> = songs.getAllSortedLive().state(null)
  val foundCount: StateFlow<Long> = songs.getCountLive(SongRetrievalMode.FOUND).state(0L)
  val remainingCount: StateFlow<Long> = songs.getCountLive(SongRetrievalMode.NOT_FOUND).state(0L)
  val keys: StateFlow<List<APIKeyDetail>?> = AppPrefsUtil.getKeyDetails().state(null)
  val oneTimeRunning: StateFlow<Boolean> = YoutubeSearchWorker.oneTimeRunning().state(false)
  val periodicRunning: StateFlow<Boolean> = YoutubeSearchWorker.periodicRunning().state(false)

  private val _selectedFile = MutableStateFlow<String?>(null)
  /** The name of the picked list of video names, once it was read. */
  val selectedFile: StateFlow<String?> = _selectedFile.asStateFlow()

  fun setupData() {
    viewModelScope.launch(Dispatchers.IO) { YoutubeSearchUtil.setupData() }
  }

  fun searchVideos() {
    viewModelScope.launch(Dispatchers.IO) { YoutubeSearchWorker.startWorker() }
  }

  fun addKey(appName: String, key: String) {
    viewModelScope.launch(Dispatchers.IO) { AppPrefsUtil.addKeyDetail(appName, key) }
  }

  fun deleteKey(key: String) {
    viewModelScope.launch(Dispatchers.IO) { AppPrefsUtil.deleteKeyDetail(key) }
  }

  fun deleteSong(song: SongDetail) {
    viewModelScope.launch(Dispatchers.IO) { songs.delete(song) }
  }

  /** Puts back a song removed by [deleteSong] (the snackbar's undo). */
  fun restoreSong(song: SongDetail) {
    viewModelScope.launch(Dispatchers.IO) { songs.insertReplace(song) }
  }

  /** Writes the found songs to two JSON files and hands the links file to [onReady]. */
  fun prepareSharingFile(onReady: (File) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val all = songs.getAllSorted()
      val links = StorageUtils.createFile("Videos", "video_links_final.json", true)
      val titles = StorageUtils.createFile("Videos", "video_titles_final.json", true)
      links.writeText(all.toJsonString())
      titles.writeText(all.map { it.title }.toJsonString())
      withContext(Dispatchers.Main) { onReady(links) }
    }
  }

  /**
   * Reads a picked JSON file and stores it for the search. It is either a list of video names, or a
   * links file exported earlier (a list of songs), whose found songs show up at once. Calls
   * [onInvalid] when the file is neither, or is empty.
   */
  fun onJsonPicked(uri: Uri, name: String?, resolver: ContentResolver, onInvalid: () -> Unit) {
    runCatching {
      resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    viewModelScope.launch(Dispatchers.IO) {
      val json = resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
      val names = runCatching { json?.toKotlinObject<ArrayList<String>>() }.getOrNull()
      val links =
        if (names == null) {
          runCatching { json?.let { linksJson.decodeFromString<ArrayList<SongDetail>>(it) } }
            .getOrNull()
        } else null
      if (json == null || (names.isNullOrEmpty() && links.isNullOrEmpty())) {
        withContext(Dispatchers.Main) { onInvalid() }
        return@launch
      }
      if (links != null) {
        // The search merges this file in too, so the songs survive a reinstall of the list.
        StorageUtils.createFile(
            Constants.FileNames.PARENT_FOLDER_NAME,
            Constants.FileNames.VIDEO_LINKS,
            true,
          )
          .writeText(links.toJsonString())
        songs.upsert(links)
      } else {
        StorageUtils.createFile(
            Constants.FileNames.PARENT_FOLDER_NAME,
            Constants.FileNames.VIDEO_NAMES,
            true,
          )
          .writeText(json)
      }
      _selectedFile.value = name ?: uri.lastPathSegment
    }
  }

  private companion object {
    const val STOP_TIMEOUT_MILLIS = 5_000L

    /** Exported links files carry extra fields (such as localPath) the app does not keep. */
    val linksJson = Json { ignoreUnknownKeys = true }
  }
}
