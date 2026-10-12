@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.support.R

/**
 * The colour roles come from the Material Theme Builder export in the support module
 * (`md_theme_*`), so the Compose screens and any remaining Views share one palette. The resources
 * switch to their `-night` values by themselves; [darkTheme] only picks the scheme's defaults for
 * roles the export does not cover.
 */
@Composable
private fun appColorScheme(darkTheme: Boolean): ColorScheme {
  val base = if (darkTheme) darkColorScheme() else lightColorScheme()
  return base.copy(
    primary = colorResource(R.color.md_theme_primary),
    onPrimary = colorResource(R.color.md_theme_onPrimary),
    primaryContainer = colorResource(R.color.md_theme_primaryContainer),
    onPrimaryContainer = colorResource(R.color.md_theme_onPrimaryContainer),
    secondary = colorResource(R.color.md_theme_secondary),
    onSecondary = colorResource(R.color.md_theme_onSecondary),
    secondaryContainer = colorResource(R.color.md_theme_secondaryContainer),
    onSecondaryContainer = colorResource(R.color.md_theme_onSecondaryContainer),
    tertiary = colorResource(R.color.md_theme_tertiary),
    onTertiary = colorResource(R.color.md_theme_onTertiary),
    tertiaryContainer = colorResource(R.color.md_theme_tertiaryContainer),
    onTertiaryContainer = colorResource(R.color.md_theme_onTertiaryContainer),
    error = colorResource(R.color.md_theme_error),
    onError = colorResource(R.color.md_theme_onError),
    errorContainer = colorResource(R.color.md_theme_errorContainer),
    onErrorContainer = colorResource(R.color.md_theme_onErrorContainer),
    background = colorResource(R.color.md_theme_background),
    onBackground = colorResource(R.color.md_theme_onBackground),
    surface = colorResource(R.color.md_theme_surface),
    onSurface = colorResource(R.color.md_theme_onSurface),
    surfaceVariant = colorResource(R.color.md_theme_surfaceVariant),
    onSurfaceVariant = colorResource(R.color.md_theme_onSurfaceVariant),
    outline = colorResource(R.color.md_theme_outline),
    outlineVariant = colorResource(R.color.md_theme_outlineVariant),
    scrim = colorResource(R.color.md_theme_scrim),
    inverseSurface = colorResource(R.color.md_theme_inverseSurface),
    inverseOnSurface = colorResource(R.color.md_theme_inverseOnSurface),
    inversePrimary = colorResource(R.color.md_theme_inversePrimary),
    surfaceDim = colorResource(R.color.md_theme_surfaceDim),
    surfaceBright = colorResource(R.color.md_theme_surfaceBright),
    surfaceContainerLowest = colorResource(R.color.md_theme_surfaceContainerLowest),
    surfaceContainerLow = colorResource(R.color.md_theme_surfaceContainerLow),
    surfaceContainer = colorResource(R.color.md_theme_surfaceContainer),
    surfaceContainerHigh = colorResource(R.color.md_theme_surfaceContainerHigh),
    surfaceContainerHighest = colorResource(R.color.md_theme_surfaceContainerHighest),
  )
}

private val Montserrat =
  FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semi_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
  )

/** Tabular figures, so a changing count does not shift sideways. */
private const val TABULAR = "tnum"

private fun TextStyle.app(weight: FontWeight? = null, tabular: Boolean = false) =
  copy(
    fontFamily = Montserrat,
    fontWeight = weight ?: fontWeight,
    fontFeatureSettings = if (tabular) TABULAR else fontFeatureSettings,
  )

private val appTypography =
  Typography().let {
    Typography(
      displayLarge = it.displayLarge.app(FontWeight.Bold, tabular = true),
      displayMedium = it.displayMedium.app(FontWeight.Bold, tabular = true),
      displaySmall = it.displaySmall.app(FontWeight.Bold, tabular = true),
      headlineLarge = it.headlineLarge.app(FontWeight.Bold, tabular = true),
      headlineMedium = it.headlineMedium.app(FontWeight.Bold, tabular = true),
      headlineSmall = it.headlineSmall.app(FontWeight.SemiBold, tabular = true),
      titleLarge = it.titleLarge.app(FontWeight.SemiBold),
      titleMedium = it.titleMedium.app(FontWeight.SemiBold),
      titleSmall = it.titleSmall.app(FontWeight.SemiBold),
      bodyLarge = it.bodyLarge.app(),
      bodyMedium = it.bodyMedium.app(),
      bodySmall = it.bodySmall.app(),
      labelLarge = it.labelLarge.app(FontWeight.SemiBold),
      labelMedium = it.labelMedium.app(FontWeight.Medium),
      labelSmall = it.labelSmall.app(FontWeight.Medium),
    )
  }

private val appShapes =
  Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  MaterialExpressiveTheme(
    colorScheme = appColorScheme(darkTheme),
    typography = appTypography,
    shapes = appShapes,
    motionScheme = MotionScheme.expressive(),
    content = content,
  )
}
