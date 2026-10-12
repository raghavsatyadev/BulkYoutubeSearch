@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.graphics.shapes.RoundedPolygon
import com.google.android.gms.ads.AdView
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds

/** A flexible top bar that collapses on scroll; shows a back arrow when [onBack] is set. */
@Composable
fun AppTopBar(
  title: String,
  scrollBehavior: TopAppBarScrollBehavior,
  onBack: (() -> Unit)? = null,
  subtitle: String? = null,
  actions: @Composable () -> Unit = {},
) {
  MediumFlexibleTopAppBar(
    title = { Text(title) },
    subtitle = subtitle?.let { { Text(it) } },
    navigationIcon = {
      if (onBack != null) {
        IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
          Icon(
            painter = painterResource(io.github.raghavsatyadev.support.R.drawable.ic_back),
            contentDescription = stringResource(R.string.navigate_back),
          )
        }
      }
    },
    actions = { actions() },
    scrollBehavior = scrollBehavior,
    colors =
      TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
      ),
  )
}

/** The AdMob banner, kept as a View; nothing in previews. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
  if (LocalInspectionMode.current) return
  val activity = LocalContext.current as? ComponentActivity ?: return
  AndroidView(
    modifier = modifier.fillMaxWidth().navigationBarsPadding(),
    factory = { context ->
      AdView(context).also { activity.loadAds(it) }
    },
  )
}

/**
 * An icon set inside a Material shape, so each kind of thing (songs, keys, search) is told apart by
 * shape as well as colour.
 */
@Composable
fun ShapeIcon(
  painter: Painter,
  container: Color,
  content: Color,
  modifier: Modifier = Modifier,
  shape: RoundedPolygon = MaterialShapes.Cookie9Sided,
  size: Dp = 56.dp,
) {
  Box(
    modifier = modifier.size(size).clip(shape.toShape()).background(container),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      painter = painter,
      contentDescription = null,
      tint = content,
      modifier = Modifier.size(size / 2.2f),
    )
  }
}

/** A short status label in a pill. */
@Composable
fun StatusPill(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
  Surface(
    shape = MaterialTheme.shapes.small,
    color = container,
    contentColor = content,
    modifier = modifier,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
  }
}

/** A friendly empty list: a shape, a title and a hint. */
@Composable
fun EmptyState(painter: Painter, title: String, hint: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    ShapeIcon(
      painter = painter,
      container = MaterialTheme.colorScheme.secondaryContainer,
      content = MaterialTheme.colorScheme.onSecondaryContainer,
      shape = MaterialShapes.SoftBurst,
      size = 96.dp,
    )
    Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
    Text(
      hint,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
  }
}
