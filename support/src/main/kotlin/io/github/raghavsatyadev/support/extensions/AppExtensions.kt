package io.github.raghavsatyadev.support.extensions

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.raghavsatyadev.support.listeners.ResultListener

object AppExtensions {
    /** Clear full cache of application */
    fun Context.clearFullCache() {
        cacheDir.deleteRecursively()
        externalCacheDir?.deleteRecursively()
    }

    inline val <T : Any> T.kotlinFileName: String
        get() = javaClass.simpleName + ".kt"

    fun AppCompatTextView.setClickableForegroundSpan(
        fullString: String,
        @ColorInt
        color: Int? = null,
        applyColor: Boolean = false,
        partStrings: Array<String>,
        listener: ResultListener<String>,
    ) {
        movementMethod = LinkMovementMethod.getInstance()
        text = buildClickableForegroundSpan(
            fullString,
            color,
            applyColor,
            partStrings,
            listener
        )
    }

    fun buildClickableForegroundSpan(
        fullString: String,
        @ColorInt
        color: Int? = null,
        applyColor: Boolean = false,
        partStrings: Array<String>,
        listener: ResultListener<String>,
    ): SpannableStringBuilder {
        val ssb = SpannableStringBuilder(fullString)

        partStrings.forEach { value ->
            val indexOf = fullString.indexOf(value)
            ssb.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    widget.cancelPendingInputEvents()
                    listener.onResult(value)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isFakeBoldText = true
                    ds.isUnderlineText = false
                }
            }, indexOf, indexOf + value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (applyColor) {
                color?.let {
                    ssb.setSpan(
                        ForegroundColorSpan(color),
                        indexOf,
                        indexOf + value.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        return ssb
    }

    /**
     * to make all dialog buttons bold
     * - creates the [AlertDialog] from [MaterialAlertDialogBuilder]
     * - sets the Buttons to be bold
     * - shows the dialog
     */
    fun MaterialAlertDialogBuilder.setBoldButtonShow(): AlertDialog {
        val dialog = create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setTypeface(positiveButton.typeface, Typeface.BOLD)
            val negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE)
            negativeButton.setTypeface(negativeButton.typeface, Typeface.BOLD)
            val neutralButton = dialog.getButton(Dialog.BUTTON_NEUTRAL)
            neutralButton.setTypeface(neutralButton.typeface, Typeface.BOLD)
        }
        dialog.show()
        return dialog
    }

    /** to make all dialog buttons bold */
    fun AlertDialog.setBoldButtons(): AlertDialog {
        setOnShowListener {
            val positiveButton = this.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setTypeface(positiveButton.typeface, Typeface.BOLD)
            val negativeButton = this.getButton(Dialog.BUTTON_NEGATIVE)
            negativeButton.setTypeface(negativeButton.typeface, Typeface.BOLD)
            val neutralButton = this.getButton(Dialog.BUTTON_NEUTRAL)
            neutralButton.setTypeface(neutralButton.typeface, Typeface.BOLD)
        }
        return this
    }
}