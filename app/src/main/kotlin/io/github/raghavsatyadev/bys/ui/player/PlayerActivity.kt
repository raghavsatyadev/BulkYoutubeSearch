package io.github.raghavsatyadev.bys.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.activity.viewModels
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import io.github.raghavsatyadev.bys.databinding.ActivityPlayerBinding
import io.github.raghavsatyadev.support.Constants.Other.YOUTUBE_LINK
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail

class PlayerActivity : CoreActivity<ActivityPlayerBinding>() {
    private val viewModel: PlayerViewModel by viewModels()
    private val tracker = YouTubePlayerTracker()
    private var player: YouTubePlayer? = null

    companion object {
        const val SONG = "SONG"
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent = Intent(
            context,
            PlayerActivity::class.java
        ).apply { putExtras(bundle) }
    }


    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)
        setupYoutubePlayer()
    }

    private fun setupYoutubePlayer() {
        val songDetail = intent
            .getStringExtra(SONG)
            ?.toKotlinObject<SongDetail>()

        if (songDetail != null) {
            lifecycle.addObserver(binding.youtubePlayerView)

            binding.youtubePlayerView.addYouTubePlayerListener(object :
                AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)
                    player = youTubePlayer
                    youTubePlayer.loadVideo(
                        songDetail.link.replace(
                            YOUTUBE_LINK,
                            ""
                        ),
                        0F
                    )
                    youTubePlayer.addListener(tracker)

                }
            })
            binding.youtubePlayerView.addFullscreenListener(object : FullscreenListener {
                override fun onEnterFullscreen(
                    fullscreenView: View,
                    exitFullscreen: () -> Unit,
                ) {

                }

                override fun onExitFullscreen() {

                }

            })
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(
                16,
                9
            ) // Adjust to your video's aspect ratio
            val pipParams = PictureInPictureParams
                .Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(pipParams)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(
            isInPictureInPictureMode,
            newConfig
        )
        if (isInPictureInPictureMode) {
            binding.adView.gone()
        } else {
            binding.adView.visible()
        }
    }

    override fun onDestroy() {
        binding.youtubePlayerView.release()
        super.onDestroy()
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityPlayerBinding.inflate(layoutInflater)

    override fun setListeners(isEnabled: Boolean) = Unit
}

