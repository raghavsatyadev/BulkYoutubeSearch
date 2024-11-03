package io.github.raghavsatyadev.support.models.essential

data class CustomError(
    val errorCode: ErrorCode,
    val exception: Exception? = null,
)

