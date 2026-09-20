package com.sevvanam.android_interview_architect.core.data.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.sevvanam.android_interview_architect.domain.repository.PostRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class CacheSyncWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository: PostRepository = mockk()

    // Stands in for HiltWorkerFactory: the worker only needs the repository interface.
    private fun worker(attempt: Int = 0): CacheSyncWorker =
        TestListenableWorkerBuilder<CacheSyncWorker>(context)
            .setRunAttemptCount(attempt)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = CacheSyncWorker(appContext, workerParameters, repository)
            })
            .build()

    @Test
    fun `success when refresh works`() = runTest {
        coEvery { repository.refreshFeed() } returns Unit

        assertEquals(Result.success(), worker().doWork())
    }

    @Test
    fun `retries while attempts remain`() = runTest {
        coEvery { repository.refreshFeed() } throws IOException("offline")

        assertEquals(Result.retry(), worker(attempt = 0).doWork())
        assertEquals(Result.retry(), worker(attempt = 2).doWork())
    }

    @Test
    fun `gives up after three attempts`() = runTest {
        coEvery { repository.refreshFeed() } throws IOException("offline")

        assertEquals(Result.failure(), worker(attempt = 3).doWork())
    }

    @Test
    fun `work request requires network and battery`() {
        val constraints = CacheSyncWorker.createConstraints()

        assertEquals(androidx.work.NetworkType.CONNECTED, constraints.requiredNetworkType)
        assertEquals(true, constraints.requiresBatteryNotLow())
    }
}
