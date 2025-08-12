package com.example.bluetoothmeshchat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetoothmeshchat.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages = viewModel.observeMessages()
    var input by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(Modifier.weight(1f)) {
            items(messages) { msg ->
                Text(text = "${'$'}{msg.senderDisplayName}: ${'$'}{msg.text} (${ '$'}{msg.status})")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            Button(onClick = {
                if (input.isNotBlank()) {
                    viewModel.sendMessage(input.trim())
                    input = ""
                }
            }) { Text("Send") }
        }
    }
}