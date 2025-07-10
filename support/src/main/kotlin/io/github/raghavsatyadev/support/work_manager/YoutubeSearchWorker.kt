package io.github.raghavsatyadev.support.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.work_manager.YoutubeSearchUtil.searchVideos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class YoutubeSearchWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(
    appContext,
    workerParams
) {

    companion object {
        private const val UNIQUE_WORK_NAME = "YoutubeSearch"
        private const val UNIQUE_WORK_NAME_PERIODIC = "YoutubeSearchPeriodic"
        private val manager by lazy {
            WorkManager.getInstance(CoreApp.instance)
        }
        private val oneTimeWorkStatus by lazy {
            manager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
        }
        private val periodicWorkStatus by lazy {
            manager.getWorkInfosForUniqueWorkFlow(
                UNIQUE_WORK_NAME_PERIODIC
            )
        }

        suspend fun startWorker() {
            // Check the status of the periodic work request
            val periodicWorkInfos = withContext(Dispatchers.IO) {
                manager
                    .getWorkInfosForUniqueWork(UNIQUE_WORK_NAME_PERIODIC)
                    .get()
            }
            val periodicWorkRunning = periodicWorkInfos.any { it.state == WorkInfo.State.RUNNING }

            if (!periodicWorkRunning) {

                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<YoutubeSearchWorker>().build()

                // Chain the periodic work to start after the one-time work completes
                manager.enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    oneTimeWorkRequest
                )

                oneTimeWorkStatus.collectLatest { infos ->
                    val oneTimeWorkRunning = infos.any { it.state == WorkInfo.State.RUNNING }

                    if (!oneTimeWorkRunning) {
                        // Start the periodic work
                        val periodicWorkRequest = PeriodicWorkRequestBuilder<YoutubeSearchWorker>(
                            1,
                            TimeUnit.DAYS
                        ).build()

                        manager.enqueueUniquePeriodicWork(
                            UNIQUE_WORK_NAME_PERIODIC,
                            ExistingPeriodicWorkPolicy.KEEP,
                            periodicWorkRequest
                        )
                    }
                }
            }
        }

        suspend fun listenToWorkStatus(
            onOneTimeWorkStatusChanged: ((Boolean) -> Unit),
            onPeriodicWorkStatusChanged: ((Boolean) -> Unit),
        ) {
            oneTimeWorkStatus.collectLatest { infos ->
                val oneTimeWorkRunning = infos.any { it.state == WorkInfo.State.RUNNING }

                onOneTimeWorkStatusChanged(oneTimeWorkRunning)
            }
            periodicWorkStatus.collectLatest { infos ->
                val periodicWorkRunning = infos.any { it.state == WorkInfo.State.RUNNING }
                onPeriodicWorkStatusChanged(periodicWorkRunning)
            }
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        if (searchVideos()) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}