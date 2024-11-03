package io.github.raghavsatyadev.bys.ui.search

import io.github.raghavsatyadev.bys.databinding.ItemKeyDetailBinding
import io.github.raghavsatyadev.support.extensions.DateExtensions.formatMillisToDate
import io.github.raghavsatyadev.support.list.CustomClickListener
import io.github.raghavsatyadev.support.list.GenObjectHolder
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil

class KeyDetailHolder(
    binding: ItemKeyDetailBinding,
    itemClickListener: CustomClickListener?,
) : GenObjectHolder<AppPrefsUtil.APIKeyDetail, ItemKeyDetailBinding>(binding, itemClickListener) {
    companion object {
        fun getInstance(
            binding: ItemKeyDetailBinding,
            itemClickListener: CustomClickListener?,
        ): KeyDetailHolder {
            return KeyDetailHolder(binding, itemClickListener)
        }
    }

    init {
        binding.btnCopyKey.setOnClickListener {
            itemClickListener?.onItemClick(layoutPosition, it, false)
        }
    }

    override fun bind(
        model: AppPrefsUtil.APIKeyDetail,
        itemViewType: Int,
        position: Int,
        itemCount: Int,
    ) {
        with(binding) {
            with(model) {
                txtKeyAppName.text = appName
                txtKeyExpiry.text = expiry.formatMillisToDate()
            }
        }
    }
}