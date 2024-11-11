package io.github.raghavsatyadev.support.extensions

import android.content.DialogInterface
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.raghavsatyadev.support.R

object ErrorShowExtensions {
    // region SnackBar
    fun Fragment.snackBar(message: String) {
        Snackbar.make(requireContext(), requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    fun ComponentActivity.snackBar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    fun Fragment.snackBar(
        @StringRes
        message: Int,
    ) {
        Snackbar.make(requireContext(), requireView(), getString(message), Snackbar.LENGTH_LONG)
            .show()
    }

    fun ComponentActivity.snackBar(
        @StringRes
        message: Int,
    ) {
        Snackbar.make(findViewById(android.R.id.content), getString(message), Snackbar.LENGTH_LONG)
            .show()
    }
    // endregion

    // region Error Dialog
    fun Fragment.errorDialog(
        @StringRes
        message: Int,
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.error)
            .setMessage(getString(message))
            .setNegativeButton(
                R.string.okay
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    fun ComponentActivity.errorDialog(
        @StringRes
        message: Int,
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error)
            .setMessage(getString(message))
            .setNegativeButton(
                R.string.okay
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }
    // endregion
}