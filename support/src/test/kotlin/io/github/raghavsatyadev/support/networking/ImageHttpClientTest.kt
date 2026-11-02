package io.github.raghavsatyadev.support.networking

import io.ktor.client.engine.okhttp.OkHttpEngine
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageHttpClientTest {
  // The Android engine crashed the app when a fast scroll cancelled image loads.
  @Test
  fun imageClientUsesOkHttpEngine() {
    imageHttpClient().use { client ->
      assertTrue(
        "Coil must load images on the OkHttp engine, not ${client.engine::class.simpleName}",
        client.engine is OkHttpEngine,
      )
    }
  }
}
