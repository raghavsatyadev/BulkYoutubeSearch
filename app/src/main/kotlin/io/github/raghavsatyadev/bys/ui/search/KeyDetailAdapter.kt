package io.github.raghavsatyadev.bys.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.raghavsatyadev.bys.databinding.ItemKeyDetailBinding
import io.github.raghavsatyadev.support.list.GenRecyclerAdapter
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil

class KeyDetailAdapter :
    GenRecyclerAdapter<AppPrefsUtil.APIKeyDetail, ItemKeyDetailBinding, KeyDetailHolder>() {
    override fun creatingViewHolder(
        parent: ViewGroup,
        viewType: Int,
        from: LayoutInflater,
    ): KeyDetailHolder {
        return KeyDetailHolder.getInstance(
            ItemKeyDetailBinding.inflate(from, parent, false),
            itemClickListener
        )
    }
}