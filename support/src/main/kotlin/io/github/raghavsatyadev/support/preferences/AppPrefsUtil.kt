package io.github.raghavsatyadev.support.preferences

import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.general.APIKeyDetail
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.deleteAllPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.getPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.savePref
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.NOTIFICATION_ENABLED
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOKEN
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOPICS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object AppPrefsUtil {
    suspend fun clearAppPreferences() {
        CoreApp.instance.deleteAllPrefs()
    }

    suspend fun saveFCMToken(token: String) {
        CoreApp.instance.savePref(TOKEN, token)
    }

    fun getFCMToken(): Flow<String?> {
        return CoreApp.instance.getPrefs(TOKEN)
    }

    suspend fun setFCMTopics(fcmTopics: ArrayList<String>) {
        CoreApp.instance.savePref(TOPICS, fcmTopics.toJsonString())
    }

    fun getFCMTopics(): Flow<ArrayList<String>> {
        return CoreApp.instance.getPrefs(TOPICS, "[]").map {
            it.toKotlinObject()
        }
    }

    suspend fun setNotificationEnableStatus(isNotificationEnabled: Boolean) {
        CoreApp.instance.savePref(NOTIFICATION_ENABLED, isNotificationEnabled)
    }

    fun isNotificationEnabled(): Flow<Boolean> {
        return CoreApp.instance.getPrefs(NOTIFICATION_ENABLED, true)
    }

    suspend fun saveKeyDetail(currentKeyDetails: APIKeyDetail, currentMilliSecond: Long) {
        val apiKeyDetails = getNonExpiredKeyDetails()
        apiKeyDetails?.find {
            it.key == currentKeyDetails.key
        }?.let {
            it.expiry = currentMilliSecond
            saveAllKeyDetail(apiKeyDetails)
        }
    }

    suspend fun saveAllKeyDetail(apiKeyDetails: ArrayList<APIKeyDetail>) {
        apiKeyDetails.sortBy {
            it.expiry
        }
        CoreApp.instance.savePref(Keys.API_KEY_DETAILS, apiKeyDetails.toJsonString())
    }

    fun getKeyDetails(): Flow<ArrayList<APIKeyDetail>> {
        return CoreApp.instance.getPrefs(Keys.API_KEY_DETAILS, "[]").map {
            it.toKotlinObject()
        }
    }

    suspend fun getNonExpiredKeyDetails(): ArrayList<APIKeyDetail>? {
        val hours24 = 1000 * 60 * 60 * 24
        val differenceTime = System.currentTimeMillis() - hours24
        val allKeys = CoreApp.instance.getPrefs(Keys.API_KEY_DETAILS, "[]")
            .first()
        if (allKeys.isEmpty()) {
            return null
        }
        val filteredAPIKeys = allKeys
            .toKotlinObject<ArrayList<APIKeyDetail>>()
            .filter {
                it.expiry < differenceTime
            }.toCollection(ArrayList())
        return filteredAPIKeys
    }

    object Keys {
        const val API_KEY_DETAILS = "api_key_details"
    }

    object FCM {
        const val TOKEN = "token"
        const val TOPICS = "topics"
        const val NOTIFICATION_ENABLED = "notification_enabled"
    }
}