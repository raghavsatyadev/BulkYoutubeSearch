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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.saveable.listSaver
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What the found songs are ordered by. Songs without a known release date always sort last. */
enum class SongSort {
  Title,
  Artist,
  Release,
}

/** A song with its HTML-decoded text, so filtering and sorting never decode per comparison. */
private data class SongRow(val song: SongDetail, val title: String, val artist: String)

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
  val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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

  Scaffold(
    modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
    topBar = {
      AppTopBar(
        title = stringResource(R.string.found_songs),
        subtitle = shown?.let { pluralStringResource(R.plurals.songs_count, it.size, it.size) },
        scrollBehavior = scroll,
        onBack = onBack,
        actions = {
          if (!songs.isNullOrEmpty()) {
            SortMenu(
              sort = sort,
              ascending = ascending,
              onSort = { sort = it },
              onDirection = { ascending = it },
            )
          }
        },
      )
    },
    snackbarHost = { SnackbarHost(snackbar) },
    bottomBar = { AdBanner() },
  ) { padding ->
    val list = shown
    when {
      list == null -> DelayedLoading(Modifier.fillMaxSize().padding(padding))
      songs.isNullOrEmpty() ->
        EmptyState(
          painter = painterResource(R.drawable.ic_library_music),
          title = stringResource(R.string.no_songs_title),
          hint = stringResource(R.string.no_songs_hint),
          modifier = Modifier.padding(padding),
        )
      else ->
        Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
          SongSearchField(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
          )
          ArtistChips(
            artists = artists,
            selected = selectedArtists,
            onToggle = { artist ->
              selectedArtists =
                if (artist in selectedArtists) selectedArtists - artist
                else selectedArtists + artist
            },
            onClear = { selectedArtists = emptySet() },
          )
          if (list.isEmpty()) {
            EmptyState(
              painter = painterResource(R.drawable.ic_search),
              title = stringResource(R.string.no_matches_title),
              hint = stringResource(R.string.no_matches_hint),
            )
          } else {
            LazyColumn(
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

@Composable
private fun SongSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier,
    singleLine = true,
    shape = CircleShape,
    placeholder = { Text(stringResource(R.string.filter_songs_hint)) },
    leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
    trailingIcon = {
      if (query.isNotEmpty()) {
        IconButton(onClick = { onQueryChange("") }, shapes = IconButtonDefaults.shapes()) {
          Icon(
            painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.clear_search),
          )
        }
      }
    },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
    colors =
      OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      ),
  )
}

/** Multi-select artist filter: chosen artists first, then the rest by how many songs they have. */
@Composable
private fun ArtistChips(
  artists: List<String>,
  selected: Set<String>,
  onToggle: (String) -> Unit,
  onClear: () -> Unit,
) {
  val ordered =
    remember(artists, selected) {
      artists.filter { it in selected } + artists.filterNot { it in selected }
    }
  LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (selected.isNotEmpty()) {
      item(key = "clear") {
        InputChip(
          selected = true,
          onClick = onClear,
          label = {
            Text(pluralStringResource(R.plurals.artist_filter_count, selected.size, selected.size))
          },
          trailingIcon = {
            Icon(
              painterResource(R.drawable.ic_close),
              contentDescription = stringResource(R.string.clear_filters),
              modifier = Modifier.size(InputChipDefaults.IconSize),
            )
          },
          modifier = Modifier.animateItem(),
        )
      }
    }
    items(ordered, key = { it }) { artist ->
      val isSelected = artist in selected
      FilterChip(
        selected = isSelected,
        onClick = { onToggle(artist) },
        label = { Text(artist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon =
          if (isSelected) {
            {
              Icon(
                painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize),
              )
            }
          } else {
            null
          },
        modifier = Modifier.widthIn(max = 220.dp).animateItem(),
      )
    }
  }
}

@Composable
private fun SortMenu(
  sort: SongSort,
  ascending: Boolean,
  onSort: (SongSort) -> Unit,
  onDirection: (Boolean) -> Unit,
) {
  var open by remember { mutableStateOf(false) }
  Box {
    FilledTonalIconButton(onClick = { open = true }, shapes = IconButtonDefaults.shapes()) {
      Icon(
        painterResource(R.drawable.ic_sort),
        contentDescription = stringResource(R.string.sort_songs),
      )
    }
    DropdownMenu(
      expanded = open,
      onDismissRequest = { open = false },
      shape = MaterialTheme.shapes.large,
    ) {
      SongSort.entries.forEach { option ->
        val label =
          when (option) {
            SongSort.Title -> R.string.sort_by_song
            SongSort.Artist -> R.string.sort_by_artist
            SongSort.Release -> R.string.sort_by_release
          }
        DropdownMenuItem(
          text = { Text(stringResource(label)) },
          onClick = {
            onSort(option)
            open = false
          },
          leadingIcon = { CheckSlot(option == sort) },
        )
      }
      HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
      listOf(true to R.string.sort_ascending, false to R.string.sort_descending).forEach {
        (up, label) ->
        DropdownMenuItem(
          text = { Text(stringResource(label)) },
          onClick = {
            onDirection(up)
            open = false
          },
          leadingIcon = { CheckSlot(up == ascending) },
          trailingIcon = {
            Icon(
              painterResource(if (up) R.drawable.ic_arrow_upward else R.drawable.ic_arrow_downward),
              contentDescription = null,
            )
          },
        )
      }
    }
  }
}

/** A check when chosen, and the same width when not, so the labels line up. */
@Composable
private fun CheckSlot(checked: Boolean) {
  Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
    if (checked) {
      Icon(
        painterResource(R.drawable.ic_check),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

private fun String.fromHtml(): String = Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()

private val ReleaseDateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** "12 Mar 2019" from YouTube's ISO-8601 time, or null when it is missing or unreadable. */
private fun formatReleaseDate(publishedAt: String): String? = runCatching {
  OffsetDateTime.parse(publishedAt).toLocalDate().format(ReleaseDateFormat)
}
  .getOrNull()

@Composable
private fun SongCard(row: SongRow, onClick: () -> Unit, onDelete: () -> Unit) {
  val song = row.song
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
          row.title,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          row.artist,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        val released = remember(song.publishedAt) { formatReleaseDate(song.publishedAt) }
        if (released != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              painterResource(R.drawable.ic_event),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(14.dp),
            )
            Text(
              stringResource(R.string.released_on, released),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.tertiary,
            )
          }
        }
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
