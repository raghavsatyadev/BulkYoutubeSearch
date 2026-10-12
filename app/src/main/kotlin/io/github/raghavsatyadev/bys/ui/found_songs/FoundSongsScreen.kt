@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.found_songs

import android.content.Context
import android.text.Html
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.ui.AppViewModel
import io.github.raghavsatyadev.bys.ui.components.AdBanner
import io.github.raghavsatyadev.bys.ui.components.AppTopBar
import io.github.raghavsatyadev.bys.ui.components.EmptyState
import io.github.raghavsatyadev.bys.ui.theme.ENTRANCE_DURATION_MILLIS
import io.github.raghavsatyadev.bys.ui.theme.StaggeredEntrance
import io.github.raghavsatyadev.bys.ui.theme.staggerDelayMillis
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FoundSongsScreen(
  viewModel: AppViewModel,
  onBack: () -> Unit,
  onPlay: (SongDetail, Context) -> Unit,
) {
  val songs by viewModel.foundSongs.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val snackbar = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val removed = stringResource(R.string.song_removed)
  val undo = stringResource(R.string.undo)

  var entranceDone by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(songs != null) {
    if (songs != null && !entranceDone) {
      delay((ENTRANCE_DURATION_MILLIS + staggerDelayMillis(songs!!.size)).toLong())
      entranceDone = true
    }
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
    topBar = {
      AppTopBar(
        title = stringResource(R.string.found_songs),
        subtitle = songs?.let { pluralStringResource(R.plurals.songs_count, it.size, it.size) },
        scrollBehavior = scroll,
        onBack = onBack,
      )
    },
    snackbarHost = { SnackbarHost(snackbar) },
    bottomBar = { AdBanner() },
  ) { padding ->
    val list = songs
    when {
      list == null -> DelayedLoading(Modifier.fillMaxSize().padding(padding))
      list.isEmpty() ->
        EmptyState(
          painter = painterResource(R.drawable.ic_library_music),
          title = stringResource(R.string.no_songs_title),
          hint = stringResource(R.string.no_songs_hint),
          modifier = Modifier.padding(padding),
        )
      else ->
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding =
            PaddingValues(
              start = 16.dp,
              end = 16.dp,
              top = padding.calculateTopPadding() + 8.dp,
              bottom = padding.calculateBottomPadding() + 16.dp,
            ),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(list, key = { it.id }) { song ->
            StaggeredEntrance(
              index = list.indexOf(song),
              play = !entranceDone,
              modifier = Modifier.animateItem(),
            ) {
              SongCard(
                song = song,
                onClick = { onPlay(song, context) },
                onDelete = {
                  // Removing is a correction: a plain reject buzz and an undo, no flourish.
                  haptic.performHapticFeedback(HapticFeedbackType.Reject)
                  viewModel.deleteSong(song)
                  scope.launch {
                    if (
                      snackbar.showSnackbar(removed, undo, withDismissAction = true) ==
                        SnackbarResult.ActionPerformed
                    ) {
                      viewModel.restoreSong(song)
                    }
                  }
                },
              )
            }
          }
        }
    }
  }
}

private fun String.fromHtml(): String = Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()

@Composable
private fun SongCard(song: SongDetail, onClick: () -> Unit, onDelete: () -> Unit) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      AsyncImage(
        model = song.thumbnailHigh.ifEmpty { song.thumbnailLow },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.width(120.dp).aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium),
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          song.title.fromHtml(),
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          song.artist.fromHtml(),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      FilledTonalIconButton(
        onClick = onDelete,
        shapes = IconButtonDefaults.shapes(),
        colors =
          IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
          ),
      ) {
        Icon(
          painterResource(R.drawable.ic_delete),
          contentDescription = stringResource(R.string.delete_song),
        )
      }
    }
  }
}

/** Shows a loading indicator only if the wait passes ~400 ms, so a fast load never flashes. */
@Composable
fun DelayedLoading(modifier: Modifier = Modifier) {
  var show by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(400)
    show = true
  }
  Box(modifier = modifier, contentAlignment = Alignment.Center) { if (show) LoadingIndicator() }
}
