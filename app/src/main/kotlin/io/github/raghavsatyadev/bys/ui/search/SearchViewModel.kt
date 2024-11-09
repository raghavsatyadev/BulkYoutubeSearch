package io.github.raghavsatyadev.bys.ui.search

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.StorageUtils
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toJsonString
import io.github.raghavsatyadev.support.listeners.ResultListener
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.work_manager.YoutubeSearch
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SearchViewModel : CoreViewModel() {
    fun searchVideos() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                YoutubeSearchWorker.startWorker()
            }
        }
    }

    fun prepareSharingFile(listener: ResultListener<File>) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val songDetails = SongDetailDataUtil.getInstance().getAll()
                val songTitles = songDetails.map {
                    it.title
                }.toList().toJsonString()
                val videoLinksFinal =
                    StorageUtils.createFile("Videos", "video_links_final.json", true)
                val videoTitlesFinal =
                    StorageUtils.createFile("Videos", "video_titles_final.json", true)
                videoLinksFinal.mkdirs()

                videoLinksFinal.writeText(songDetails.toJsonString())
                videoTitlesFinal.writeText(songTitles)
                withContext(mainDispatcher) {
                    listener.onResult(videoLinksFinal)
                }
            }
        }
    }

    fun setupData() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                YoutubeSearch.setupData()
            }
        }
    }
}