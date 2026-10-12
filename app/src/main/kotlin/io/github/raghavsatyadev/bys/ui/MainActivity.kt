@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.raghavsatyadev.bys.ui.dashboard.DashboardScreen
import io.github.raghavsatyadev.bys.ui.found_songs.FoundSongsScreen
import io.github.raghavsatyadev.bys.ui.manage_keys.ManageKeysScreen
import io.github.raghavsatyadev.bys.ui.player.PlayerActivity
import io.github.raghavsatyadev.bys.ui.search.SearchScreen
import io.github.raghavsatyadev.bys.ui.theme.AppTheme
import io.github.raghavsatyadev.bys.ui.theme.animationsEnabled
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import kotlinx.serialization.Serializable

@Serializable data object Dashboard : NavKey

@Serializable data object Search : NavKey

@Serializable data object FoundSongs : NavKey

@Serializable data object ManageKeys : NavKey

class MainActivity : ComponentActivity() {
  private val viewModel: AppViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) viewModel.setupData()
    setContent { AppTheme { BysApp(viewModel) } }
  }
}

/**
 * Peer screens slide on the shared X axis: forward enters from the right, back retraces it to the
 * right, so predictive back shows the true reversal. With motion off the screens only fade.
 */
private fun sharedAxisX(
  motion: MotionScheme,
  forward: Boolean,
  animate: Boolean,
): ContentTransform {
  if (!animate)
    return fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
  val dir = if (forward) 1 else -1
  return (slideInHorizontally(motion.defaultSpatialSpec()) { (it * 0.3f).toInt() * dir } +
    fadeIn(motion.defaultEffectsSpec())) togetherWith
    (slideOutHorizontally(motion.defaultSpatialSpec()) { -(it * 0.3f).toInt() * dir } +
      fadeOut(motion.fastEffectsSpec()))
}

@Composable
private fun BysApp(viewModel: AppViewModel) {
  val backStack = rememberNavBackStack(Dashboard)
  val motion = MaterialTheme.motionScheme
  val animate = animationsEnabled()
  val back: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
  NavDisplay(
    backStack = backStack,
    onBack = back,
    transitionSpec = { sharedAxisX(motion, forward = true, animate) },
    popTransitionSpec = { sharedAxisX(motion, forward = false, animate) },
    predictivePopTransitionSpec = { sharedAxisX(motion, forward = false, animate) },
    entryProvider =
      entryProvider {
        entry<Dashboard> {
          DashboardScreen(
            viewModel = viewModel,
            onSearch = { backStack.add(Search) },
            onFoundSongs = { backStack.add(FoundSongs) },
            onManageKeys = { backStack.add(ManageKeys) },
          )
        }
        entry<Search> {
          SearchScreen(
            viewModel = viewModel,
            onBack = back,
            onManageKeys = { backStack.add(ManageKeys) },
          )
        }
        entry<FoundSongs> {
          FoundSongsScreen(
            viewModel = viewModel,
            onBack = back,
            onPlay = { song, context ->
              context.startActivity(PlayerActivity.intent(context, song.toJsonString()))
            },
          )
        }
        entry<ManageKeys> { ManageKeysScreen(viewModel = viewModel, onBack = back) }
      },
  )
}
