package io.github.raghavsatyadev.support.preferences

import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.models.general.APIKeyDetail
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.deleteAllPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.getPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.getPrefsList
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.savePref
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.NOTIFICATION_ENABLED
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOKEN
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOPICS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
        CoreApp.instance.savePref(TOPICS, fcmTopics)
    }

    fun getFCMTopics(): Flow<ArrayList<String>> {
        return CoreApp.instance.getPrefs(TOPICS, ArrayList())
    }

    suspend fun setNotificationEnableStatus(isNotificationEnabled: Boolean) {
        CoreApp.instance.savePref(NOTIFICATION_ENABLED, isNotificationEnabled)
    }

    fun isNotificationEnabled(): Flow<Boolean> {
        return CoreApp.instance.getPrefs(NOTIFICATION_ENABLED, true)
    }

    suspend fun saveKeyDetail(currentKeyDetails: APIKeyDetail, currentMilliSecond: Long) {
        val apiKeyDetails = getKeyDetails().first()
        apiKeyDetails.find {
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
        CoreApp.instance.savePref(Keys.API_KEY_DETAILS, apiKeyDetails)
    }

    fun getKeyDetails(): Flow<ArrayList<APIKeyDetail>> {
        return CoreApp.instance.getPrefsList(Keys.API_KEY_DETAILS)
    }

    suspend fun setRemainingSongsToBeFound(firstTimeValue: Int = -1) {
        val songsRemaining = if (firstTimeValue == -1) {
            var oldValue = getRemainingSongsToBeFound().first()
            oldValue--
            oldValue
        } else {
            firstTimeValue
        }
        CoreApp.instance.savePref(Keys.SONGS_REMAINING_TO_BE_FOUND, songsRemaining)
    }

    fun getRemainingSongsToBeFound(): Flow<Int> {
        return CoreApp.instance.getPrefs(Keys.SONGS_REMAINING_TO_BE_FOUND, 0)
    }

    object Keys {
        const val API_KEY_DETAILS = "api_key_details"
        const val SONGS_REMAINING_TO_BE_FOUND = "songs_remaining_to_be_found"
    }

    object FCM {
        const val TOKEN = "token"
        const val TOPICS = "topics"
        const val NOTIFICATION_ENABLED = "notification_enabled"
    }
}