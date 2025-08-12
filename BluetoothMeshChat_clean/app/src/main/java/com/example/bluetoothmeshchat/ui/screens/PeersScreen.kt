package com.example.bluetoothmeshchat.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetoothmeshchat.viewmodel.PeersViewModel

@Composable
fun PeersScreen(viewModel: PeersViewModel = viewModel()) {
    val peers by viewModel.peers.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(Modifier.fillMaxWidth()) {
            items(peers) { peer ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(text = "${'$'}{peer.displayName} (${ '$'}{peer.deviceId.take(6)})", modifier = Modifier.weight(1f))
                    if (peer.isConnected) {
                        Button(onClick = { viewModel.disconnect(peer.deviceId) }) { Text("Disconnect") }
                    } else {
                        Button(onClick = { viewModel.connect(peer.deviceId) }) { Text("Connect") }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.startScanning() }) { Text("Scan") }
        }
    }
}