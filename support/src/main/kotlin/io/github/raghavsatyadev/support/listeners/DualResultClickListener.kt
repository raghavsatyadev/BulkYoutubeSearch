package io.github.raghavsatyadev.support.listeners

import android.view.View
import io.github.raghavsatyadev.support.list.CustomClickListener

class DualResultClickListener<T, U>(
    private val onResultClick: (position: Int, v: View?, t: T, u: U) -> Unit,
) : CustomClickListener({ position, v, isLongClick ->
    // Default implementation for CustomClickListener
}) {
    fun onResultClick(position: Int, v: View?, t: T, u: U) {
        onResultClick(position, v, t, u)
    }
}