package com.example.bluetoothmeshchat.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothmeshchat.data.PeerEntity
import com.example.bluetoothmeshchat.repo.PeerRepository
import com.example.bluetoothmeshchat.service.BluetoothMeshService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PeersViewModel(app: Application) : AndroidViewModel(app) {
    private val peerRepo = PeerRepository(app)
    private val _peers = MutableStateFlow<List<PeerEntity>>(emptyList())
    val peers: StateFlow<List<PeerEntity>> = _peers.asStateFlow()

    init {
        viewModelScope.launch {
            peerRepo.observePeers().collectLatest { list -> _peers.value = list }
        }
    }

    fun startScanning() {
        getApplication<Application>().startService(Intent(getApplication(), BluetoothMeshService::class.java).apply {
            action = "SCAN"
        })
    }

    fun connect(deviceId: String) {
        getApplication<Application>().startService(Intent(getApplication(), BluetoothMeshService::class.java).apply {
            action = "CONNECT"
            putExtra("device", deviceId)
        })
    }

    fun disconnect(deviceId: String) {
        // Not implemented in this skeleton
    }
}