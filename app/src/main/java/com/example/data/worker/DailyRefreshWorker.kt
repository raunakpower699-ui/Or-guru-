package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.NumberRepository
import java.util.concurrent.TimeUnit

class DailyRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "DailyRefreshWorker started: checking 24h quota & anti-loop status...")
            val database = AppDatabase.getInstance(applicationContext)
            val repository = NumberRepository(database)

            // Ensures expired numbers are retired, old blacklist hashes cleaned,
            // and 10 fresh numbers are allocated with anti-loop guarantee.
            val count = repository.ensureDailyQuota(forceRefresh = false)
            Log.d(TAG, "DailyRefreshWorker completed successfully with $count active numbers.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "DailyRefreshWorker execution failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DailyRefreshWorker"
        const val WORK_NAME = "raunak_daily_refresh_worker"

        fun scheduleDailyWork(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()

                val refreshRequest = PeriodicWorkRequestBuilder<DailyRefreshWorker>(
                    24, TimeUnit.HOURS,
                    30, TimeUnit.MINUTES // 30-minute flex interval
                ).setConstraints(constraints).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    refreshRequest
                )
                Log.d(TAG, "Scheduled 24-hour periodic daily refresh work.")
            } catch (e: Exception) {
                Log.w(TAG, "WorkManager scheduling skipped: ${e.message}")
            }
        }
    }
}
