@file:Suppress("unused")

package io.github.raghavsatyadev.support.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toGsonObject
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toJsonString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AppPrefsExtensions {
    val APP_PREFS_NAME: String = CoreApp.instance.getString(R.string.app_name)

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(APP_PREFS_NAME)

    suspend inline fun <T> Context.savePref(key: String, value: T) {
        dataStore.edit {
            when (value) {
                is Boolean -> it[booleanPreferencesKey(key)] = value
                is Int -> it[intPreferencesKey(key)] = value
                is String -> it[stringPreferencesKey(key)] = value
                is Float -> it[floatPreferencesKey(key)] = value
                is Double -> it[doublePreferencesKey(key)] = value
                is Long -> it[longPreferencesKey(key)] = value
                else -> it[stringPreferencesKey(key)] = value.toJsonString()
            }
        }
    }

    suspend inline fun <T> Fragment.savePref(key: String, value: T) {
        requireContext().savePref(key, value)
    }

    inline fun <reified T> getKey(key: String): Key<*>? {
        return when (T::class) {
            Boolean::class -> booleanPreferencesKey(key)
            Int::class -> intPreferencesKey(key)
            String::class -> stringPreferencesKey(key)
            Float::class -> floatPreferencesKey(key)
            Double::class -> doublePreferencesKey(key)
            Long::class -> longPreferencesKey(key)
            else -> null
        }
    }

    suspend fun Context.deleteAllPrefs() {
        dataStore.edit { it.clear() }
    }

    suspend inline fun <reified T> Context.deletePref(key: String) {
        dataStore.edit {
            val keyType = getKey<T>(key)
            if (keyType != null) {
                it.remove(keyType)
            }
        }
    }

    suspend fun Fragment.deleteAllPrefs() {
        requireContext().deleteAllPrefs()
    }

    suspend inline fun <reified T> Fragment.deletePref(key: String) {
        requireContext().deletePref<T>(key)
    }

    inline fun <reified T> Context.getPrefs(
        key: String,
        defaultValue: T,
    ): Flow<T> {
        return dataStore.data.map {
            val keyType = getKey<T>(key)
            if (keyType != null) {
                (it[keyType] ?: defaultValue) as T
            } else {
                val objectKey = stringPreferencesKey(key)
                (it[objectKey])?.toGsonObject() ?: defaultValue
            }
        }
    }

    inline fun <reified T> Context.getPrefs(
        key: String,
    ): Flow<T?> {
        return dataStore.data.map {
            val keyType = getKey<T>(key)
            if (keyType != null) {
                (it[keyType]) as T?
            } else {
                val objectKey = stringPreferencesKey(key)
                (it[objectKey])?.toGsonObject()
            }
        }
    }

    suspend inline fun <reified T> Context.savePref(key: String, value: List<T>) {
        savePref(key, value.toJsonString())
    }

    suspend inline fun <reified K, reified V> Context.savePref(key: String, value: Map<K, V>) {
        savePref(key, value.toJsonString())
    }

    suspend inline fun <reified T> Context.savePref(key: String, value: Set<T>) {
        savePref(key, value.toJsonString())
    }

    inline fun <reified T> Context.getPrefsList(
        key: String,
        defaultValue: ArrayList<T> = arrayListOf(),
    ): Flow<ArrayList<T>> {
        return getPrefs(key, defaultValue.toJsonString()).map { it.toGsonObject() }
    }

    inline fun <reified K, reified V> Context.getPrefsMap(
        key: String,
        defaultValue: Map<K, V> = emptyMap(),
    ): Flow<Map<K, V>> {
        return getPrefs(key, defaultValue.toJsonString()).map { it.toGsonObject() }
    }

    inline fun <reified T> Context.getPrefsSet(
        key: String,
        defaultValue: Set<T> = emptySet(),
    ): Flow<Set<T>> {
        return getPrefs(key, defaultValue.toJsonString()).map { it.toGsonObject() }
    }
}
