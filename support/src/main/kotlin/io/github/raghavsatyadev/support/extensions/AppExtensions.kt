package io.github.raghavsatyadev.support.extensions

import android.content.Context

@Suppress("unused")
object AppExtensions {
  /** Clear full cache of application */
  fun Context.clearFullCache() {
    cacheDir.deleteRecursively()
    externalCacheDir?.deleteRecursively()
  }

  inline val <T : Any> T.kotlinFileName: String
    get() = javaClass.simpleName + ".kt"
}
