@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.search

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.bys.ui.AppViewModel
import io.github.raghavsatyadev.bys.ui.components.AdBanner
import io.github.raghavsatyadev.bys.ui.components.AppTopBar
import io.github.raghavsatyadev.bys.ui.components.ShapeIcon
import io.github.raghavsatyadev.bys.ui.components.StatusPill
import io.github.raghavsatyadev.bys.ui.theme.AppMotion
import io.github.raghavsatyadev.support.JsonFilePicker
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.shareFile
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.showInterstitialAd
import io.github.raghavsatyadev.support.getFileNameFromUri

@Composable
fun SearchScreen(viewModel: AppViewModel, onBack: () -> Unit, onManageKeys: () -> Unit) {
  val context = LocalContext.current
  val activity = context as? ComponentActivity
  val haptic = LocalHapticFeedback.current
  val selectedFile by viewModel.selectedFile.collectAsStateWithLifecycle()
  val remaining by viewModel.remainingCount.collectAsStateWithLifecycle()
  val keys by viewModel.keys.collectAsStateWithLifecycle()
  val oneTime by viewModel.oneTimeRunning.collectAsStateWithLifecycle()
  val periodic by viewModel.periodicRunning.collectAsStateWithLifecycle()
  var invalidFile by remember { mutableStateOf(false) }
  val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  val picker =
    rememberLauncherForActivityResult(JsonFilePicker()) { uri ->
      if (uri != null) {
        viewModel.onJsonPicked(uri, uri.getFileNameFromUri(context), context.contentResolver) {
          invalidFile = true
        }
      }
    }
  // Ads never hold an action back: with no ad ready the action runs at once.
  val withAd: (() -> Unit) -> Unit = { action ->
    activity?.showInterstitialAd(action) ?: action()
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
    topBar = { AppTopBar(stringResource(R.string.search_songs), scroll, onBack = onBack) },
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
      StepCard(
        step = 1,
        title = stringResource(R.string.step_pick_list),
        done = selectedFile != null,
      ) {
        Text(
          selectedFile ?: stringResource(R.string.select_file_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FilledTonalButton(
          onClick = { withAd { picker.launch(Unit) } },
          shapes = ButtonDefaults.shapes(),
        ) {
          Icon(
            painterResource(R.drawable.ic_upload),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Text(stringResource(R.string.select_file), modifier = Modifier.padding(start = 8.dp))
        }
      }

      StepCard(step = 2, title = stringResource(R.string.step_search), done = false) {
        val fade = AppMotion.dataSpec<Float>()
        AnimatedContent(
          remaining,
          transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
          label = "remaining",
        ) {
          Text(
            pluralStringResource(R.plurals.songs_remaining, it.toInt(), it.toInt()),
            style = MaterialTheme.typography.titleMedium,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          WorkStatus(running = oneTime, label = stringResource(R.string.search_now_status))
          WorkStatus(running = periodic, label = stringResource(R.string.daily_search_status))
        }
        Text(
          pluralStringResource(R.plurals.keys_count, keys?.size ?: 0, keys?.size ?: 0),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.Confirm)
              withAd { viewModel.searchVideos() }
            },
            enabled = selectedFile != null && !oneTime,
            shapes = ButtonDefaults.shapes(),
          ) {
            Icon(
              painterResource(R.drawable.ic_search),
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Text(stringResource(R.string.search), modifier = Modifier.padding(start = 8.dp))
          }
          OutlinedButton(onClick = onManageKeys, shapes = ButtonDefaults.shapes()) {
            Text(stringResource(R.string.manage_keys))
          }
        }
      }

      StepCard(step = 3, title = stringResource(R.string.step_share), done = false) {
        Text(
          stringResource(R.string.share_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FilledTonalButton(
          onClick = { withAd { viewModel.prepareSharingFile { file -> context.shareFile(file) } } },
          shapes = ButtonDefaults.shapes(),
        ) {
          Icon(
            painterResource(R.drawable.ic_share),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Text(
            stringResource(R.string.share_result_file),
            modifier = Modifier.padding(start = 8.dp),
          )
        }
      }
    }
  }

  if (invalidFile) {
    AlertDialog(
      onDismissRequest = { invalidFile = false },
      title = { Text(stringResource(R.string.invalid_file_title)) },
      text = {
        Text(stringResource(io.github.raghavsatyadev.support.R.string.warning_invalid_json_file))
      },
      confirmButton = {
        TextButton(onClick = { invalidFile = false }, shapes = ButtonDefaults.shapes()) {
          Text(stringResource(android.R.string.ok))
        }
      },
    )
  }
}

/** A numbered step; its badge turns into a tick once the step is done. */
@Composable
private fun StepCard(
  step: Int,
  title: String,
  done: Boolean,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        val fade = AppMotion.effectsSpec<Float>()
        AnimatedContent(
          done,
          transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
          label = "step",
        ) { isDone ->
          if (isDone) {
            ShapeIcon(
              painter = painterResource(R.drawable.ic_check),
              container = MaterialTheme.colorScheme.primary,
              content = MaterialTheme.colorScheme.onPrimary,
              shape = MaterialShapes.Cookie9Sided,
              size = 36.dp,
            )
          } else {
            StatusPill(
              text = step.toString(),
              container = MaterialTheme.colorScheme.secondaryContainer,
              content = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
      }
      content()
    }
  }
}

/** A background search, shown in place and quietly: a pill while it runs, nothing when it ends. */
@Composable
private fun WorkStatus(running: Boolean, label: String) {
  val motion = MaterialTheme.motionScheme
  AnimatedVisibility(
    visible = running,
    enter = fadeIn(motion.defaultEffectsSpec()) + expandVertically(motion.defaultSpatialSpec()),
    // An exit only fades; the slot closes on the fast tier.
    exit = fadeOut(motion.fastEffectsSpec()) + shrinkVertically(motion.fastSpatialSpec()),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      LoadingIndicator(modifier = Modifier.size(24.dp))
      Text(label, style = MaterialTheme.typography.labelLarge)
    }
  }
}
