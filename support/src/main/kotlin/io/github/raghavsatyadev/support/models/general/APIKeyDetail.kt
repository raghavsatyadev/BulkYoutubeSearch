package io.github.raghavsatyadev.support.models.general

data class APIKeyDetail(
    val appName: String,
    val key: String,
    var expiry: Long,
)