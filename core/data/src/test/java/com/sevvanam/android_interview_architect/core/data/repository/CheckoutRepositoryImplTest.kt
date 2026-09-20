package com.sevvanam.android_interview_architect.core.data.repository

import com.sevvanam.android_interview_architect.core.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutRepositoryImplTest {

    private val repository = CheckoutRepositoryImpl()

    @Test
    fun `card ending in 0000 is declined`() = runTest {
        val result = repository.submitOrder("1 Main St", "4242424242420000")

        assertTrue(result is Result.Error)
        assertEquals("Your card was declined", (result as Result.Error).message)
    }

    @Test
    fun `any other card succeeds with a confirmation`() = runTest {
        val result = repository.submitOrder("1 Main St", "4242424242424242")

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.orderId.isNotBlank())
    }

    @Test
    fun `each successful order gets a distinct id`() = runTest {
        val first = (repository.submitOrder("a", "4242") as Result.Success).data.orderId
        val second = (repository.submitOrder("a", "4242") as Result.Success).data.orderId

        assertNotEquals(first, second)
    }
}
