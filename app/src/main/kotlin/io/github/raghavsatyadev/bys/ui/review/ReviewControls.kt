@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.bys.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.bys.R
import io.github.raghavsatyadev.support.models.db.song_review.ReviewVerdict

/**
 * The review buttons, in their own bar under the videos so they never cover either one. The button
 * of the saved verdict is filled; tapping it again clears the review.
 */
@Composable
internal fun ReviewControls(
  verdict: ReviewVerdict?,
  hasPrevious: Boolean,
  hasNext: Boolean,
  onPrevious: () -> Unit,
  onNext: () -> Unit,
  onMark: (ReviewVerdict) -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  Surface(color = colors.surfaceContainer) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 8.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalIconButton(
        onClick = onPrevious,
        enabled = hasPrevious,
        shapes = IconButtonDefaults.shapes(),
      ) {
        Icon(
          painterResource(R.drawable.ic_skip_previous),
          contentDescription = stringResource(R.string.review_previous),
        )
      }
      VerdictButton(
        text = stringResource(R.string.review_mark_mismatch),
        icon = R.drawable.ic_close,
        selected = verdict == ReviewVerdict.MISMATCH,
        filled = colors.error to colors.onError,
        tonal = colors.errorContainer to colors.onErrorContainer,
        onClick = { onMark(ReviewVerdict.MISMATCH) },
        modifier = Modifier.weight(1f),
      )
      VerdictButton(
        text = stringResource(R.string.review_mark_match),
        icon = R.drawable.ic_check,
        selected = verdict == ReviewVerdict.MATCH,
        filled = colors.primary to colors.onPrimary,
        tonal = colors.primaryContainer to colors.onPrimaryContainer,
        onClick = { onMark(ReviewVerdict.MATCH) },
        modifier = Modifier.weight(1f),
      )
      FilledTonalIconButton(
        onClick = onNext,
        enabled = hasNext,
        shapes = IconButtonDefaults.shapes(),
      ) {
        Icon(
          painterResource(R.drawable.ic_skip_next),
          contentDescription = stringResource(R.string.review_next),
        )
      }
    }
  }
}

@Composable
private fun VerdictButton(
  text: String,
  icon: Int,
  selected: Boolean,
  filled: Pair<Color, Color>,
  tonal: Pair<Color, Color>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val (container, content) = if (selected) filled else tonal
  Button(
    onClick = onClick,
    shapes = ButtonDefaults.shapes(),
    colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
    contentPadding = PaddingValues(horizontal = 8.dp),
    modifier = modifier.heightIn(min = 56.dp),
  ) {
    Icon(
      painterResource(icon),
      contentDescription = null,
      modifier = Modifier.size(ButtonDefaults.IconSize),
    )
    Text(
      text,
      maxLines = 1,
      softWrap = false,
      style = MaterialTheme.typography.titleSmall,
      modifier = Modifier.padding(start = 6.dp),
    )
  }
}
