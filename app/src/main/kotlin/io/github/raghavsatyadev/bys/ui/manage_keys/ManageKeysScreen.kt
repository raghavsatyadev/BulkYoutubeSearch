@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.manage_keys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.raghavsatyadev.bys.ui.components.EmptyState
import io.github.raghavsatyadev.bys.ui.components.ShapeIcon
import io.github.raghavsatyadev.bys.ui.found_songs.DelayedLoading
import io.github.raghavsatyadev.support.extensions.DateExtensions.formatMillisToDate
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.copyToClipboard
import io.github.raghavsatyadev.support.models.general.APIKeyDetail
import kotlinx.coroutines.launch

@Composable
fun ManageKeysScreen(viewModel: AppViewModel, onBack: () -> Unit) {
  val keys by viewModel.keys.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val snackbar = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  var adding by remember { mutableStateOf(false) }
  var deleting by remember { mutableStateOf<APIKeyDetail?>(null) }
  val copied = stringResource(R.string.key_copied)

  Scaffold(
    modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
    topBar = {
      AppTopBar(
        title = stringResource(R.string.manage_keys),
        subtitle = keys?.let { pluralStringResource(R.plurals.keys_count, it.size, it.size) },
        scrollBehavior = scroll,
        onBack = onBack,
      )
    },
    floatingActionButton = {
      SmallExtendedFloatingActionButton(
        onClick = { adding = true },
        icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
        text = { Text(stringResource(R.string.add_key)) },
      )
    },
    snackbarHost = { SnackbarHost(snackbar) },
    bottomBar = { AdBanner() },
  ) { padding ->
    val list = keys
    when {
      list == null -> DelayedLoading(Modifier.fillMaxSize().padding(padding))
      list.isEmpty() ->
        EmptyState(
          painter = painterResource(R.drawable.ic_key),
          title = stringResource(R.string.no_keys_title),
          hint = stringResource(R.string.no_keys_hint),
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
              bottom = padding.calculateBottomPadding() + 96.dp,
            ),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(list, key = { it.key }) { key ->
            KeyCard(
              key = key,
              modifier = Modifier.animateItem(),
              onCopy = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                context.copyToClipboard("API Key", key.key)
                scope.launch { snackbar.showSnackbar(copied) }
              },
              onDelete = { deleting = key },
            )
          }
        }
    }
  }

  if (adding) {
    AddKeyDialog(
      onDismiss = { adding = false },
      onAdd = { name, key ->
        viewModel.addKey(name, key)
        adding = false
      },
    )
  }
  deleting?.let { key ->
    AlertDialog(
      onDismissRequest = { deleting = null },
      title = { Text(stringResource(R.string.confirm_delete_key_title)) },
      text = { Text(stringResource(R.string.confirm_delete_key_message)) },
      confirmButton = {
        TextButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            viewModel.deleteKey(key.key)
            deleting = null
          },
          shapes = ButtonDefaults.shapes(),
        ) {
          Text(stringResource(R.string.delete_key), color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { deleting = null }, shapes = ButtonDefaults.shapes()) {
          Text(stringResource(android.R.string.cancel))
        }
      },
    )
  }
}

@Composable
private fun KeyCard(
  key: APIKeyDetail,
  modifier: Modifier,
  onCopy: () -> Unit,
  onDelete: () -> Unit,
) {
  // A key that hit its daily quota is marked with that time and is usable again a day later.
  val resetAt = key.expiry + DAY_MILLIS
  val expired = key.expiry > 0 && resetAt > System.currentTimeMillis()
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      ShapeIcon(
        painter = painterResource(R.drawable.ic_key),
        container =
          if (expired) MaterialTheme.colorScheme.errorContainer
          else MaterialTheme.colorScheme.secondaryContainer,
        content =
          if (expired) MaterialTheme.colorScheme.onErrorContainer
          else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialShapes.Gem,
        size = 48.dp,
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(key.appName, style = MaterialTheme.typography.titleMedium)
        // Not colour alone: an exhausted key also says so in words.
        Text(
          if (expired)
            stringResource(
              R.string.key_exhausted_on,
              key.expiry.formatMillisToDate("dd MMM, HH:mm"),
            )
          else stringResource(R.string.key_ready),
          style = MaterialTheme.typography.bodySmall,
          color =
            if (expired) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      // The two actions are one connected group.
      Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        FilledTonalIconButton(
          onClick = onCopy,
          shapes =
            IconButtonDefaults.shapes(
              shape =
                RoundedCornerShape(
                  topStartPercent = 50,
                  bottomStartPercent = 50,
                  topEndPercent = 15,
                  bottomEndPercent = 15,
                ),
              pressedShape = MaterialTheme.shapes.small,
            ),
        ) {
          Icon(
            painterResource(R.drawable.ic_copy),
            contentDescription = stringResource(R.string.copy_key),
          )
        }
        FilledTonalIconButton(
          onClick = onDelete,
          shapes =
            IconButtonDefaults.shapes(
              shape =
                RoundedCornerShape(
                  topStartPercent = 15,
                  bottomStartPercent = 15,
                  topEndPercent = 50,
                  bottomEndPercent = 50,
                ),
              pressedShape = MaterialTheme.shapes.small,
            ),
          colors =
            IconButtonDefaults.filledTonalIconButtonColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
          Icon(
            painterResource(R.drawable.ic_delete),
            contentDescription = stringResource(R.string.delete_key),
          )
        }
      }
    }
  }
}

@Composable
private fun AddKeyDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
  var name by remember { mutableStateOf("") }
  var key by remember { mutableStateOf("") }
  var tried by remember { mutableStateOf(false) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.add_api_key)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(R.string.key_app_name)) },
          isError = tried && name.isBlank(),
          singleLine = true,
        )
        OutlinedTextField(
          value = key,
          onValueChange = { key = it },
          label = { Text(stringResource(R.string.api_key_hint)) },
          isError = tried && key.isBlank(),
          singleLine = true,
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          tried = true
          if (name.isNotBlank() && key.isNotBlank()) onAdd(name.trim(), key.trim())
        },
        shapes = ButtonDefaults.shapes(),
      ) {
        Text(stringResource(R.string.add_key))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
        Text(stringResource(android.R.string.cancel))
      }
    },
  )
}

private const val DAY_MILLIS = 24L * 60 * 60 * 1000
