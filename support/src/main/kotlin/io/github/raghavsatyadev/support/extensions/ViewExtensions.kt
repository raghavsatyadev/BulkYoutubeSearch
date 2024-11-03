package io.github.raghavsatyadev.support.extensions

import android.view.View

object ViewExtensions {
    fun View.gone() {
        if (visibility == View.GONE) return
        visibility = View.GONE
    }

    fun View.visible() {
        if (visibility == View.VISIBLE) return
        visibility = View.VISIBLE
    }

    fun View.invisible() {
        if (visibility == View.INVISIBLE) return
        visibility = View.INVISIBLE
    }
}