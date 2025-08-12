package com.example.bluetoothmeshchat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.bluetoothmeshchat.data.AppDatabase
import com.example.bluetoothmeshchat.data.MessageEntity
import com.example.bluetoothmeshchat.data.MessageStatus
import com.example.bluetoothmeshchat.data.PeerEntity
import com.example.bluetoothmeshchat.model.WireEnvelope
import com.example.bluetoothmeshchat.model.WireMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BluetoothMeshService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var serverSocket: BluetoothServerSocket? = null
    private val connectedSockets: MutableMap<String, BluetoothSocket> = ConcurrentHashMap()

    private val appUuid: UUID = UUID.fromString("3e1ef5b0-5c6f-4b7a-8f7c-4d6b9b3a8c2a")
    private val channelId = "mesh_service"

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device ?: return
                serviceScope.launch {
                    val db = AppDatabase.getInstance(applicationContext)
                    db.peerDao().upsert(
                        PeerEntity(
                            deviceId = device.address,
                            displayName = device.name ?: device.address,
                            isConnected = connectedSockets.containsKey(device.address),
                            lastSeen = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startServer()
        registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "SEND" -> {
                val payload = intent.getStringExtra("payload")
                if (payload != null) {
                    serviceScope.launch { forwardToPeers(payload, except = null) }
                }
            }
            "CONNECT" -> {
                val deviceId = intent.getStringExtra("device")
                val device = bluetoothAdapter?.bondedDevices?.firstOrNull { it.address == deviceId }
                if (device != null) connectTo(device)
            }
            "SCAN" -> {
                bluetoothAdapter?.startDiscovery()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try { serverSocket?.close() } catch (_: Throwable) {}
        connectedSockets.values.forEach { try { it.close() } catch (_: Throwable) {} }
        connectedSockets.clear()
        try { unregisterReceiver(discoveryReceiver) } catch (_: Throwable) {}
    }

    private fun startForegroundService() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Mesh Service", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bluetooth Mesh")
            .setContentText("Running")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
        startForeground(1, notification)
    }

    private fun startServer() {
        serviceScope.launch {
            val adapter = bluetoothAdapter ?: return@launch
            serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("MeshChat", appUuid)
            while (true) {
                val socket = try { serverSocket?.accept() } catch (_: Throwable) { null } ?: break
                val remoteAddress = socket.remoteDevice.address
                connectedSockets[remoteAddress] = socket
                handleConnection(socket)
            }
        }
    }

    fun connectTo(device: BluetoothDevice) {
        serviceScope.launch {
            try {
                val socket = device.createInsecureRfcommSocketToServiceRecord(appUuid)
                bluetoothAdapter?.cancelDiscovery()
                socket.connect()
                connectedSockets[device.address] = socket
                handleConnection(socket)
            } catch (_: Throwable) {
            }
        }
    }

    private fun handleConnection(socket: BluetoothSocket) {
        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val peerDao = db.peerDao()
            val messageDao = db.messageDao()
            val device = socket.remoteDevice
            peerDao.upsert(PeerEntity(device.address, device.name ?: device.address, true, System.currentTimeMillis()))

            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.outputStream)), true)

            val localMessages = messageDao.observeAll().first()
            localMessages.forEach { msg ->
                val envelope = WireEnvelope("chat", WireMessage(msg.id, msg.senderId, msg.senderDisplayName, msg.text, msg.timestamp))
                writer.println(Json.encodeToString(envelope))
            }

            while (true) {
                val line = try { reader.readLine() } catch (_: Throwable) { null } ?: break
                try {
                    val envelope = Json.decodeFromString<WireEnvelope>(line)
                    if (envelope.type == "chat") {
                        val m = envelope.message
                        val exists = messageDao.getById(m.id) != null
                        if (!exists) {
                            messageDao.upsert(
                                MessageEntity(
                                    id = m.id,
                                    senderId = m.senderId,
                                    senderDisplayName = m.senderDisplayName,
                                    text = m.text,
                                    timestamp = m.timestamp,
                                    status = MessageStatus.RECEIVED
                                )
                            )
                            forwardToPeers(line, except = socket.remoteDevice.address)
                        }
                    }
                } catch (_: Throwable) { }
            }

            connectedSockets.remove(device.address)
            peerDao.updateConnection(device.address, false, System.currentTimeMillis())
            try { socket.close() } catch (_: Throwable) {}
        }
    }

    fun sendChatMessage(envelope: WireEnvelope) {
        serviceScope.launch {
            val payload = Json.encodeToString(envelope)
            forwardToPeers(payload, except = null)
        }
    }

    private suspend fun forwardToPeers(payload: String, except: String?) {
        withContext(Dispatchers.IO) {
            connectedSockets.forEach { (addr, socket) ->
                if (except != null && except == addr) return@forEach
                try {
                    val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.outputStream)), true)
                    writer.println(payload)
                } catch (_: Throwable) { }
            }
        }
    }
}