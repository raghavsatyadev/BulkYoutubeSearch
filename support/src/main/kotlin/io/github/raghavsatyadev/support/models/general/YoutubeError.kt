package io.github.raghavsatyadev.support.models.general

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class YoutubeError(
    var error: Error,
) : Parcelable {
    @Keep
    @Parcelize
    data class Error(
        var code: Int,
        var message: String,
        var errors: List<Error>,
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Error(
            var message: String,
            var domain: String,
            var reason: String,
        ) : Parcelable
    }
}