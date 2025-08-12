package com.example.bluetoothmeshchat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    @ColumnInfo(name = "sender_display_name") val senderDisplayName: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "status") val status: MessageStatus,
)

enum class MessageStatus { SENT, RECEIVED, FORWARDED }

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val deviceId: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "is_connected") val isConnected: Boolean = false,
    @ColumnInfo(name = "last_seen") val lastSeen: Long = 0L
)