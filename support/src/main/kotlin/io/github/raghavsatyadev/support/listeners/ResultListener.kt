package io.github.raghavsatyadev.support.listeners

fun interface ResultListener<T> {

    fun onResult(t: T)
}