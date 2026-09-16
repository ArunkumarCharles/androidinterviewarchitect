package com.sevvanam.android_interview_architect.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a successfully submitted checkout order.
 */
@Serializable
data class OrderConfirmation(
    val orderId: String,
    val confirmedAtEpochMillis: Long
)
