package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.permissionx.guolindev.PermissionX
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.listeners.CompleteListener
import io.github.raghavsatyadev.support.listeners.ErrorListener

@Suppress("unused", "MemberVisibilityCanBePrivate")
object PermissionExtensions {
    fun AppCompatActivity.getPermissionX(
        permissions: List<String>,
        permissionReason: String? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<List<String>>? = null,
    ) = PermissionX.init(this)
        .permissions(permissions)
        .explainReasonBeforeRequest()
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                permissionReason ?: getString(R.string.permission_deny_reason_message),
                getString(R.string.permission_positive_button),
                getString(R.string.permission_negative_button)
            )
        }
        .onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                deniedList,
                getString(R.string.permission_forward_settings_message),
                getString(R.string.permission_positive_button),
                getString(R.string.permission_negative_button)
            )
        }
        .request { allGranted, _, deniedList ->
            if (allGranted) {
                listener?.onComplete()
            } else {
                errorListener?.onError(deniedList)
            }
        }

    fun Fragment.getPermissionX(
        permissions: List<String>,
        permissionReason: String? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<List<String>>? = null,
    ) = PermissionX.init(this)
        .permissions(permissions)
        .explainReasonBeforeRequest()
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                permissionReason ?: getString(R.string.permission_deny_reason_message),
                getString(R.string.permission_positive_button),
                getString(R.string.permission_negative_button)
            )
        }
        .onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                deniedList,
                getString(R.string.permission_forward_settings_message),
                getString(R.string.permission_positive_button),
                getString(R.string.permission_negative_button)
            )
        }
        .request { allGranted, _, deniedList ->
            if (allGranted) {
                listener?.onComplete()
            } else {
                errorListener?.onError(deniedList)
            }
        }

    fun Context.checkPermissions(permissions: ArrayList<String>): Boolean {
        return permissions.map { checkPermission(it) }.find { !it } ?: true
    }

    fun Context.checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun Fragment.checkPermissions(permissions: ArrayList<String>): Boolean {
        return requireContext().checkPermissions(permissions)
    }

    fun Fragment.checkPermission(permission: String): Boolean {
        return requireContext().checkPermission(permission)
    }
}