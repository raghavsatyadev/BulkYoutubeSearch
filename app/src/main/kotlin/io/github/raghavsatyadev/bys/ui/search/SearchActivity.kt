package io.github.raghavsatyadev.bys.ui.search

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
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.databinding.ActivitySearchBinding
import io.github.raghavsatyadev.support.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.ads.AdExtensions.showInterstitialAd
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.copyToClipboard
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.shareFile
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import io.github.raghavsatyadev.support.list.CustomClickListener
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : CoreActivity<ActivitySearchBinding>() {
    private val viewModel: SearchViewModel by viewModels()
    private val adapter = KeyDetailAdapter()

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent =
            Intent(context, SearchActivity::class.java).apply { putExtras(bundle) }
    }


    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)
        setupUI()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getPermissions()
            }
        }
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivitySearchBinding.inflate(layoutInflater)

    override fun getToolBar() = binding.toolbar

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            adapter.itemClickListener = CustomClickListener { position, _, _ ->
                showInterstitialAd {
                    copyToClipboard(
                        "API Key",
                        adapter.items[position].key
                    )
                }
            }
            binding.btnSearchVideos.setOnClickListener {
                showInterstitialAd {
                    launch {
                        withContext(ioDispatcher) {
                            viewModel.searchVideos()
                        }
                    }
                }
            }
            binding.btnShareResultFile.setOnClickListener {
                showInterstitialAd {
                    launch {
                        withContext(ioDispatcher) {
                            viewModel.prepareSharingFile { file ->
                                file.shareFile(this@SearchActivity)
                            }
                        }
                    }
                }
            }
        } else {
            adapter.itemClickListener = null
            binding.btnShareResultFile.setOnClickListener(null)
            binding.btnSearchVideos.setOnClickListener(null)
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

    private fun setupUI() {
        binding.listKeyExpiry.adapter = adapter
        launch {
            AppPrefsUtil.getRemainingSongsToBeFound().collectLatest {
                withContext(mainDispatcher) {
                    binding.txtSongsRemainToBeFound.text =
                        getString(R.string.songs_remain_to_be_found, it)
                }
            }
        }
        launch {
            AppPrefsUtil.getKeyDetails().collectLatest {
                withContext(mainDispatcher) {
                    adapter.replaceAll(it)
                }
            }
        }
        launch {
            YoutubeSearchWorker.listenToWorkStatus({
                launch {
                    withContext(mainDispatcher) {
                        if (it) {
                            binding.progressOneTimeWorkStatus.visible()
                        } else {
                            binding.progressOneTimeWorkStatus.gone()
                        }
                    }
                }
            }, {
                launch {
                    withContext(mainDispatcher) {
                        if (it) {
                            binding.progressPeriodicWorkStatus.visible()
                        } else {
                            binding.progressPeriodicWorkStatus.gone()
                        }
                    }
                }
            })
        }
    }
}

