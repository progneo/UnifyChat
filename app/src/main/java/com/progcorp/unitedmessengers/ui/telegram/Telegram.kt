package com.progcorp.unitedmessengers.ui.telegram

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Telegram(viewModel: TelegramViewModel = viewModel()) {
    Button(onClick = {}) {
        Text("telegram")
    }
}