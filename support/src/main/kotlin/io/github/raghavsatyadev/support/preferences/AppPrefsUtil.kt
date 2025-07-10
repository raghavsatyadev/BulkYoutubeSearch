package io.github.raghavsatyadev.support.preferences

import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.general.APIKeyDetail
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.getPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.savePref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object AppPrefsUtil {
    suspend fun saveKeyDetail(
        currentKeyDetails: APIKeyDetail,
        currentMilliSecond: Long,
    ) {
        val apiKeyDetails = getNonExpiredKeyDetails()
        val keyDetail = apiKeyDetails.find { it.key == currentKeyDetails.key }
        keyDetail?.expiry = currentMilliSecond
        saveAllKeyDetail(apiKeyDetails)
    }

    suspend fun saveAllKeyDetail(apiKeyDetails: ArrayList<APIKeyDetail>) {
        apiKeyDetails.sortBy { it.expiry }
        CoreApp.instance.savePref(
            Keys.API_KEY_DETAILS,
            apiKeyDetails.toJsonString()
        )
    }

    fun getKeyDetails(): Flow<ArrayList<APIKeyDetail>> {
        return CoreApp.instance
            .getPrefs(
                Keys.API_KEY_DETAILS,
                "[]"
            )
            .map { it.toKotlinObject() }
    }

    suspend fun getNonExpiredKeyDetails(): ArrayList<APIKeyDetail> {
        val hours24 = 1000 * 60 * 60 * 24
        val differenceTime = System.currentTimeMillis() - hours24
        val allKeys = CoreApp.instance
            .getPrefs(
                Keys.API_KEY_DETAILS,
                "[]"
            )
            .first()

        val filteredAPIKeys = allKeys
            .toKotlinObject<ArrayList<APIKeyDetail>>()
            .filter { it.expiry < differenceTime }
            .toCollection(ArrayList())
        return filteredAPIKeys
    }

    object Keys {
        const val API_KEY_DETAILS = "api_key_details"
    }
}
