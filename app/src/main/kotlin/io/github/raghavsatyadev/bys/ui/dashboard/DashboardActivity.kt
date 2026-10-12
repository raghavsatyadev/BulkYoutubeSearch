package io.github.raghavsatyadev.bys.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import io.github.raghavsatyadev.bys.databinding.ActivityDashboardBinding
import io.github.raghavsatyadev.bys.ui.found_songs.FoundSongsActivity
import io.github.raghavsatyadev.bys.ui.manage_keys.ManageKeysActivity
import io.github.raghavsatyadev.bys.ui.search.SearchActivity
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : CoreActivity<ActivityDashboardBinding>() {
  private val viewModel: DashboardViewModel by viewModels()

  companion object {
    fun getIntentObject(context: Context, bundle: Bundle = Bundle.EMPTY): Intent =
      Intent(context, DashboardActivity::class.java).apply { putExtras(bundle) }
  }

  override fun createReference(savedInstanceState: Bundle?) {
    loadAds(binding.adView)

    startSetup()
  }

  private fun startSetup() {
    launch { withContext(ioDispatcher) { viewModel.setupData() } }
  }

  override fun createBinding(savedInstanceState: Bundle?) =
    ActivityDashboardBinding.inflate(layoutInflater)

  override fun getToolBar() = binding.toolbar

  override fun setListeners(isEnabled: Boolean) {
    if (isEnabled) {
      binding.btnSearch.setOnClickListener {
        startActivityWithTransition(SearchActivity.getIntentObject(this))
      }
      binding.btnFoundSongs.setOnClickListener {
        startActivityWithTransition(FoundSongsActivity.getIntentObject(this))
      }
      binding.btnManageKeys.setOnClickListener {
        startActivityWithTransition(ManageKeysActivity.getIntentObject(this))
      }
    } else {
      binding.btnFoundSongs.setOnClickListener(null)
      binding.btnSearch.setOnClickListener(null)
      binding.btnManageKeys.setOnClickListener(null)
    }
  }
}
