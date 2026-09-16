package com.sevvanam.android_interview_architect.feature.checkout

import androidx.lifecycle.SavedStateHandle
import com.sevvanam.android_interview_architect.core.model.OrderConfirmation
import com.sevvanam.android_interview_architect.core.model.Result
import com.sevvanam.android_interview_architect.domain.usecase.SubmitOrderUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private val submitOrderUseCase: SubmitOrderUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        CheckoutViewModel(submitOrderUseCase, savedStateHandle)

    @Test
    fun `full step state machine reaches Success`() = runTest {
        val confirmation = OrderConfirmation(orderId = "order-1", confirmedAtEpochMillis = 0L)
        coEvery { submitOrderUseCase("123 Main St", "4111111111111111") } returns Result.Success(confirmation)

        val vm = viewModel()
        assertTrue(vm.uiState.value is CheckoutUiState.AddressStep)

        vm.handleIntent(CheckoutIntent.EnterAddress("123 Main St"))
        vm.handleIntent(CheckoutIntent.ProceedToPayment)
        assertTrue(vm.uiState.value is CheckoutUiState.PaymentStep)

        vm.handleIntent(CheckoutIntent.EnterPayment("4111111111111111"))
        vm.handleIntent(CheckoutIntent.SubmitOrder)
        assertTrue(vm.uiState.value is CheckoutUiState.Processing)

        advanceUntilIdle()

        val finalState = vm.uiState.value
        assertTrue(finalState is CheckoutUiState.Success)
        assertEquals(confirmation, (finalState as CheckoutUiState.Success).confirmation)
    }

    @Test
    fun `declined order surfaces Error and Retry returns to PaymentStep`() = runTest {
        coEvery { submitOrderUseCase(any(), "0000000000000000") } returns
            Result.Error(IllegalStateException("declined"), "Your card was declined")

        val vm = viewModel()
        vm.handleIntent(CheckoutIntent.EnterAddress("123 Main St"))
        vm.handleIntent(CheckoutIntent.ProceedToPayment)
        vm.handleIntent(CheckoutIntent.EnterPayment("0000000000000000"))
        vm.handleIntent(CheckoutIntent.SubmitOrder)
        advanceUntilIdle()

        val errorState = vm.uiState.value
        assertTrue(errorState is CheckoutUiState.Error)
        assertEquals("Your card was declined", (errorState as CheckoutUiState.Error).message)

        vm.handleIntent(CheckoutIntent.Retry)
        val retriedState = vm.uiState.value
        assertTrue(retriedState is CheckoutUiState.PaymentStep)
        assertEquals("0000000000000000", (retriedState as CheckoutUiState.PaymentStep).cardNumber)
    }

    @Test
    fun `in-progress input survives process death via SavedStateHandle`() {
        val savedStateHandle = SavedStateHandle()
        val firstInstance = viewModel(savedStateHandle)

        firstInstance.handleIntent(CheckoutIntent.EnterAddress("456 Oak Ave"))
        firstInstance.handleIntent(CheckoutIntent.ProceedToPayment)
        firstInstance.handleIntent(CheckoutIntent.EnterPayment("5555444433332222"))

        // Simulate process death: a brand new ViewModel instance restored from the same
        // SavedStateHandle (this is exactly what the framework does on process-death recreation).
        val restoredInstance = viewModel(savedStateHandle)

        val restoredState = restoredInstance.uiState.value
        assertTrue(restoredState is CheckoutUiState.PaymentStep)
        assertEquals("5555444433332222", (restoredState as CheckoutUiState.PaymentStep).cardNumber)
    }
}
