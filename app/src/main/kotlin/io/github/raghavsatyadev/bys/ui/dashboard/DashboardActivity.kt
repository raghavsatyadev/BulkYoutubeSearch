package io.github.raghavsatyadev.bys.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent = Intent(
            context,
            DashboardActivity::class.java
        ).apply { putExtras(bundle) }
    }

    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getPermissions()
            }
        }
    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            } else {
                launch {
                    withContext(ioDispatcher) {
                        viewModel.setupData()
                    }
                }
            }
        }
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityDashboardBinding.inflate(layoutInflater)

    override fun getToolBar() = binding.toolbar

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            binding.btnSearch.setOnClickListener {
                startActivity(SearchActivity.getIntentObject(this))
            }
            binding.btnFoundSongs.setOnClickListener {
                startActivity(FoundSongsActivity.getIntentObject(this))
            }
            binding.btnManageKeys.setOnClickListener {
                startActivity(ManageKeysActivity.getIntentObject(this))
            }
        } else {
            binding.btnFoundSongs.setOnClickListener(null)
            binding.btnSearch.setOnClickListener(null)
        }
    }
}