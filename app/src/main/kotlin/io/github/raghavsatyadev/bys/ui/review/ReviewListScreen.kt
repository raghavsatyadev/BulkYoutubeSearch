@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.review

import android.net.Uri
import android.provider.DocumentsContract
import android.text.Html
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.ui.components.AppTopBar
import io.github.raghavsatyadev.bys.ui.components.EmptyState
import io.github.raghavsatyadev.bys.ui.components.StatusPill
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.shareFile
import io.github.raghavsatyadev.support.models.db.song_review.ReviewVerdict

/** Where the folder picker opens first: the folder the videos were copied to. */
private val INITIAL_FOLDER: Uri =
  DocumentsContract.buildDocumentUri(
    "com.android.externalstorage.documents",
    "primary:Movies/BYS",
  )

@Composable
fun ReviewListScreen(viewModel: ReviewViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
  val context = LocalContext.current
  val items by viewModel.items.collectAsStateWithLifecycle()
  val visible by viewModel.visible.collectAsStateWithLifecycle()
  val filter by viewModel.filter.collectAsStateWithLifecycle()
  val folder by viewModel.folderUri.collectAsStateWithLifecycle()
  val scanning by viewModel.scanning.collectAsStateWithLifecycle()
  val backupBlocked by viewModel.backupBlocked.collectAsStateWithLifecycle()
  val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val pickFolder =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
      uri?.let(viewModel::onFolderPicked)
    }
  // Videos may still be arriving on the phone, so each visit looks again.
  LaunchedEffect(Unit) { viewModel.refresh() }
  val all = items.orEmpty()
  val reviewed = remember(all) { all.reviewedCount() }

  Scaffold(
    modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
    topBar = {
      AppTopBar(
        title = stringResource(R.string.review_songs),
        subtitle = stringResource(R.string.reviewed_of, reviewed, all.size),
        scrollBehavior = scroll,
        onBack = onBack,
        actions = {
          IconButton(
            onClick = { pickFolder.launch(INITIAL_FOLDER) },
            shapes = IconButtonDefaults.shapes(),
          ) {
            Icon(
              painterResource(R.drawable.ic_folder),
              contentDescription = stringResource(R.string.review_change_folder),
            )
          }
          IconButton(
            onClick = {
              viewModel.backupNow { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
              }
            },
            shapes = IconButtonDefaults.shapes(),
          ) {
            Icon(
              painterResource(R.drawable.ic_backup),
              contentDescription = stringResource(R.string.review_backup_now),
            )
          }
          IconButton(
            onClick = { viewModel.exportReviews { file -> context.shareFile(file) } },
            shapes = IconButtonDefaults.shapes(),
          ) {
            Icon(
              painterResource(R.drawable.ic_share),
              contentDescription = stringResource(R.string.review_export),
            )
          }
        },
      )
    },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      when {
        folder == "" -> PickFolderCard { pickFolder.launch(INITIAL_FOLDER) }
        scanning -> ScanningRow()
        folder != null -> {
          val withLocal = remember(all) { all.count { it.local != null } }
          Text(
            stringResource(R.string.review_local_found, withLocal, all.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
        }
      }
      if (backupBlocked && !folder.isNullOrEmpty()) {
        Text(
          stringResource(R.string.review_backup_blocked),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.error,
          modifier =
            Modifier.clickable { pickFolder.launch(INITIAL_FOLDER) }
              .padding(horizontal = 16.dp, vertical = 4.dp),
        )
      }
      FilterRow(selected = filter, onSelect = viewModel::setFilter)
      val shown = visible
      when {
        shown == null -> Unit
        shown.isEmpty() ->
          EmptyState(
            painter = painterResource(R.drawable.ic_compare),
            title = stringResource(R.string.review_empty_title),
            hint = stringResource(R.string.review_empty_hint),
            modifier = Modifier.fillMaxSize(),
          )
        else -> {
          val listState = rememberLazyListState()
          LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(shown, key = { it.key }) { item ->
              ReviewRow(
                item = item,
                onClick = {
                  viewModel.startQueue(item.key)
                  onOpen(item.key)
                },
                modifier = Modifier.animateItem(),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PickFolderCard(onPick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    shape = MaterialTheme.shapes.extraLarge,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        stringResource(R.string.review_pick_folder_title),
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        stringResource(R.string.review_pick_folder_hint),
        style = MaterialTheme.typography.bodyMedium,
      )
      Button(onClick = onPick, shapes = ButtonDefaults.shapes()) {
        Icon(
          painterResource(R.drawable.ic_folder),
          contentDescription = null,
          modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Text(
          stringResource(R.string.review_pick_folder),
          modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
        )
      }
    }
  }
}

@Composable
private fun ScanningRow() {
  Row(
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    LoadingIndicator(modifier = Modifier.size(24.dp))
    Text(stringResource(R.string.review_scanning), style = MaterialTheme.typography.labelMedium)
  }
}

@Composable
private fun FilterRow(selected: ReviewFilter, onSelect: (ReviewFilter) -> Unit) {
  LazyRow(
    contentPadding = PaddingValues(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    items(ReviewFilter.entries.toList(), key = { it.name }) { filter ->
      FilterChip(
        selected = filter == selected,
        onClick = { onSelect(filter) },
        label = { Text(filter.label()) },
      )
    }
  }
}

@Composable
private fun ReviewFilter.label(): String =
  stringResource(
    when (this) {
      ReviewFilter.ALL -> R.string.review_filter_all
      ReviewFilter.NOT_REVIEWED -> R.string.review_filter_not_reviewed
      ReviewFilter.REVIEWED -> R.string.review_filter_reviewed
      ReviewFilter.MATCH -> R.string.review_filter_match
      ReviewFilter.REJECTED -> R.string.review_filter_rejected
      ReviewFilter.MISMATCH -> R.string.review_filter_mismatch
    }
  )

internal fun htmlText(text: String): String =
  Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT).toString()

@Composable
private fun ReviewRow(item: ReviewItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val song = item.song
  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      AsyncImage(
        model = song.thumbnailLow.ifEmpty { song.thumbnailHigh },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.width(96.dp).aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium),
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          item.key,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          remember(song.title) { htmlText(song.title) },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          VerdictPill(item.review?.verdict)
          if (item.local == null) {
            StatusPill(
              text = stringResource(R.string.review_no_local),
              container = MaterialTheme.colorScheme.surfaceContainerHighest,
              content = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
internal fun VerdictPill(verdict: ReviewVerdict?) {
  val colors = MaterialTheme.colorScheme
  when (verdict) {
    ReviewVerdict.MATCH ->
      StatusPill(
        stringResource(R.string.review_filter_match),
        colors.primaryContainer,
        colors.onPrimaryContainer,
      )
    ReviewVerdict.REJECTED ->
      StatusPill(
        stringResource(R.string.review_filter_rejected),
        colors.tertiaryContainer,
        colors.onTertiaryContainer,
      )
    ReviewVerdict.MISMATCH ->
      StatusPill(
        stringResource(R.string.review_filter_mismatch),
        colors.errorContainer,
        colors.onErrorContainer,
      )
    null ->
      StatusPill(
        stringResource(R.string.review_filter_not_reviewed),
        colors.surfaceContainerHighest,
        colors.onSurfaceVariant,
      )
  }
}
