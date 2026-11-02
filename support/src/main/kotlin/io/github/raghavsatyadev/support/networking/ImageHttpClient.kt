package io.github.raghavsatyadev.support.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * The HTTP client Coil loads images with. It must use OkHttp: a fast scroll cancels many image
 * loads, and the Android (HttpURLConnection) engine closes the stream from another thread on
 * cancel, which throws "Unbalanced enter/exit" and crashes the app. OkHttp cancels a call safely.
 */
internal fun imageHttpClient(): HttpClient = HttpClient(OkHttp)
