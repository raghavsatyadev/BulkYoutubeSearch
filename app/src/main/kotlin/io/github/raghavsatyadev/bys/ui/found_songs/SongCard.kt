package io.github.raghavsatyadev.bys.ui.found_songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.raghavsatyadev.bys.R
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val ReleaseDateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** "12 Mar 2019" from YouTube's ISO-8601 time, or null when it is missing or unreadable. */
private fun formatReleaseDate(publishedAt: String): String? = runCatching {
  OffsetDateTime.parse(publishedAt).toLocalDate().format(ReleaseDateFormat)
}
  .getOrNull()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SongCard(row: SongRow, onClick: () -> Unit, onDelete: () -> Unit) {
  val song = row.song
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      AsyncImage(
        model = song.thumbnailHigh.ifEmpty { song.thumbnailLow },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.width(120.dp).aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium),
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          row.title,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          row.artist,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        val released = remember(song.publishedAt) { formatReleaseDate(song.publishedAt) }
        if (released != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              painterResource(R.drawable.ic_event),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(14.dp),
            )
            Text(
              stringResource(R.string.released_on, released),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.tertiary,
            )
          }
        }
      }
      FilledTonalIconButton(
        onClick = onDelete,
        shapes = IconButtonDefaults.shapes(),
        colors =
          IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
          ),
      ) {
        Icon(
          painterResource(R.drawable.ic_delete),
          contentDescription = stringResource(R.string.delete_song),
        )
      }
    }
  }
}
