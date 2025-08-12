# BluetoothMeshChat (Offline-first Bluetooth Mesh Chat)

An Android app demonstrating offline-first, peer-to-peer chat over Bluetooth Classic (RFCOMM) with simple gossip-style forwarding and local persistence via Room.

## Features
- Peer discovery and connections over Bluetooth Classic
- Gossip-style message forwarding across multiple hops
- Local Room database for messages and peers
- Compose UI with Chat and Peers screens

## Build
This repo includes Gradle configuration. If the Gradle wrapper jar is missing, generate it locally:

```bash
# Install Gradle (if needed) and generate wrapper
gradle wrapper --gradle-version 8.7
./gradlew assembleDebug
```

Open the project in Android Studio (Giraffe+ recommended), sync, and run on a device. Ensure Bluetooth and location permissions are granted.

## Permissions
On Android 12+, BLUETOOTH_CONNECT/SCAN are required. On older versions, location is required for discovery.

## Notes
- The Bluetooth mesh is best-effort and simplified. It uses insecure RFCOMM sockets for ease of pairing in a demo.
- For production use, add proper trust/auth, retries, backoff, and queue management.