@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.found_songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.bys.R
import kotlinx.coroutines.launch

/** An artist and how many of the found songs are theirs. */
internal data class ArtistCount(val name: String, val songs: Int)

/**
 * Pick any number of artists. Each tick filters the list behind the sheet at once; Done only closes
 * the sheet. The list keeps its most-songs-first order, so a row never moves under the finger.
 */
@Composable
internal fun ArtistFilterSheet(
  artists: List<ArtistCount>,
  selected: Set<String>,
  onToggle: (String) -> Unit,
  onClear: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  val keyboard = LocalSoftwareKeyboardController.current
  val close: () -> Unit = {
    keyboard?.hide()
    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
  }
  var search by rememberSaveable { mutableStateOf("") }
  val matches =
    remember(artists, search) {
      val needle = search.trim()
      if (needle.isEmpty()) artists else artists.filter { it.name.contains(needle, true) }
    }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(Modifier.fillMaxWidth().imePadding()) {
      Text(
        stringResource(R.string.filter_by_artist),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 24.dp),
      )
      ArtistSearchField(
        value = search,
        onValueChange = { search = it },
        onSearch = { keyboard?.hide() },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      )
      if (matches.isEmpty()) {
        Text(
          stringResource(R.string.no_artists_match),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
      }
      LazyColumn(
        modifier = Modifier.weight(1f, fill = false),
        contentPadding = PaddingValues(horizontal = 8.dp),
      ) {
        items(matches, key = { it.name }) { artist ->
          ArtistRow(
            artist = artist,
            checked = artist.name in selected,
            onToggle = { onToggle(artist.name) },
          )
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(
          onClick = onClear,
          enabled = selected.isNotEmpty(),
          shapes = ButtonDefaults.shapes(),
        ) {
          Text(stringResource(R.string.clear_all))
        }
        Button(onClick = close, shapes = ButtonDefaults.shapes()) {
          Text(stringResource(R.string.done))
        }
      }
    }
  }
}

@Composable
private fun ArtistSearchField(
  value: String,
  onValueChange: (String) -> Unit,
  onSearch: () -> Unit,
  modifier: Modifier = Modifier,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = true,
    shape = MaterialTheme.shapes.extraLarge,
    placeholder = { Text(stringResource(R.string.search_artists)) },
    leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
    trailingIcon =
      if (value.isNotEmpty()) {
        {
          IconButton(onClick = { onValueChange("") }, shapes = IconButtonDefaults.shapes()) {
            Icon(
              painterResource(R.drawable.ic_close),
              contentDescription = stringResource(R.string.clear_search),
            )
          }
        }
      } else {
        null
      },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
  )
}

@Composable
private fun ArtistRow(artist: ArtistCount, checked: Boolean, onToggle: () -> Unit) {
  ListItem(
    headlineContent = { Text(artist.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    supportingContent = {
      Text(pluralStringResource(R.plurals.songs_count, artist.songs, artist.songs))
    },
    // The whole row toggles; the box only shows the state.
    leadingContent = { Checkbox(checked = checked, onCheckedChange = null) },
    // Transparent, so the rows sit on the sheet's own colour.
    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    modifier =
      Modifier.toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
  )
}
