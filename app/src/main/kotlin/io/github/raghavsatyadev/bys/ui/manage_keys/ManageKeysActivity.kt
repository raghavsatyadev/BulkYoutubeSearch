package io.github.raghavsatyadev.bys.ui.manage_keys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import io.github.raghavsatyadev.bys.databinding.ActivityManageKeysBinding
import io.github.raghavsatyadev.support.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.core.CoreActivity

class ManageKeysActivity : CoreActivity<ActivityManageKeysBinding>() {
    private val viewModel: ManageKeysViewModel by viewModels()

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent =
            Intent(context, ManageKeysActivity::class.java).apply { putExtras(bundle) }
    }


    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)
    }

    override fun getToolBar() = binding.toolbar

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityManageKeysBinding.inflate(layoutInflater)

    override fun setListeners(isEnabled: Boolean) {}
}

