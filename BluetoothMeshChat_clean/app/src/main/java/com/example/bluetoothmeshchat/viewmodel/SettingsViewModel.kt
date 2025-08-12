package com.example.bluetoothmeshchat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bluetoothmeshchat.repo.IdentityRepository

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val identity = IdentityRepository(app)

    var displayName: String
        get() = identity.displayName
        private set(_) {}

    fun saveDisplayName(newName: String) {
        identity.displayName = newName.ifBlank { "User" }
        displayName = identity.displayName
    }
}