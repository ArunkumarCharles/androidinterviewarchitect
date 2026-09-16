package com.sevvanam.android_interview_architect.domain.repository

import com.sevvanam.android_interview_architect.core.model.OrderConfirmation
import com.sevvanam.android_interview_architect.core.model.Result

/**
 * Domain repository interface for submitting checkout orders, adhering to Clean Architecture
 * Dependency Inversion (feature module depends on this abstraction, not the :core:data impl).
 */
interface CheckoutRepository {
    suspend fun submitOrder(address: String, cardNumber: String): Result<OrderConfirmation>
}
