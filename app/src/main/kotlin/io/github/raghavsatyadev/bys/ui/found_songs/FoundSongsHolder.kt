package io.github.raghavsatyadev.bys.ui.found_songs

import android.graphics.drawable.Drawable
import android.text.Html
import io.github.raghavsatyadev.bys.databinding.ItemFoundSongBinding
import io.github.raghavsatyadev.support.extensions.ImageExtensions.loadImage
import io.github.raghavsatyadev.support.list.CustomClickListener
import io.github.raghavsatyadev.support.list.GenObjectHolder
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail

class FoundSongsHolder(
    private val placeholder: Drawable?,
    binding: ItemFoundSongBinding,
    itemClickListener: CustomClickListener?,
) : GenObjectHolder<SongDetail, ItemFoundSongBinding>(
    binding,
    itemClickListener
) {
    companion object {
        fun getInstance(
            placeholder: Drawable?,
            binding: ItemFoundSongBinding,
            itemClickListener: CustomClickListener?,
        ): FoundSongsHolder {
            return FoundSongsHolder(
                placeholder,
                binding,
                itemClickListener
            )
        }
    }

    init {
        binding.btnDelete.setOnClickListener {
            itemClickListener?.onItemClick(
                layoutPosition,
                binding.btnDelete,
                false
            )
        }
    }

    override fun bind(
        model: SongDetail,
        itemViewType: Int,
        position: Int,
        itemCount: Int,
    ) {
        with(binding) {
            with(model) {
                txtFoundSongTitle.text = Html
                    .fromHtml(
                        title,
                        Html.FROM_HTML_MODE_COMPACT
                    )
                    .toString()
                txtFoundSongArtist.text = Html
                    .fromHtml(
                        artist,
                        Html.FROM_HTML_MODE_COMPACT
                    )
                    .toString()
                imgFoundSong.loadImage(
                    thumbnailLow,
                    placeHolder = placeholder,
                    crossFade = true,
                    listener = {
                        imgFoundSong.loadImage(
                            thumbnailHigh,
                            crossFade = true,
                        )
                    })
            }
        }
    }
}