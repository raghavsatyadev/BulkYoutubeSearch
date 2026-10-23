@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.found_songs

import android.content.Context
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.ui.AppViewModel
import io.github.raghavsatyadev.bys.ui.components.AdBanner
import io.github.raghavsatyadev.bys.ui.components.AppTopBar
import io.github.raghavsatyadev.bys.ui.components.EmptyState
import io.github.raghavsatyadev.bys.ui.components.ScrollBars
import io.github.raghavsatyadev.bys.ui.theme.ENTRANCE_DURATION_MILLIS
import io.github.raghavsatyadev.bys.ui.theme.StaggeredEntrance
import io.github.raghavsatyadev.bys.ui.theme.animationsEnabled
import io.github.raghavsatyadev.bys.ui.theme.staggerDelayMillis
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What the found songs are ordered by. Songs without a known release date always sort last. */
enum class SongSort {
  Title,
  Artist,
  Release,
}

/** A song with its HTML-decoded text, so filtering and sorting never decode per comparison. */
internal data class SongRow(val song: SongDetail, val title: String, val artist: String)

private val ArtistSetSaver =
  listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() })

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
  val removed = stringResource(R.string.song_removed)
  val undo = stringResource(R.string.undo)

  LaunchedEffect(Unit) { viewModel.backfillReleaseDates() }

  // Every choice survives rotation.
  var query by rememberSaveable { mutableStateOf("") }
  var selectedArtists by
    rememberSaveable(stateSaver = ArtistSetSaver) { mutableStateOf(emptySet<String>()) }
  var sort by rememberSaveable { mutableStateOf(SongSort.Title) }
  var ascending by rememberSaveable { mutableStateOf(true) }

  val rows =
    remember(songs) { songs?.map { SongRow(it, it.title.fromHtml(), it.artist.fromHtml()) } }
  // Artists with the most songs first, so the useful chips are in reach.
  val artists =
    remember(rows) {
      rows
        .orEmpty()
        .groupingBy { it.artist }
        .eachCount()
        .entries
        .sortedWith(
          compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase() }
        )
        .map { it.key }
    }
  val shown =
    remember(rows, query, selectedArtists, sort, ascending) {
      rows?.let { filterAndSort(it, query, selectedArtists, sort, ascending) }
    }

  var entranceDone by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(songs != null) {
    if (songs != null && !entranceDone) {
      delay((ENTRANCE_DURATION_MILLIS + staggerDelayMillis(songs!!.size)).toLong())
      entranceDone = true
    }
  }

  // Scrolling down hides the header (there is no top app bar); scrolling up shows it.
  val listState = rememberLazyListState()
  val bars = remember { ScrollBars() }
  var searchFocused by remember { mutableStateOf(false) }
  val atTop by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
    }
  }
  // Kept while typing a search, and at the top of the list, where there is nothing to make room
  // for.
  val showHeader = bars.visible || searchFocused || atTop
  val animate = animationsEnabled()
  val hasSongs = !songs.isNullOrEmpty()

  Scaffold(
    topBar = {
      // Nothing to search yet: a plain bar keeps the title and the way back.
      if (!hasSongs) {
        AppTopBar(
          title = stringResource(R.string.found_songs),
          scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
          onBack = onBack,
        )
      }
    },
    snackbarHost = { SnackbarHost(snackbar) },
    bottomBar = { AdBanner() },
  ) { padding ->
    val list = shown
    when {
      list == null -> DelayedLoading(Modifier.fillMaxSize().padding(padding))
      !hasSongs ->
        EmptyState(
          painter = painterResource(R.drawable.ic_library_music),
          title = stringResource(R.string.no_songs_title),
          hint = stringResource(R.string.no_songs_hint),
          modifier = Modifier.padding(padding),
        )
      else ->
        Column(
          modifier =
            Modifier.fillMaxSize()
              .padding(top = padding.calculateTopPadding())
              .nestedScroll(bars.connection)
        ) {
          AnimatedVisibility(
            visible = showHeader,
            enter = if (animate) expandVertically() + fadeIn() else EnterTransition.None,
            exit = if (animate) shrinkVertically() + fadeOut() else ExitTransition.None,
          ) {
            FoundSongsHeader(
              query = query,
              onQueryChange = { query = it },
              onSearchFocus = { searchFocused = it },
              shownCount = list.size,
              onBack = onBack,
              sort = sort,
              ascending = ascending,
              onSort = { sort = it },
              onDirection = { ascending = it },
              artists = artists,
              selectedArtists = selectedArtists,
              onToggleArtist = { artist ->
                selectedArtists =
                  if (artist in selectedArtists) selectedArtists - artist
                  else selectedArtists + artist
              },
              onClearArtists = { selectedArtists = emptySet() },
            )
          }
          if (list.isEmpty()) {
            EmptyState(
              painter = painterResource(R.drawable.ic_search),
              title = stringResource(R.string.no_matches_title),
              hint = stringResource(R.string.no_matches_hint),
            )
          } else {
            LazyColumn(
              state = listState,
              modifier = Modifier.fillMaxSize(),
              contentPadding =
                PaddingValues(
                  start = 16.dp,
                  end = 16.dp,
                  top = 8.dp,
                  bottom = padding.calculateBottomPadding() + 16.dp,
                ),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              itemsIndexed(list, key = { _, row -> row.song.id }) { index, row ->
                StaggeredEntrance(
                  index = index,
                  play = !entranceDone,
                  modifier = Modifier.animateItem(),
                ) {
                  SongCard(
                    row = row,
                    onClick = { onPlay(row.song, context) },
                    onDelete = {
                      // Removing is a correction: a plain reject buzz and an undo, no flourish.
                      haptic.performHapticFeedback(HapticFeedbackType.Reject)
                      viewModel.deleteSong(row.song)
                      scope.launch {
                        if (
                          snackbar.showSnackbar(removed, undo, withDismissAction = true) ==
                            SnackbarResult.ActionPerformed
                        ) {
                          viewModel.restoreSong(row.song)
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
  }
}

private fun filterAndSort(
  rows: List<SongRow>,
  query: String,
  artists: Set<String>,
  sort: SongSort,
  ascending: Boolean,
): List<SongRow> {
  val needle = query.trim()
  val filtered = rows.filter { row ->
    (artists.isEmpty() || row.artist in artists) &&
      (needle.isEmpty() ||
        row.title.contains(needle, ignoreCase = true) ||
        row.artist.contains(needle, ignoreCase = true))
  }
  val byKey: Comparator<SongRow> =
    when (sort) {
      SongSort.Title -> compareBy { it.title.lowercase() }
      SongSort.Artist ->
        compareBy<SongRow> { it.artist.lowercase() }.thenBy { it.title.lowercase() }
      SongSort.Release -> compareBy { it.song.publishedAt }
    }
  val directed = if (ascending) byKey else byKey.reversed()
  // Unknown release dates go last either way: they carry no order.
  return if (sort == SongSort.Release) {
    filtered.sortedWith(compareBy<SongRow> { it.song.publishedAt.isEmpty() }.then(directed))
  } else {
    filtered.sortedWith(directed)
  }
}

private fun String.fromHtml(): String = Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()

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
