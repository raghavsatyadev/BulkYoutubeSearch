package io.github.raghavsatyadev.bys.ui.dashboard

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel : CoreViewModel() {
    fun setupData() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                YoutubeSearchUtil.setupData()
            }
        }
    }
}