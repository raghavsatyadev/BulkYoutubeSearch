@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.support.Constants.Other.YOUTUBE_LINK

/**
 * Compares one found song: the YouTube video on top, the local video under it, and the verdict
 * buttons in a bar below both. Previous and next step through the list the song was opened from.
 */
@Composable
fun ReviewPlayerScreen(viewModel: ReviewViewModel, startKey: String, onBack: () -> Unit) {
  val queue by viewModel.queue.collectAsStateWithLifecycle()
  val items by viewModel.items.collectAsStateWithLifecycle()
  val keys = queue.ifEmpty { listOf(startKey) }
  var index by rememberSaveable { mutableIntStateOf(keys.indexOf(startKey).coerceAtLeast(0)) }
  val key = keys.getOrElse(index) { startKey }
  val byKey = remember(items) { items.orEmpty().associateBy { it.key } }
  val item = byKey[key]
  var localMuted by rememberSaveable { mutableStateOf(true) }
  var youTube by remember { mutableStateOf<YouTubePlayer?>(null) }
  var youTubePlaying by remember { mutableStateOf(false) }
  val exo = rememberLocalPlayer()
  val localPlaying = rememberIsPlaying(exo)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            stringResource(R.string.review_position, index + 1, keys.size),
            style = MaterialTheme.typography.titleMedium,
          )
        },
        actions = { Box(Modifier.padding(end = 12.dp)) { VerdictPill(item?.review?.verdict) } },
        expandedHeight = 48.dp,
        navigationIcon = {
          IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
            Icon(
              painterResource(io.github.raghavsatyadev.support.R.drawable.ic_back),
              contentDescription = stringResource(R.string.navigate_back),
            )
          }
        },
      )
    },
    bottomBar = {
      ReviewControls(
        verdict = item?.review?.verdict,
        hasPrevious = index > 0,
        hasNext = index < keys.lastIndex,
        onPrevious = { if (index > 0) index-- },
        onNext = { if (index < keys.lastIndex) index++ },
        onMark = { verdict ->
          val current = item ?: return@ReviewControls
          viewModel.mark(current, verdict)
          // A new verdict moves on to the next song; tapping the same one only clears it.
          if (current.review?.verdict != verdict && index < keys.lastIndex) index++
        },
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      YouTubeVideo(
        videoId = item?.song?.link?.replace(YOUTUBE_LINK, "").orEmpty(),
        modifier = videoFrame(),
        onPlayer = { youTube = it },
        onPlayingChange = { youTubePlaying = it },
      )
      // One short line under each video, so the videos get the height.
      TitleLine(
        label = stringResource(R.string.review_youtube),
        title =
          item
            ?.let {
              remember(it.song.title, it.song.artist) {
                "${htmlText(it.song.title)} · ${htmlText(it.song.artist)}"
              }
            }
            .orEmpty(),
      ) {
        PlayBothButton(
          playing = youTubePlaying || localPlaying,
          onPlay = {
            youTube?.play()
            if (exo.mediaItemCount > 0) exo.play()
          },
          onPause = {
            youTube?.pause()
            exo.pause()
          },
        )
      }
      val local = item?.local
      if (local != null) {
        LocalVideo(
          exo = exo,
          uri = local.uri,
          muted = localMuted,
          onToggleMute = { localMuted = !localMuted },
          playing = localPlaying,
          modifier = videoFrame(),
        )
      } else {
        LaunchedEffect(key) { exo.clearMediaItems() }
        Box(
          modifier = videoFrame().background(MaterialTheme.colorScheme.surfaceContainerHighest),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            stringResource(R.string.review_local_missing, key),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
      TitleLine(
        label = stringResource(R.string.review_local),
        title = local?.path?.substringAfterLast('/')?.substringBeforeLast('.') ?: key,
      )
    }
  }
}

/** A small source label and a one-line title, with an optional button at the end. */
@Composable
private fun TitleLine(label: String, title: String, trailing: @Composable () -> Unit = {}) {
  Row(
    modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
    )
    Text(
      title,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    trailing()
  }
}

/** Full width: in portrait the screen width sets the largest a 16:9 video can be. */
private fun videoFrame(): Modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)

/** One YouTube view for the whole screen; a new [videoId] loads into it. */
@Composable
private fun YouTubeVideo(
  videoId: String,
  modifier: Modifier,
  onPlayer: (YouTubePlayer) -> Unit,
  onPlayingChange: (Boolean) -> Unit,
) {
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  var player by remember { mutableStateOf<YouTubePlayer?>(null) }
  AndroidView(
    modifier = modifier,
    factory = { context ->
      YouTubePlayerView(context).also { created ->
        lifecycle.addObserver(created)
        created.enableAutomaticInitialization = false
        created.initialize(
          object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
              player = youTubePlayer
              onPlayer(youTubePlayer)
            }

            override fun onStateChange(
              youTubePlayer: YouTubePlayer,
              state: PlayerConstants.PlayerState,
            ) {
              onPlayingChange(
                state == PlayerConstants.PlayerState.PLAYING ||
                  state == PlayerConstants.PlayerState.BUFFERING
              )
            }
          },
          IFramePlayerOptions.Builder(context).controls(1).fullscreen(0).build(),
        )
      }
    },
    // Released only when the view leaves the screen, never on a recomposition.
    onRelease = {
      lifecycle.removeObserver(it)
      it.release()
    },
  )
  LaunchedEffect(player, videoId) {
    if (videoId.isNotEmpty()) player?.loadVideo(videoId, 0f)
  }
}

/** The local player, owned by the screen so "play both" can reach it. */
@Composable
private fun rememberLocalPlayer(): ExoPlayer {
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  // No audio focus: the local player must not pause the YouTube video, and the reverse.
  val exo = remember {
    ExoPlayer.Builder(context).build().apply {
      setAudioAttributes(AudioAttributes.DEFAULT, false)
    }
  }
  DisposableEffect(exo, lifecycle) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE) exo.pause()
    }
    lifecycle.addObserver(observer)
    onDispose {
      lifecycle.removeObserver(observer)
      exo.release()
    }
  }
  return exo
}

@Composable
private fun rememberIsPlaying(exo: ExoPlayer): Boolean {
  var playing by remember { mutableStateOf(exo.isPlaying) }
  DisposableEffect(exo) {
    val listener =
      object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
          playing = isPlaying
        }
      }
    exo.addListener(listener)
    onDispose { exo.removeListener(listener) }
  }
  return playing
}

@Composable
private fun PlayBothButton(playing: Boolean, onPlay: () -> Unit, onPause: () -> Unit) {
  FilledTonalIconButton(
    onClick = if (playing) onPause else onPlay,
    shapes = IconButtonDefaults.shapes(),
  ) {
    Icon(
      painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play),
      contentDescription =
        stringResource(if (playing) R.string.review_pause_both else R.string.review_play_both),
    )
  }
}
