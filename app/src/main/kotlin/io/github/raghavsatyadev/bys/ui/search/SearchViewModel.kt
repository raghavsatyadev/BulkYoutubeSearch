package io.github.raghavsatyadev.bys.ui.search

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.StorageUtils
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchUtil
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SearchViewModel : CoreViewModel() {
  fun searchVideos() {
    viewModelScope.launch { withContext(ioDispatcher) { YoutubeSearchWorker.startWorker() } }
  }

  fun prepareSharingFile(listener: ((File) -> Unit)) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val songDetails = SongDetailDataUtil
          .getInstance()
          .getAllSorted()
        val songTitles = songDetails
          .map { it.title }
          .toList()
          .toJsonString()
        val videoLinksFinal = StorageUtils.createFile(
          "Videos",
          "video_links_final.json",
          true
        )
        val videoTitlesFinal = StorageUtils.createFile(
          "Videos",
          "video_titles_final.json",
          true
        )
        videoLinksFinal.mkdirs()

        videoLinksFinal.writeText(songDetails.toJsonString())
        videoTitlesFinal.writeText(songTitles)
        withContext(mainDispatcher) { listener(videoLinksFinal) }
      }
    }
  }

  fun setupData() {
    viewModelScope.launch { withContext(ioDispatcher) { YoutubeSearchUtil.setupData() } }
  }

  fun onJsonPicked(
    fileUri: Uri,
    contentResolver: ContentResolver,
    onSuccess: () -> Unit,
    showInvalidJSONWarning: () -> Unit,
  ) {
    contentResolver.takePersistableUriPermission(
      fileUri,
      Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
    viewModelScope.launch(Dispatchers.IO) {
      contentResolver
        .openInputStream(fileUri)
        ?.bufferedReader()
        .use { reader ->
          val json = reader?.readText()
          val videoNames = json?.toKotlinObject<ArrayList<String>>()
          if (videoNames.isNullOrEmpty()) {
            withContext(Dispatchers.Main) { showInvalidJSONWarning() }
          } else {
            val videoNamesFile = StorageUtils.createFile(
              Constants.FileNames.PARENT_FOLDER_NAME,
              Constants.FileNames.VIDEO_NAMES,
              true
            )

            videoNamesFile.mkdirs()
            videoNamesFile.writeText(json)
            withContext(Dispatchers.Main) { onSuccess() }
          }
        }
    }
  }
}
