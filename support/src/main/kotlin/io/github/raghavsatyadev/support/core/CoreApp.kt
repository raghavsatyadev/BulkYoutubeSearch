package io.github.raghavsatyadev.support.core

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.multidex.MultiDex
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.database.RoomDBUtil
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.GoogleExtensions.checkPlayServiceAvailability
import io.github.raghavsatyadev.support.networking.KtorUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CoreApp : Application(), CoroutineScope {

    private lateinit var job: Job

    private val handler = CoroutineExceptionHandler { _, exception ->
        AppLog.loge(false, kotlinFileName, "handler", exception, Exception())
    }

    override val coroutineContext: CoroutineContext
        get() {
            var context = Dispatchers.Main + job
            if (!BuildConfig.DEBUG) context += handler
            return context
        }

    companion object {
        @Volatile
        lateinit var instance: CoreApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        job = Job()

        setupGoogleServices()
        RoomDBUtil.getDatabase()
        KtorUtil.httpClient
    }

    private fun setupGoogleServices() {
        if (checkPlayServiceAvailability()) {
            MobileAds.initialize(this)
            val initializeApp = FirebaseApp.initializeApp(this)
            initializeApp?.let {
                // initializeAppCheck(it)
            }
        } else {
            Toast.makeText(this, R.string.warning_update_play_service, Toast.LENGTH_SHORT).show()
        }
    }

    // private fun initializeAppCheck(firebaseApp: FirebaseApp) {
    //     Firebase.appCheck.installAppCheckProviderFactory(
    //         if (BuildConfig.DEBUG) {
    //             val storageHelper = StorageHelper(
    //                 firebaseApp.applicationContext,
    //                 firebaseApp.persistenceKey
    //             )
    //             // TODO: Don't forget to delete this when publishing the app
    //             storageHelper.saveDebugSecret("2E434121-1420-400F-8DCB-8E9AB4098386")
    //             DebugAppCheckProviderFactory.getInstance()
    //         } else {
    //             PlayIntegrityAppCheckProviderFactory.getInstance()
    //         },
    //     )
    // }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onTerminate() {
        job.cancel()
        super.onTerminate()
    }
}