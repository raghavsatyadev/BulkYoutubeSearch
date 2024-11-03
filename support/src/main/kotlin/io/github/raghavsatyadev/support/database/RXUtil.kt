package io.github.raghavsatyadev.support.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

object RXUtil {
    suspend fun <T> mainThreadHandler(
        t: T, coroutineScope: CoroutineScope,
    ): T {
        return withContext(coroutineScope.coroutineContext) { t }
    }
}
