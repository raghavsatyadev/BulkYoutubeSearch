@file:Suppress("unused")

package io.github.raghavsatyadev.support.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import coil.ImageLoader
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageRequest.Builder
import coil.request.ImageRequest.Listener
import coil.request.SuccessResult
import coil.size.ViewSizeResolver
import io.github.raghavsatyadev.support.extensions.ResourceExtensions.getConDrawable
import io.github.raghavsatyadev.support.listeners.CompleteListener
import io.github.raghavsatyadev.support.listeners.ErrorListener

object ImageExtensions {
    fun ImageView?.loadImage(
        url: String?,
        placeHolder: Drawable? = null,
        crossFade: Boolean = false,
        error: Drawable? = null,
        cacheAllowed: Boolean = true,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            val pair =
                getImageLoaderBuilder(
                    url = url,
                    placeHolderImage = placeHolder,
                    errorImage = error,
                    crossFade = crossFade,
                    cacheAllowed = cacheAllowed,
                    errorListener = errorListener,
                    listener = listener
                )

            pair?.first?.enqueue(pair.second.build())
        }
    }

    private fun ImageView?.getImageLoaderBuilder(
        url: String?,
        placeHolderImage: Drawable?,
        errorImage: Drawable?,
        crossFade: Boolean,
        cacheAllowed: Boolean = true,
        errorListener: ErrorListener<Throwable>?,
        listener: CompleteListener?,
    ): Pair<ImageLoader, Builder>? {
        this?.let {
            val imageLoader = context.imageLoader

            val builder = with(Builder(context)) {
                data(url)
                target(it)
                size(ViewSizeResolver(it))
                placeHolderImage?.let { placeholder(placeHolderImage).fallback(placeHolderImage) }
                errorImage?.let { error(errorImage) }
                crossfade(crossFade)
                if (!cacheAllowed) memoryCachePolicy(CachePolicy.DISABLED)
                listener(object : Listener {
                    override fun onError(request: ImageRequest, result: ErrorResult) {
                        super.onError(request, result)
                        errorListener?.onError(result.throwable)
                    }

                    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                        super.onSuccess(request, result)
                        listener?.onComplete()
                    }
                })
            }

            return Pair(imageLoader, builder)
        } ?: run { return null }
    }

    fun ImageView?.loadImage(
        url: String?,
        @DrawableRes
        placeHolderRes: Int? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            loadImage(
                url = url,
                placeHolder = context.getConDrawable(placeHolderRes),
                error = context.getConDrawable(errorRes),
                listener = listener,
                errorListener = errorListener
            )
        }
    }

    fun ImageView?.loadImage(
        url: String?,
        placeHolder: Drawable? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            loadImage(
                url = url,
                placeHolder = placeHolder,
                error = context.getConDrawable(errorRes),
                listener = listener,
                errorListener = errorListener
            )
        }
    }

    fun ImageView?.loadImage(
        url: String?,
        @DrawableRes
        placeHolderRes: Int? = null,
        error: Drawable? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            loadImage(
                url = url,
                placeHolder = context.getConDrawable(placeHolderRes),
                error = error,
                listener = listener,
                errorListener = errorListener
            )
        }
    }

    fun ImageView?.loadImage(
        @StringRes
        urlRes: Int,
        placeHolder: Drawable? = null,
        error: Drawable? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            loadImage(
                url = context.getString(urlRes),
                placeHolder = placeHolder,
                error = error,
                listener = listener,
                errorListener = errorListener,
            )
        }
    }

    fun ImageView?.loadImage(
        @StringRes
        urlRes: Int,
        @DrawableRes
        placeHolderRes: Int? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            loadImage(
                urlRes = urlRes,
                placeHolder = context.getConDrawable(placeHolderRes),
                error = context.getConDrawable(errorRes),
                listener = listener, errorListener = errorListener
            )
        }
    }

    fun ImageView?.loadImage(
        @StringRes
        urlRes: Int,
        placeHolder: Drawable? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            loadImage(
                urlRes = urlRes,
                placeHolder = placeHolder,
                error = context.getConDrawable(errorRes),
                listener = listener,
                errorListener = errorListener
            )
        }
    }

    fun ImageView?.loadImage(
        @StringRes
        urlRes: Int,
        @DrawableRes
        placeHolderRes: Int? = null,
        error: Drawable? = null,
        listener: CompleteListener? = null,
        errorListener: ErrorListener<Throwable>? = null,
    ) {
        this?.let {
            loadImage(
                urlRes = urlRes,
                placeHolder = context.getConDrawable(placeHolderRes),
                error = error,
                listener = listener,
                errorListener = errorListener
            )
        }
    }
}