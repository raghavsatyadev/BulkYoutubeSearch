package io.github.raghavsatyadev.support.models.general

import kotlinx.serialization.Serializable

@Serializable
data class APIKeyDetail(
    val appName: String,
    val key: String,
    var expiry: Long,
)