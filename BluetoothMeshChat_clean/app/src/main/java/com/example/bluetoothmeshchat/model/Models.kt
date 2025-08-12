package com.example.bluetoothmeshchat.model

import kotlinx.serialization.Serializable

@Serializable
data class WireMessage(
    val id: String,
    val senderId: String,
    val senderDisplayName: String,
    val text: String,
    val timestamp: Long
)

@Serializable
data class WireEnvelope(
    val type: String, // e.g., "chat"
    val message: WireMessage
)