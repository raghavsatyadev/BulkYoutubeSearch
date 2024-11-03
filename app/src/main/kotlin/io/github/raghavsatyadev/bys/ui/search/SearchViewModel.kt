package io.github.raghavsatyadev.bys.ui.search

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel : CoreViewModel() {
    fun searchVideos() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                YoutubeSearchWorker.startWorker()
            }
        }
    }
}