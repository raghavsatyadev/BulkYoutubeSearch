package io.github.raghavsatyadev.bys.ui.found_songs

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoundSongsViewModel : CoreViewModel() {
    fun loadListUsingPagination(adapter: FoundSongsAdapter) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                SongDetailDataUtil.getInstance().getAllSortedLive().collectLatest {
                    withContext(mainDispatcher) {
                        adapter.replaceAll(it)
                    }
                }
            }
        }
    }

    fun deleteSong(detail: SongDetail) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                SongDetailDataUtil
                    .getInstance()
                    .delete(detail)
            }
        }
    }
}