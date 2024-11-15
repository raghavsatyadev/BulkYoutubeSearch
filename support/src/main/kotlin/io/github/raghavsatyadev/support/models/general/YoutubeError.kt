package io.github.raghavsatyadev.support.models.general

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
data class YoutubeError(
    var error: Error,
) : Parcelable {
    @Keep
    @Parcelize
    @Serializable
    data class Error(
        var code: Int,
        var message: String,
        var errors: List<Error>,
    ) : Parcelable {
        @Keep
        @Parcelize
        @Serializable
        data class Error(
            var message: String,
            var domain: String,
            var reason: String,
        ) : Parcelable
    }
}