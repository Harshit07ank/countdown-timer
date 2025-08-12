# Keep Kotlin serialization models and Room entities
-keep class kotlinx.** { *; }
-keepclassmembers class **Kt { *; }
-keep class androidx.room.** { *; }
-dontwarn kotlinx.serialization.**