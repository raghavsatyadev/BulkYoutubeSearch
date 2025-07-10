package io.github.raghavsatyadev.bys.ui.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.databinding.ActivitySearchBinding
import io.github.raghavsatyadev.support.JsonFilePicker
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.ErrorShowExtensions.errorDialog
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.copyToClipboard
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.shareFile
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.showInterstitialAd
import io.github.raghavsatyadev.support.getFileNameFromUri
import io.github.raghavsatyadev.support.list.CustomClickListener
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.models.db.song_detail.SongRetrievalMode
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : CoreActivity<ActivitySearchBinding>() {
    private val viewModel: SearchViewModel by viewModels()
    private val adapter = KeyDetailAdapter()
    private var selectedFile: Uri = Uri.EMPTY
    private val jsonPickerLauncher = registerForActivityResult(JsonFilePicker()) { uri ->
        uri?.let {
            viewModel.onJsonPicked(
                it,
                this@SearchActivity.contentResolver,
                onSuccess = {
                    selectedFile = it
                    binding.txtSelectedFile.text = it.getFileNameFromUri(this@SearchActivity)
                    binding.txtSelectedFile.visible()
                },
                showInvalidJSONWarning = {
                    errorDialog(io.github.raghavsatyadev.support.R.string.warning_invalid_json_file)
                })
        }
    }

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent = Intent(
            context,
            SearchActivity::class.java
        ).apply { putExtras(bundle) }
    }

    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)
        setupUI()
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivitySearchBinding.inflate(layoutInflater)

    override fun getToolBar() = binding.toolbar

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            binding.btnSelectFile.setOnClickListener {
                showInterstitialAd { jsonPickerLauncher.launch(Unit) }
            }
            adapter.itemClickListener = CustomClickListener { position, _, _ ->
                showInterstitialAd {
                    copyToClipboard(
                        "API Key",
                        adapter.items[position].key
                    )
                }
            }
            binding.btnSearchVideos.setOnClickListener {
                if (selectedFile != Uri.EMPTY) {
                    showInterstitialAd { viewModel.searchVideos() }
                }
            }
            binding.btnShareResultFile.setOnClickListener {
                showInterstitialAd {
                    launch {
                        withContext(ioDispatcher) { viewModel.prepareSharingFile { file -> shareFile(file) } }
                    }
                }
            }
        } else {
            binding.btnSelectFile.setOnClickListener(null)
            adapter.itemClickListener = null
            binding.btnShareResultFile.setOnClickListener(null)
            binding.btnSearchVideos.setOnClickListener(null)
        }
    }

    private fun setupUI() {
        binding.listKeyExpiry.adapter = adapter
        launch {
            SongDetailDataUtil
                .getInstance()
                .getCountLive(SongRetrievalMode.NOT_FOUND)
                .collectLatest {
                    withContext(mainDispatcher) {
                        binding.txtSongsRemainToBeFound.text = getString(
                            R.string.songs_remain_to_be_found,
                            it
                        )
                    }
                }
        }
        launch {
            AppPrefsUtil
                .getKeyDetails()
                .collectLatest {
                    withContext(mainDispatcher) { adapter.replaceAll(it) }
                }
        }
        launch {
            YoutubeSearchWorker.listenToWorkStatus(
                {
                    launch {
                        withContext(mainDispatcher) {
                            if (it) {
                                binding.progressOneTimeWorkStatus.visible()
                            } else {
                                binding.progressOneTimeWorkStatus.gone()
                            }
                        }
                    }
                },
                {
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
