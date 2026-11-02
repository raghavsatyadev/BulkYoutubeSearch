@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.found_songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.bys.R

/**
 * The found songs' whole header in two slim rows: back, search and sort, then the artist filter. It
 * stands in for a top app bar, so the list gets the room.
 */
@Composable
internal fun FoundSongsHeader(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearchFocus: (Boolean) -> Unit,
  shownCount: Int,
  onBack: () -> Unit,
  sort: SongSort,
  ascending: Boolean,
  onSort: (SongSort) -> Unit,
  onDirection: (Boolean) -> Unit,
  showArtistFilter: Boolean,
  selectedArtists: Set<String>,
  onOpenArtists: () -> Unit,
  onRemoveArtist: (String) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().height(48.dp).padding(end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
        Icon(
          painter = painterResource(io.github.raghavsatyadev.support.R.drawable.ic_back),
          contentDescription = stringResource(R.string.navigate_back),
        )
      }
      CompactSearchField(
        query = query,
        onQueryChange = onQueryChange,
        onFocusChange = onSearchFocus,
        shownCount = shownCount,
        modifier = Modifier.weight(1f),
      )
      SortMenu(sort = sort, ascending = ascending, onSort = onSort, onDirection = onDirection)
    }
    if (showArtistFilter) {
      ArtistFilterRow(
        selected = selectedArtists,
        onOpen = onOpenArtists,
        onRemove = onRemoveArtist,
      )
    }
  }
}

/** A 40 dp pill search; its placeholder carries the song count the old top bar showed. */
@Composable
private fun CompactSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  onFocusChange: (Boolean) -> Unit,
  shownCount: Int,
  modifier: Modifier = Modifier,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  val colors = MaterialTheme.colorScheme
  val textStyle = MaterialTheme.typography.bodyMedium
  BasicTextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.height(40.dp).onFocusChanged { onFocusChange(it.isFocused) },
    singleLine = true,
    textStyle = textStyle.copy(color = colors.onSurface),
    cursorBrush = SolidColor(colors.primary),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
    decorationBox = { field ->
      Row(
        modifier =
          Modifier.fillMaxSize()
            .clip(CircleShape)
            .background(colors.surfaceContainerHigh)
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          painterResource(R.drawable.ic_search),
          contentDescription = null,
          tint = colors.onSurfaceVariant,
          modifier = Modifier.size(18.dp),
        )
        Box(Modifier.weight(1f).padding(horizontal = 8.dp)) {
          if (query.isEmpty()) {
            Text(
              pluralStringResource(R.plurals.search_songs_count, shownCount, shownCount),
              style = textStyle,
              color = colors.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          field()
        }
        if (query.isNotEmpty()) {
          Text(
            pluralStringResource(R.plurals.songs_count, shownCount, shownCount),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
          )
          IconButton(
            onClick = { onQueryChange("") },
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.size(32.dp),
          ) {
            Icon(
              painterResource(R.drawable.ic_close),
              contentDescription = stringResource(R.string.clear_search),
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    },
  )
}

/**
 * The artist filter button, then one chip per picked artist; tapping a chip drops that artist. The
 * button opens [ArtistFilterSheet], where artists are searched and picked.
 */
@Composable
private fun ArtistFilterRow(
  selected: Set<String>,
  onOpen: () -> Unit,
  onRemove: (String) -> Unit,
) {
  val picked = remember(selected) { selected.sortedBy { it.lowercase() } }
  LazyRow(
    contentPadding = PaddingValues(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    item(key = "open") {
      FilterChip(
        selected = selected.isNotEmpty(),
        onClick = onOpen,
        label = {
          Text(
            if (selected.isEmpty()) stringResource(R.string.artists_filter)
            else stringResource(R.string.artists_filter_selected, selected.size)
          )
        },
        trailingIcon = {
          Icon(
            painterResource(R.drawable.ic_arrow_drop_down),
            contentDescription = null,
            modifier = Modifier.size(FilterChipDefaults.IconSize),
          )
        },
      )
    }
    items(picked, key = { it }) { artist ->
      val remove = stringResource(R.string.remove_artist_filter, artist)
      InputChip(
        selected = true,
        onClick = { onRemove(artist) },
        label = { Text(artist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingIcon = {
          Icon(
            painterResource(R.drawable.ic_close),
            contentDescription = remove,
            modifier = Modifier.size(InputChipDefaults.IconSize),
          )
        },
        modifier = Modifier.widthIn(max = 200.dp).animateItem(),
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
