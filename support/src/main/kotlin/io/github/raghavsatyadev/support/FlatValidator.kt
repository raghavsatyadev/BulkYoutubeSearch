package io.github.raghavsatyadev.support

import android.text.InputFilter
import android.text.Spanned

class FlatValidator(private val flats: List<String>) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        val inputText = (dest?.subSequence(0, dstart).toString() + source?.subSequence(
            start,
            end
        ) + dest?.subSequence(dend, dest.length))
        return if (flats.contains(inputText)) {
            null
        } else {
            ""
        }
    }

}
