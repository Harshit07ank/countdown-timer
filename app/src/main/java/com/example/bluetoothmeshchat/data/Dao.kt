package com.example.bluetoothmeshchat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: MessageStatus)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MessageEntity?
}

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY display_name ASC")
    fun observeAll(): Flow<List<PeerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(peer: PeerEntity)

    @Query("UPDATE peers SET is_connected = :isConnected, last_seen = :lastSeen WHERE deviceId = :deviceId")
    suspend fun updateConnection(deviceId: String, isConnected: Boolean, lastSeen: Long)
}