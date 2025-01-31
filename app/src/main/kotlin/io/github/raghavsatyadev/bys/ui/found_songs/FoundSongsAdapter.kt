package io.github.raghavsatyadev.bys.ui.found_songs

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.raghavsatyadev.bys.databinding.ItemFoundSongBinding
import io.github.raghavsatyadev.support.list.GenRecyclerAdapter
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail

class FoundSongsAdapter(private val placeholder: Drawable?) : GenRecyclerAdapter<SongDetail, ItemFoundSongBinding, FoundSongsHolder>() {

    override fun creatingViewHolder(
        parent: ViewGroup,
        viewType: Int,
        from: LayoutInflater,
    ): FoundSongsHolder {
        return FoundSongsHolder.getInstance(
            placeholder,
            ItemFoundSongBinding.inflate(
                from,
                parent,
                false
            ),
            itemClickListener
        )
    }
}