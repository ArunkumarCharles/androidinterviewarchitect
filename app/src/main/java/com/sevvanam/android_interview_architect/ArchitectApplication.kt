package com.sevvanam.android_interview_architect

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.sevvanam.android_interview_architect.core.data.work.CacheSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ArchitectApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // KEEP: don't restart (and lose progress on) an already-scheduled sync if this is just a
        // normal app relaunch rather than a first install.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CacheSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            CacheSyncWorker.createWorkRequest()
        )
    }
}
