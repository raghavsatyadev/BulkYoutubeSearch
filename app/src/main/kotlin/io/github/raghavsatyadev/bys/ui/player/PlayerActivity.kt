@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.ui.components.AdBanner
import io.github.raghavsatyadev.bys.ui.components.AppTopBar
import io.github.raghavsatyadev.bys.ui.theme.AppTheme
import io.github.raghavsatyadev.support.Constants.Other.YOUTUBE_LINK
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail

/** Plays one found song. Its own activity, so it can shrink into picture-in-picture. */
class PlayerActivity : ComponentActivity() {
  private var inPip by mutableStateOf(false)

  companion object {
    private const val SONG = "SONG"

    fun intent(context: Context, songJson: String): Intent =
      Intent(context, PlayerActivity::class.java).putExtra(SONG, songJson)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    val song = intent.getStringExtra(SONG)?.toKotlinObject<SongDetail>()
    if (song == null) {
      finish()
      return
    }
    setContent { AppTheme { PlayerScreen(song) } }
  }

  @Composable
  private fun PlayerScreen(song: SongDetail) {
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    // The player stays in the same place in the tree in and out of picture-in-picture, so the
    // video keeps playing; only the chrome around it comes and goes.
    Scaffold(
      topBar = {
        if (!inPip) {
          AppTopBar(
            title = stringResource(R.string.now_playing),
            scrollBehavior = scroll,
            onBack = ::finish,
          )
        }
      },
      bottomBar = { if (!inPip) AdBanner() },
    ) { padding ->
      Column(
        modifier =
          Modifier.fillMaxSize()
            .padding(if (inPip) androidx.compose.foundation.layout.PaddingValues() else padding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        AndroidView(
          modifier =
            if (inPip) Modifier.fillMaxSize()
            else
              Modifier.padding(16.dp)
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.large),
          factory = { context ->
            YouTubePlayerView(context).also { view ->
              lifecycle.addObserver(view)
              view.addYouTubePlayerListener(
                object : AbstractYouTubePlayerListener() {
                  override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(song.link.replace(YOUTUBE_LINK, ""), 0f)
                  }
                }
              )
            }
          },
        )
        if (!inPip) {
          Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              Html.fromHtml(song.title, Html.FROM_HTML_MODE_COMPACT).toString(),
              style = MaterialTheme.typography.headlineSmall,
            )
            Text(
              Html.fromHtml(song.artist, Html.FROM_HTML_MODE_COMPACT).toString(),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }

  override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      enterPictureInPictureMode(
        PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
      )
    }
  }

  override fun onPictureInPictureModeChanged(
    isInPictureInPictureMode: Boolean,
    newConfig: Configuration,
  ) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    inPip = isInPictureInPictureMode
  }
}
