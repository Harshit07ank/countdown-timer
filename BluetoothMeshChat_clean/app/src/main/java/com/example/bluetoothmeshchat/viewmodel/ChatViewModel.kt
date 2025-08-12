package com.example.bluetoothmeshchat.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothmeshchat.data.MessageEntity
import com.example.bluetoothmeshchat.repo.IdentityRepository
import com.example.bluetoothmeshchat.repo.MeshRepository
import com.example.bluetoothmeshchat.repo.MessageRepository
import com.example.bluetoothmeshchat.service.BluetoothMeshService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val identity = IdentityRepository(app)
    private val messagesRepo = MessageRepository(app)
    private val meshRepo = MeshRepository(app)

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            messagesRepo.observeMessages().collectLatest { list ->
                _messages.value = list
            }
        }
        app.startService(Intent(app, BluetoothMeshService::class.java))
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val saved = messagesRepo.insertOutgoing(text, identity)
            val env = meshRepo.toEnvelope(saved)
            appContext().startService(Intent(appContext(), BluetoothMeshService::class.java).apply {
                action = "SEND"
                putExtra("payload", kotlinx.serialization.json.Json.encodeToString(com.example.bluetoothmeshchat.model.WireEnvelope.serializer(), env))
            })
        }
    }

    private fun appContext() = getApplication<Application>()
}