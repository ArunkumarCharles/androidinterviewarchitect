package com.sevvanam.android_interview_architect.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.sevvanam.android_interview_architect.core.database.entity.PostEntity

/**
 * Bridges network -> Room for Paging. Room stays the single source of truth: the UI only ever pages
 * over Room's PagingSource, and this mediator just refreshes the table.
 *
 * The API has no page cursor (it returns the full list), so only REFRESH does work; PREPEND/APPEND
 * report end-of-pagination. With a paged backend you would store next-page keys per row here.
 * A failed refresh surfaces as LoadState.Error while cached pages keep displaying.
 */
@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val sync: suspend () -> Unit
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        if (loadType != LoadType.REFRESH) return MediatorResult.Success(endOfPaginationReached = true)
        return try {
            sync()
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
