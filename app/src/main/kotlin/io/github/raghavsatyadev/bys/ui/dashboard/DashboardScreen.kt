@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.ui.AppViewModel
import io.github.raghavsatyadev.bys.ui.components.AdBanner
import io.github.raghavsatyadev.bys.ui.components.AppTopBar
import io.github.raghavsatyadev.bys.ui.components.ShapeIcon
import io.github.raghavsatyadev.bys.ui.components.StatusPill
import io.github.raghavsatyadev.bys.ui.theme.AppMotion
import io.github.raghavsatyadev.bys.ui.theme.ENTRANCE_DURATION_MILLIS
import io.github.raghavsatyadev.bys.ui.theme.StaggeredEntrance
import io.github.raghavsatyadev.bys.ui.theme.staggerDelayMillis
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
  viewModel: AppViewModel,
  onSearch: () -> Unit,
  onFoundSongs: () -> Unit,
  onManageKeys: () -> Unit,
) {
  val found by viewModel.foundCount.collectAsStateWithLifecycle()
  val remaining by viewModel.remainingCount.collectAsStateWithLifecycle()
  val keys by viewModel.keys.collectAsStateWithLifecycle()
  val oneTime by viewModel.oneTimeRunning.collectAsStateWithLifecycle()
  val periodic by viewModel.periodicRunning.collectAsStateWithLifecycle()
  val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  // The cards cascade in on the first open only, never on the way back.
  var entranceDone by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay((ENTRANCE_DURATION_MILLIS + staggerDelayMillis(3)).toLong())
    entranceDone = true
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
    topBar = {
      AppTopBar(
        title = stringResource(R.string.app_title),
        subtitle = stringResource(R.string.app_subtitle),
        scrollBehavior = scroll,
      )
    },
    bottomBar = { AdBanner() },
  ) { padding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      StaggeredEntrance(index = 0, play = !entranceDone) {
        SummaryCard(found = found, remaining = remaining, searching = oneTime || periodic)
      }
      StaggeredEntrance(index = 1, play = !entranceDone) {
        ActionCard(
          title = stringResource(R.string.search_songs),
          body = stringResource(R.string.search_songs_hint),
          painter = painterResource(R.drawable.ic_search),
          container = MaterialTheme.colorScheme.primaryContainer,
          content = MaterialTheme.colorScheme.onPrimaryContainer,
          shape = MaterialShapes.Cookie9Sided,
          onClick = onSearch,
        )
      }
      StaggeredEntrance(index = 2, play = !entranceDone) {
        ActionCard(
          title = stringResource(R.string.found_songs),
          body = pluralString(R.plurals.songs_count, found.toInt()),
          painter = painterResource(R.drawable.ic_library_music),
          container = MaterialTheme.colorScheme.tertiaryContainer,
          content = MaterialTheme.colorScheme.onTertiaryContainer,
          shape = MaterialShapes.Clover4Leaf,
          onClick = onFoundSongs,
        )
      }
      StaggeredEntrance(index = 3, play = !entranceDone) {
        ActionCard(
          title = stringResource(R.string.manage_keys),
          body = pluralString(R.plurals.keys_count, keys?.size ?: 0),
          painter = painterResource(R.drawable.ic_key),
          container = MaterialTheme.colorScheme.secondaryContainer,
          content = MaterialTheme.colorScheme.onSecondaryContainer,
          shape = MaterialShapes.Gem,
          onClick = onManageKeys,
        )
      }
    }
  }
}

@Composable
private fun pluralString(id: Int, count: Int): String =
  androidx.compose.ui.res.pluralStringResource(id, count, count)

/** The one hero block: how many songs are found, how many remain, and whether a search runs. */
@Composable
private fun SummaryCard(found: Long, remaining: Long, searching: Boolean) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.extraLarge,
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          stringResource(R.string.library),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f),
        )
        if (searching) {
          LoadingIndicator(modifier = Modifier.size(32.dp))
          StatusPill(
            text = stringResource(R.string.searching_now),
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Count(value = found, label = stringResource(R.string.found))
        Count(value = remaining, label = stringResource(R.string.remaining))
      }
    }
  }
}

@Composable
private fun Count(value: Long, label: String) {
  val fade = AppMotion.dataSpec<Float>()
  Column {
    // A count is data: it fades between values and never overshoots.
    AnimatedContent(
      targetState = value,
      transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
      label = label,
    ) {
      Text(it.toString(), style = MaterialTheme.typography.displayMedium)
    }
    Text(
      label,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ActionCard(
  title: String,
  body: String,
  painter: Painter,
  container: Color,
  content: Color,
  shape: RoundedPolygon,
  onClick: () -> Unit,
) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
      modifier = Modifier.padding(20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      ShapeIcon(painter = painter, container = container, content = content, shape = shape)
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
          body,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
