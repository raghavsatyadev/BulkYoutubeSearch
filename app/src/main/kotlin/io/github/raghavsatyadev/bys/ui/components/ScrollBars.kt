package io.github.raghavsatyadev.bys.ui.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * Whether a list's header shows. Scrolling down through [connection] hides it, scrolling up brings
 * it back.
 */
@Stable
class ScrollBars {
  var visible by mutableStateOf(true)

  // How far the finger or wheel went in one direction since it last turned.
  private var travel = 0f

  val connection =
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val dy = available.y
        if (dy == 0f) return Offset.Zero
        travel = if (dy < 0 == travel < 0) travel + dy else dy
        when {
          travel < -TURN_PX -> visible = false
          travel > TURN_PX -> visible = true
        }
        return Offset.Zero
      }
    }
}

// A small jitter of the finger should not flick the header.
private const val TURN_PX = 24f
