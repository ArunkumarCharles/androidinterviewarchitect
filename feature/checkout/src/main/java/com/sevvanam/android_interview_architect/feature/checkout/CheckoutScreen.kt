package com.sevvanam.android_interview_architect.feature.checkout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutRoute(
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Checkout Flow (MVI)") })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (val state = uiState) {
                is CheckoutUiState.AddressStep -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Step 1: Shipping Address")
                        OutlinedTextField(
                            value = state.address,
                            onValueChange = { viewModel.handleIntent(CheckoutIntent.EnterAddress(it)) },
                            label = { Text("Street Address") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.handleIntent(CheckoutIntent.ProceedToPayment) },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            enabled = state.address.isNotBlank()
                        ) {
                            Text("Next: Payment")
                        }
                    }
                }
                is CheckoutUiState.PaymentStep -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Step 2: Payment Details")
                        OutlinedTextField(
                            value = state.cardNumber,
                            onValueChange = { viewModel.handleIntent(CheckoutIntent.EnterPayment(it)) },
                            label = { Text("Credit Card Number") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.handleIntent(CheckoutIntent.SubmitOrder) },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            enabled = state.cardNumber.isNotBlank()
                        ) {
                            Text("Submit Order")
                        }
                    }
                }
                is CheckoutUiState.Processing -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CheckoutUiState.Success -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉 Order Placed Successfully!")
                        Text("Order ID: ${state.confirmation.orderId}")
                        Button(
                            onClick = { viewModel.handleIntent(CheckoutIntent.Reset) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Start New Order")
                        }
                    }
                }
                is CheckoutUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(
                            onClick = { viewModel.handleIntent(CheckoutIntent.Retry) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
