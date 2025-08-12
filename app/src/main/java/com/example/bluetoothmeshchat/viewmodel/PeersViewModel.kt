package com.example.bluetoothmeshchat.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothmeshchat.data.PeerEntity
import com.example.bluetoothmeshchat.repo.PeerRepository
import com.example.bluetoothmeshchat.service.BluetoothMeshService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PeersViewModel(app: Application) : AndroidViewModel(app) {
    private val peerRepo = PeerRepository(app)
    private val _peers = MutableStateFlow<List<PeerEntity>>(emptyList())

    fun observePeers(): List<PeerEntity> = _peers.value

    init {
        viewModelScope.launch {
            peerRepo.observePeers().collectLatest { list -> _peers.value = list }
        }
    }

    fun startScanning() {
        // Placeholder: you would implement Bluetooth discovery and update DB via service callbacks
        getApplication<Application>().startService(Intent(getApplication(), BluetoothMeshService::class.java))
        BluetoothAdapter.getDefaultAdapter()?.startDiscovery()
    }

    fun connect(deviceId: String) {
        // Actual connection is handled in the service; here we could send an intent
        getApplication<Application>().startService(Intent(getApplication(), BluetoothMeshService::class.java).apply {
            action = "CONNECT"
            putExtra("device", deviceId)
        })
    }

    fun disconnect(deviceId: String) {
        // Not implemented in this skeleton
    }
}