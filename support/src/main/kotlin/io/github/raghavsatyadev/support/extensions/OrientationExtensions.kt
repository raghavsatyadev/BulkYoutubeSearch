package io.github.raghavsatyadev.support.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Suppress("unused")
object OrientationExtensions {
    /**
     * will set [ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE] if it is tab
     * or TV otherwise it will set [ActivityInfo.SCREEN_ORIENTATION_PORTRAIT]
     */
    fun Activity.setOrientation(isPortrait: Boolean) {
        requestedOrientation =
            if (isPortrait) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /** check if current orientation is portrait */
    fun Activity.isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * for setting up screen to full screen
     *
     * @param shouldMakeLandScape if true it will set
     *     [ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE], if false first
     *     it will go in [ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT] than
     *     [ActivityInfo.SCREEN_ORIENTATION_SENSOR]
     */
    @SuppressLint("SourceLockedOrientationActivity")
    fun Activity.lockOrientation(shouldMakeLandScape: Boolean) {
        if (shouldMakeLandScape) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    /** set orientation dependent on sensor of device */
    fun Activity.setFreeOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    fun Activity.enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controllerCompat = WindowInsetsControllerCompat(window, window.decorView)

        controllerCompat.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controllerCompat.hide(WindowInsetsCompat.Type.systemBars())

        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    fun Activity.disableFullScreen() {
        val controllerCompat = WindowInsetsControllerCompat(window, window.decorView)

        controllerCompat.show(WindowInsetsCompat.Type.systemBars())
    }
}