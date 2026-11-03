@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.review

import android.net.Uri
import androidx.annotation.OptIn as AndroidXOptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.indicator.DurationText
import androidx.media3.ui.compose.material3.indicator.PositionText
import androidx.media3.ui.compose.material3.indicator.ProgressSlider
import io.github.raghavsatyadev.bys.R
import kotlinx.coroutines.delay

private const val CONTROLS_TIMEOUT_MILLIS = 3_000L

/**
 * The local file, muted by default so it does not talk over the YouTube video. Its control bar
 * hides after a few seconds of playing; a tap on the video shows or hides it.
 */
@AndroidXOptIn(UnstableApi::class)
@Composable
internal fun LocalVideo(
  exo: ExoPlayer,
  uri: Uri,
  muted: Boolean,
  onToggleMute: () -> Unit,
  playing: Boolean,
  modifier: Modifier,
) {
  LaunchedEffect(uri) {
    exo.setMediaItem(MediaItem.fromUri(uri))
    exo.prepare()
    exo.playWhenReady = true
  }
  LaunchedEffect(muted) { exo.volume = if (muted) 0f else 1f }

  var showControls by rememberSaveable { mutableStateOf(true) }
  val sliderInteraction = remember { MutableInteractionSource() }
  val dragging by sliderInteraction.collectIsDraggedAsState()
  val pressing by sliderInteraction.collectIsPressedAsState()
  // Hide only while the video plays and nobody is touching the seek bar.
  LaunchedEffect(showControls, playing, dragging, pressing) {
    if (showControls && playing && !dragging && !pressing) {
      delay(CONTROLS_TIMEOUT_MILLIS)
      showControls = false
    }
  }

  Box(
    modifier =
      modifier.background(Color.Black).clickable(interactionSource = null, indication = null) {
        showControls = !showControls
      }
  ) {
    ContentFrame(
      player = exo,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Fit,
    )
    AnimatedVisibility(
      visible = showControls,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      ControlBar(exo, sliderInteraction, muted, onToggleMute)
    }
  }
}

@AndroidXOptIn(UnstableApi::class)
@Composable
private fun ControlBar(
  exo: ExoPlayer,
  sliderInteraction: MutableInteractionSource,
  muted: Boolean,
  onToggleMute: () -> Unit,
) {
  val white = Color.White
  Row(
    modifier =
      Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    PlayPauseButton(player = exo, tint = white)
    PositionText(player = exo, color = white)
    ProgressSlider(
      player = exo,
      modifier = Modifier.weight(1f),
      colors =
        SliderDefaults.colors(
          thumbColor = MaterialTheme.colorScheme.primary,
          activeTrackColor = MaterialTheme.colorScheme.primary,
          inactiveTrackColor = white.copy(alpha = 0.3f),
        ),
      interactionSource = sliderInteraction,
    )
    DurationText(player = exo, color = white)
    IconButton(onClick = onToggleMute, shapes = IconButtonDefaults.shapes()) {
      Icon(
        painterResource(if (muted) R.drawable.ic_volume_off else R.drawable.ic_volume_up),
        contentDescription =
          stringResource(if (muted) R.string.review_unmute_local else R.string.review_mute_local),
        tint = white,
      )
    }
  }
}
