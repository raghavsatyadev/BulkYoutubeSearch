@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Motion roles: pick the spec by what moves. A value read as data (a count, a progress end) never
 * overshoots; a container (card, pane, button) may bounce a little; alpha and colour use effects.
 */
object AppMotion {
  /** For a value read as data: counts, progress. Critically damped, never overshoots. */
  @Composable
  @ReadOnlyComposable
  fun <T> dataSpec(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultEffectsSpec()

  /** For containers: cards, panes, buttons. May bounce a little. */
  @Composable
  @ReadOnlyComposable
  fun <T> containerSpec(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultSpatialSpec()

  /** For fades and colour changes. */
  @Composable
  @ReadOnlyComposable
  fun <T> effectsSpec(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultEffectsSpec()

  /** Material's emphasized decelerate curve: quick to arrive, gentle to settle. */
  val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
}

/**
 * False when the device has animations off (animator duration scale 0). Compose animations end at
 * once by themselves then; read this for motion Compose does not time (an infinite loop) and to
 * swap spatial motion for a short fade.
 */
@Composable
fun animationsEnabled(): Boolean {
  val scale = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
  return scale > 0f
}

/** How long one list item takes to fade and rise in. */
const val ENTRANCE_DURATION_MILLIS: Int = 180
private const val STAGGER_STEP_MILLIS = 40
private const val STAGGER_MAX_DELAY_MILLIS = 120

/** Delay before item [index] enters; capped so the cascade ends within ~300 ms. */
fun staggerDelayMillis(index: Int): Int =
  (index * STAGGER_STEP_MILLIS).coerceAtMost(STAGGER_MAX_DELAY_MILLIS)

/**
 * Fades and lifts [content] in on first composition when [play] is true. Pass [play] only for items
 * on screen when a list first opens, so it never runs on scroll.
 */
@Composable
fun StaggeredEntrance(
  index: Int,
  play: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val animate = play && animationsEnabled()
  val progress = remember { Animatable(if (animate) 0f else 1f) }
  LaunchedEffect(Unit) {
    if (progress.value < 1f) {
      progress.animateTo(
        1f,
        tween(
          durationMillis = ENTRANCE_DURATION_MILLIS,
          delayMillis = staggerDelayMillis(index),
          easing = AppMotion.EmphasizedDecelerate,
        ),
      )
    }
  }
  val offset = with(LocalDensity.current) { 16.dp.toPx() }
  Box(
    modifier =
      modifier.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offset
      }
  ) {
    content()
  }
}
