package io.github.raghavsatyadev.bys.ui.found_songs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.raghavsatyadev.bys.databinding.ActivityFoundSongsBinding
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.openBrowser
import io.github.raghavsatyadev.support.extensions.ResourceExtensions.getConDrawable
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.list.CustomClickListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoundSongsActivity : CoreActivity<ActivityFoundSongsBinding>() {
    private val viewModel: FoundSongsViewModel by viewModels()
    private val adapter by lazy {
        FoundSongsAdapter(getConDrawable(io.github.raghavsatyadev.support.R.drawable.img_placeholder))
    }

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent =
            Intent(context, FoundSongsActivity::class.java).apply { putExtras(bundle) }
    }


    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)

        binding.listFoundSongs.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(ioDispatcher) {
                    viewModel.loadListUsingPagination(adapter)
                }
            }
        }
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityFoundSongsBinding.inflate(layoutInflater)

    override fun getToolBar() = binding.toolbar

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            adapter.itemClickListener = CustomClickListener(
                onClick = { position, view, _ ->
                    openBrowser(adapter.getItem(position).link)
                }
            )
        }
    }
}