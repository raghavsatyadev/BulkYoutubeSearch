package io.github.raghavsatyadev.support.backup

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.getPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/** Writes [DbBackup] once a day into the picked videos folder. */
class DbBackupWorker(appContext: Context, params: WorkerParameters) :
  CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    val folder = applicationContext.getPrefs(AppPrefsUtil.Keys.REVIEW_FOLDER_URI, "").first()
    if (folder.isEmpty()) return Result.success()
    return runCatching { DbBackup.write(applicationContext, folder.toUri()) }
      .fold(
        onSuccess = { Result.success() },
        onFailure = { e ->
          AppLog.loge(false, kotlinFileName, "doWork", e, Exception())
          // No write access will not fix itself; the review screen asks to pick the folder again.
          if (e is SecurityException) Result.success() else Result.retry()
        },
      )
  }

  companion object {
    private const val UNIQUE_WORK_NAME = "DbBackupDaily"

    /** Schedules the daily backup; does nothing when it is already scheduled. */
    fun schedule(context: Context) {
      WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
          UNIQUE_WORK_NAME,
          ExistingPeriodicWorkPolicy.KEEP,
          PeriodicWorkRequestBuilder<DbBackupWorker>(1, TimeUnit.DAYS).build(),
        )
    }
  }
}
