@file:Suppress("unused")

package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate")
object ResourceExtensions {
  fun getString(format: String, vararg formatArgs: Any?): String {
    return String.format(Locale.getDefault(), format, *formatArgs)
  }

  fun Context.getConDimen(@DimenRes dimenId: Int): Float {
    return resources.getDimension(dimenId)
  }

  fun Context.getConInt(@IntegerRes intRes: Int): Int {
    return resources.getInteger(intRes)
  }

  fun Context.getConDrawable(@DrawableRes drawableId: Int?): Drawable? {
    if (drawableId == null) return null
    return ContextCompat.getDrawable(this, drawableId)
  }

  @ColorInt
  fun Context.getConColor(@ColorRes colorId: Int): Int {
    return ContextCompat.getColor(this, colorId)
  }

  @ColorInt
  fun Context.getAttrColor(@AttrRes attrID: Int): Int {
    return getAttrColorString(attrID).toColorInt()
  }

  fun Context.getAttrColorStringWithoutHash(@AttrRes attrID: Int): String {
    return getAttributeValue(attrID, "%06X")
  }

  fun Context.getAttributeValue(attrID: Int, format: String): String {
    val outValue = TypedValue()
    theme.resolveAttribute(attrID, outValue, true)
    return getString(format, 0xFFFFFF and outValue.data)
  }

  fun Context.getAttrColorString(@AttrRes attrID: Int): String {
    return getAttributeValue(attrID, "#%06X")
  }

  fun Context.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp.toFloat(),
      resources.displayMetrics,
    )
  }

  fun pxToDp(px: Int): Int {
    return (px / Resources.getSystem().displayMetrics.density).toInt()
  }
}
