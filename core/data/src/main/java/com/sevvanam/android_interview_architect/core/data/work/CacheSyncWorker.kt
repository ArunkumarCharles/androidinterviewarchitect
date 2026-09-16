package com.sevvanam.android_interview_architect.core.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker demonstrating reliable background sync with network and battery constraints.
 * Interview point: Used for offline-first background cache synchronization when the app is idle.
 * Depends on the PostRepository interface (not PostRepositoryImpl) so it stays testable against a
 * fake repository and honors the same Dependency Inversion the rest of the app follows.
 */
@HiltWorker
class CacheSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val postRepository: PostRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            // Refresh posts from network repository into local Room database (Single Source of Truth)
            postRepository.refreshFeed()
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                ListenableWorker.Result.retry()
            } else {
                ListenableWorker.Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "CacheSyncPeriodicWork"

        fun createConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        }

        fun createWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<CacheSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(createConstraints())
                .build()
        }
    }
}
