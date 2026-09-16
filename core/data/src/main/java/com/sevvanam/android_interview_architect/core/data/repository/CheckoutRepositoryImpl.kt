package com.sevvanam.android_interview_architect.core.data.repository

import com.sevvanam.android_interview_architect.core.model.OrderConfirmation
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.repository.CheckoutRepository
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

/**
 * No real backend exists for this teaching sample, so order submission is simulated with a
 * network-like delay. The decline path is deterministic (cards ending "0000", mirroring Stripe's
 * test-card convention) rather than random, so callers can unit-test both outcomes reliably.
 */
class CheckoutRepositoryImpl @Inject constructor() : CheckoutRepository {

    override suspend fun submitOrder(address: String, cardNumber: String): Result<OrderConfirmation> {
        delay(1500)
        return if (cardNumber.endsWith("0000")) {
            Result.Error(IllegalStateException("Card declined"), "Your card was declined")
        } else {
            Result.Success(
                OrderConfirmation(
                    orderId = UUID.randomUUID().toString(),
                    confirmedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }
}
