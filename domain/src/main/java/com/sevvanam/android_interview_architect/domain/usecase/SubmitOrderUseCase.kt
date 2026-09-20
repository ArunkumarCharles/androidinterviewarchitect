package com.sevvanam.android_interview_architect.domain.usecase

import com.sevvanam.android_interview_architect.core.model.OrderConfirmation
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.repository.CheckoutRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * UseCase submitting the final checkout order once address and payment steps are complete.
 */
@ViewModelScoped
class SubmitOrderUseCase @Inject constructor(
    private val repository: CheckoutRepository
) {
    suspend operator fun invoke(address: String, cardNumber: String): Result<OrderConfirmation> {
        return repository.submitOrder(address, cardNumber)
    }
}
