package io.github.raghavsatyadev.support.listeners

fun interface ErrorListener<T> {

    fun onError(t: T)
}