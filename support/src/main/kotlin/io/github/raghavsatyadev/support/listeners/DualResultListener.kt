package io.github.raghavsatyadev.support.listeners

fun interface DualResultListener<T, U> {

    fun onResult(t: T, u: U)
}