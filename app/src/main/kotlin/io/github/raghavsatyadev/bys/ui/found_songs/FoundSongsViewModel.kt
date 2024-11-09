package io.github.raghavsatyadev.bys.ui.found_songs

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoundSongsViewModel : CoreViewModel() {
    fun loadListUsingPagination(adapter: FoundSongsAdapter) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val all = SongDetailDataUtil.getInstance().getAll()
                withContext(mainDispatcher) {
                    adapter.replaceAll(all)
                }
            }
        }
    }
}