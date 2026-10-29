package io.github.raghavsatyadev.bys.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.raghavsatyadev.bys.ui.theme.AppTheme

// Render with: android studio render-compose-preview <this file> StatusPillPreview

@Preview(showBackground = true)
@Composable
fun StatusPillPreview() {
  AppTheme {
    StatusPill(
      text = "Found",
      container = MaterialTheme.colorScheme.primaryContainer,
      content = MaterialTheme.colorScheme.onPrimaryContainer,
    )
  }
}
