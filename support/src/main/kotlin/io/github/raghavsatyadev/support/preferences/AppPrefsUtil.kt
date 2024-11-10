package io.github.raghavsatyadev.support.preferences

import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toGsonObject
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toJsonString
import io.github.raghavsatyadev.support.models.general.APIKeyDetail
import io.github.raghavsatyadev.support.models.general.User
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.NOTIFICATION_ENABLED
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOKEN
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOPICS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

object AppPrefsUtil {
    suspend fun clearUserData() {
        CoreApp.instance.clearAllPrefs()
    }

    suspend fun saveFCMToken(token: String) {
        CoreApp.instance.savePref(TOKEN, token)
    }

    fun getFCMToken(): Flow<String?> {
        return CoreApp.instance.getPrefs(TOKEN)
    }

    suspend fun setFCMTopics(fcmTopics: JSONArray) {
        CoreApp.instance.savePref(TOPICS, fcmTopics.toString())
    }

    fun getFCMTopics(): Flow<String> {
        return CoreApp.instance.getPrefs<String?>(TOPICS).map {
            it ?: "[]"
        }
    }

    suspend fun setNotificationEnableStatus(isNotificationEnabled: Boolean) {
        CoreApp.instance.savePref(NOTIFICATION_ENABLED, isNotificationEnabled)
    }

    fun isNotificationEnabled(): Flow<Boolean> {
        return CoreApp.instance.getPrefs(NOTIFICATION_ENABLED, true)
    }

    suspend fun saveUserDetails(user: User) {
        CoreApp.instance.savePref(Keys.USER_DETAILS, user.toJsonString())
    }

    fun getUserDetails(): Flow<User?> {
        return CoreApp.instance.getPrefs(Keys.USER_DETAILS, "").map {
            if (it.isEmpty()) {
                null
            } else {
                it.toGsonObject<User>()
            }
        }
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
        CoreApp.instance.savePref(Keys.API_KEY_DETAILS, apiKeyDetails.toJsonString())
    }

    fun getKeyDetails(): Flow<ArrayList<APIKeyDetail>> {
        return CoreApp.instance.getPrefs(Keys.API_KEY_DETAILS, "").map {
            if (it.isEmpty()) {
                ArrayList()
            } else {
                it.toGsonObject<ArrayList<APIKeyDetail>>()
            }
        }
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
        const val USER_DETAILS = "user_details"
        const val API_KEY_DETAILS = "api_key_details"
        const val SONGS_REMAINING_TO_BE_FOUND = "songs_remaining_to_be_found"
    }

    object FCM {
        const val TOKEN = "token"
        const val TOPICS = "topics"
        const val NOTIFICATION_ENABLED = "notification_enabled"
    }
}