package io.github.raghavsatyadev.support.extensions.serializer

@Suppress("unused")
object SerializationExtensions {
    inline fun <reified T> String.toKotlinObject(): T {
        return kotlinJsonSerializer.decodeFromString(this)
    }

    inline fun <reified T> T.toJsonString(): String {
        return kotlinJsonSerializer.encodeToString(this)
    }
}