package io.github.raghavsatyadev.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContract

class JsonFilePicker : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(
        context: Context,
        input: Unit,
    ): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json" // filtering for JSON only
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Uri? = if (resultCode == Activity.RESULT_OK) {
        intent?.data
    } else {
        null
    }
}

fun Uri.getFileNameFromUri(context: Context): String? {
    // Try querying the provider
    context.contentResolver
        .query(
            this,
            null,
            null,
            null,
            null
        )
        ?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
        }

    // Fallback: extract from URI path
    return this.path?.substringAfterLast('/')
}
