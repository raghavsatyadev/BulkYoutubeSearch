package io.github.raghavsatyadev.support.networking

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ErrorResult(
    @SerializedName("error")
    var error: Error,
) {
    @Keep
    data class Error(
        @SerializedName("name")
        var name: String,
        @SerializedName("message")
        var message: String,
    )
}