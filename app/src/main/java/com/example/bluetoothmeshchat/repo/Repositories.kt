package com.example.bluetoothmeshchat.repo

import android.content.Context
import android.content.SharedPreferences
import com.example.bluetoothmeshchat.data.AppDatabase
import com.example.bluetoothmeshchat.data.MessageEntity
import com.example.bluetoothmeshchat.data.MessageStatus
import com.example.bluetoothmeshchat.data.PeerEntity
import com.example.bluetoothmeshchat.model.WireEnvelope
import com.example.bluetoothmeshchat.model.WireMessage
import com.example.bluetoothmeshchat.service.BluetoothMeshService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class IdentityRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("identity", Context.MODE_PRIVATE)

    val deviceId: String by lazy {
        prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("device_id", it).apply()
        }
    }

    var displayName: String
        get() = prefs.getString("display_name", "User") ?: "User"
        set(value) { prefs.edit().putString("display_name", value).apply() }
}

class MessageRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)

    fun observeMessages(): Flow<List<MessageEntity>> = db.messageDao().observeAll()

    suspend fun insertOutgoing(text: String, identity: IdentityRepository): MessageEntity {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = identity.deviceId,
            senderDisplayName = identity.displayName,
            text = text,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        db.messageDao().upsert(message)
        return message
    }
}

class PeerRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    fun observePeers(): Flow<List<PeerEntity>> = db.peerDao().observeAll()
}

class MeshRepository(private val context: Context) {
    fun toEnvelope(message: MessageEntity): WireEnvelope = WireEnvelope(
        type = "chat",
        message = WireMessage(
            id = message.id,
            senderId = message.senderId,
            senderDisplayName = message.senderDisplayName,
            text = message.text,
            timestamp = message.timestamp
        )
    )
}