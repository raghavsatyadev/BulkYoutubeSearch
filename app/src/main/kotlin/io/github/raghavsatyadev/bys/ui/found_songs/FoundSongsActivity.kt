package io.github.raghavsatyadev.bys.ui.found_songs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import io.github.raghavsatyadev.bys.databinding.ActivityFoundSongsBinding
import io.github.raghavsatyadev.support.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.core.CoreActivity

class FoundSongsActivity : CoreActivity<ActivityFoundSongsBinding>() {
    private val viewModel: FoundSongsViewModel by viewModels()

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent =
            Intent(context, FoundSongsActivity::class.java).apply { putExtras(bundle) }
    }


    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityFoundSongsBinding.inflate(layoutInflater)

    override fun getToolBar() = binding.toolbar

    override fun setListeners(isEnabled: Boolean) {}
}

