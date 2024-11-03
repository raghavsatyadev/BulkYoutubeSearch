package io.github.raghavsatyadev.support

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyBoardUtil {
    fun hideKeyBoard(v: View?) {
        val inputManager =
            v?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}