package io.github.raghavsatyadev.support

import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.CornerSize
import com.google.android.material.shape.ShapeAppearanceModel

object AppUtil {
    val topRoundCornerShapeAppearanceOverlay = ShapeAppearanceModel().toBuilder().apply {
        setTopRightCorner(CornerFamily.ROUNDED, CornerSize { return@CornerSize 45F })
        setTopLeftCorner(CornerFamily.ROUNDED, CornerSize { return@CornerSize 45F })
    }.build()
}