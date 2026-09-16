package com.sevvanam.android_interview_architect.feature.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevvanam.android_interview_architect.core.model.OrderConfirmation
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.usecase.SubmitOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI architecture for multi-step Checkout flow.
 * Demonstrates deterministic state machine transitions for complex ecommerce/workflow UI.
 */
sealed interface CheckoutUiState {
    data class AddressStep(val address: String = "") : CheckoutUiState
    data class PaymentStep(val cardNumber: String = "") : CheckoutUiState
    object Processing : CheckoutUiState
    data class Success(val confirmation: OrderConfirmation) : CheckoutUiState
    data class Error(val message: String, val cardNumber: String) : CheckoutUiState
}

sealed interface CheckoutIntent {
    data class EnterAddress(val address: String) : CheckoutIntent
    data class EnterPayment(val cardNumber: String) : CheckoutIntent
    object ProceedToPayment : CheckoutIntent
    object SubmitOrder : CheckoutIntent
    object Retry : CheckoutIntent
    object Reset : CheckoutIntent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val submitOrderUseCase: SubmitOrderUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private companion object {
        const val KEY_STEP = "checkout_step"
        const val KEY_ADDRESS = "checkout_address"
        const val KEY_CARD_NUMBER = "checkout_card_number"
        const val STEP_PAYMENT = "PAYMENT"
    }

    // Restoring from SavedStateHandle means a process death mid-checkout (e.g. the OS reclaiming
    // memory while the user is backgrounded on the payment step) doesn't lose their in-progress
    // address/card input -- the exact scenario onSaveInstanceState/SavedStateHandle exists for.
    private val _uiState = MutableStateFlow(restoreInitialState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private fun restoreInitialState(): CheckoutUiState {
        val address = savedStateHandle.get<String>(KEY_ADDRESS) ?: ""
        val cardNumber = savedStateHandle.get<String>(KEY_CARD_NUMBER) ?: ""
        return if (savedStateHandle.get<String>(KEY_STEP) == STEP_PAYMENT) {
            CheckoutUiState.PaymentStep(cardNumber)
        } else {
            CheckoutUiState.AddressStep(address)
        }
    }

    fun handleIntent(intent: CheckoutIntent) {
        when (intent) {
            is CheckoutIntent.EnterAddress -> {
                val current = _uiState.value
                if (current is CheckoutUiState.AddressStep) {
                    savedStateHandle[KEY_ADDRESS] = intent.address
                    _uiState.value = current.copy(address = intent.address)
                }
            }
            is CheckoutIntent.ProceedToPayment -> {
                savedStateHandle[KEY_STEP] = STEP_PAYMENT
                _uiState.value = CheckoutUiState.PaymentStep(
                    cardNumber = savedStateHandle.get<String>(KEY_CARD_NUMBER) ?: ""
                )
            }
            is CheckoutIntent.EnterPayment -> {
                val current = _uiState.value
                if (current is CheckoutUiState.PaymentStep) {
                    savedStateHandle[KEY_CARD_NUMBER] = intent.cardNumber
                    _uiState.value = current.copy(cardNumber = intent.cardNumber)
                }
            }
            is CheckoutIntent.SubmitOrder -> submitOrder()
            is CheckoutIntent.Retry -> {
                val current = _uiState.value
                if (current is CheckoutUiState.Error) {
                    _uiState.value = CheckoutUiState.PaymentStep(current.cardNumber)
                }
            }
            is CheckoutIntent.Reset -> {
                clearSavedState()
                _uiState.value = CheckoutUiState.AddressStep()
            }
        }
    }

    private fun submitOrder() {
        val current = _uiState.value
        if (current !is CheckoutUiState.PaymentStep) return
        val address = savedStateHandle.get<String>(KEY_ADDRESS) ?: ""
        val cardNumber = current.cardNumber

        _uiState.value = CheckoutUiState.Processing
        viewModelScope.launch {
            when (val result = submitOrderUseCase(address, cardNumber)) {
                is Result.Success -> {
                    clearSavedState()
                    _uiState.value = CheckoutUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = CheckoutUiState.Error(
                        message = result.message ?: "Order submission failed",
                        cardNumber = cardNumber
                    )
                }
                // CheckoutRepository never emits Loading (it's a single suspend call, not a Flow);
                // this branch only exists because Result<T> requires exhaustive handling.
                is Result.Loading -> Unit
            }
        }
    }

    private fun clearSavedState() {
        savedStateHandle.remove<String>(KEY_STEP)
        savedStateHandle.remove<String>(KEY_ADDRESS)
        savedStateHandle.remove<String>(KEY_CARD_NUMBER)
    }
}
