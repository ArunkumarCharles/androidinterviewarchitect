package com.sevvanam.android_interview_architect.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import com.sevvanam.android_interview_architect.core.database.entity.PostEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediatorTest {

    private val state = PagingState<Int, PostEntity>(emptyList(), null, PagingConfig(pageSize = 10), 0)

    @Test
    fun `refresh syncs and reports end of pagination`() = runTest {
        var syncs = 0
        val result = PostRemoteMediator { syncs++ }.load(LoadType.REFRESH, state)

        assertEquals(1, syncs)
        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `refresh failure becomes MediatorResult Error so cached pages stay visible`() = runTest {
        val result = PostRemoteMediator { throw IOException("offline") }.load(LoadType.REFRESH, state)

        assertTrue(result is MediatorResult.Error)
    }

    @Test
    fun `append and prepend do no network work`() = runTest {
        var syncs = 0
        val mediator = PostRemoteMediator { syncs++ }

        assertTrue((mediator.load(LoadType.APPEND, state) as MediatorResult.Success).endOfPaginationReached)
        assertTrue((mediator.load(LoadType.PREPEND, state) as MediatorResult.Success).endOfPaginationReached)
        assertEquals(0, syncs)
    }
}
