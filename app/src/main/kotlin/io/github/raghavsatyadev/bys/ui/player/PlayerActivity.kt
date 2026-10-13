@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
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

  /** Full screen from our button or the player's own: landscape, no system bars. */
  private var fullscreen by mutableStateOf(false)

  /** The player's own full-screen view, while its full-screen button is on. */
  private var playerFullscreenView by mutableStateOf<View?>(null)
  private var exitPlayerFullscreen: (() -> Unit)? = null

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
    setContent { AppTheme { Box { PlayerScreen(song) } } }
  }

  private fun exitFullscreen() {
    val exit = exitPlayerFullscreen
    if (exit != null) exit() else fullscreen = false
  }

  /** Landscape without system bars while full screen; back to portrait when it ends. */
  private fun applyFullscreen(on: Boolean) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    if (on) {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      controller.hide(WindowInsetsCompat.Type.systemBars())
    } else {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      controller.show(WindowInsetsCompat.Type.systemBars())
    }
  }

  @Composable
  private fun PlayerScreen(song: SongDetail) {
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    var wasFullscreen by remember { mutableStateOf(false) }
    LaunchedEffect(fullscreen) {
      // Leave the orientation alone on open; only undo what full screen changed.
      if (fullscreen || wasFullscreen) applyFullscreen(fullscreen)
      wasFullscreen = fullscreen
    }
    BackHandler(enabled = fullscreen) { exitFullscreen() }
    val immersive = inPip || fullscreen
    // The player stays in the same place in the tree in and out of picture-in-picture, so the
    // video keeps playing; only the chrome around it comes and goes.
    Scaffold(
      topBar = {
        if (!immersive) {
          AppTopBar(
            title = stringResource(R.string.now_playing),
            scrollBehavior = scroll,
            onBack = ::finish,
          )
        }
      },
      bottomBar = { if (!immersive) AdBanner() },
      containerColor =
        if (fullscreen) MaterialTheme.colorScheme.scrim else MaterialTheme.colorScheme.background,
    ) { padding ->
      Column(
        modifier =
          Modifier.fillMaxSize()
            .padding(
              if (immersive) androidx.compose.foundation.layout.PaddingValues() else padding
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        AndroidView(
          modifier =
            if (immersive) Modifier.fillMaxSize()
            else
              Modifier.padding(16.dp)
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.large),
          factory = { context ->
            YouTubePlayerView(context).also { view ->
              lifecycle.addObserver(view)
              view.enableAutomaticInitialization = false
              // The player's own full-screen button hands us its view to show full size.
              view.addFullscreenListener(
                object : FullscreenListener {
                  override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                    playerFullscreenView = fullscreenView
                    exitPlayerFullscreen = exitFullscreen
                    fullscreen = true
                  }

                  override fun onExitFullscreen() {
                    playerFullscreenView = null
                    exitPlayerFullscreen = null
                    fullscreen = false
                  }
                }
              )
              view.initialize(
                object : AbstractYouTubePlayerListener() {
                  override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(song.link.replace(YOUTUBE_LINK, ""), 0f)
                  }
                },
                IFramePlayerOptions.Builder(context).controls(1).fullscreen(1).build(),
              )
            }
          },
        )
        if (!immersive) {
          FilledTonalButton(
            onClick = { fullscreen = true },
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.padding(horizontal = 16.dp),
          ) {
            Icon(
              painterResource(R.drawable.ic_fullscreen),
              contentDescription = null,
              modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.full_screen))
          }
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
    playerFullscreenView?.let { fullView ->
      AndroidView(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim),
        factory = { context -> FrameLayout(context) },
        update = { frame ->
          if (fullView.parent !== frame) {
            (fullView.parent as? ViewGroup)?.removeView(fullView)
            frame.removeAllViews()
            frame.addView(
              fullView,
              FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
              ),
            )
          }
        },
      )
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
