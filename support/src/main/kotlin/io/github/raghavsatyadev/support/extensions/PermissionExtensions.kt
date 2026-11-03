package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

@Suppress("unused", "MemberVisibilityCanBePrivate")
object PermissionExtensions {
  fun Context.checkPermissions(permissions: ArrayList<String>): Boolean {
    return permissions.map { checkPermission(it) }.find { !it } ?: true
  }

  fun Context.checkPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
  }
}
