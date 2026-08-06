package com.example.codexiabeta.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import com.example.codexiabeta.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val backupRepository = app.backupRepository

    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf<String?>(null)

    fun exportLibrary(uri: Uri) {
        viewModelScope.launch {
            isLoading = true

            // Show an ongoing "in-progress" notification with the app icon
            NotificationHelper.showBackupNotification(
                context = app,
                notificationId = NotificationHelper.ID_EXPORT,
                title = "Codexia – Exporting Library",
                message = "Saving your library as a CSV file…",
                isOngoing = true
            )

            val userName = app.userPreferences.userName.first()
            val result = backupRepository.exportToCsv(uri, app, userName)

            // Replace with result notification
            if (result.isSuccess) {
                NotificationHelper.showBackupNotification(
                    context = app,
                    notificationId = NotificationHelper.ID_EXPORT,
                    title = "Codexia – Export Complete",
                    message = "Your library was exported successfully.",
                    isOngoing = false
                )
                statusMessage = "Library exported successfully!"
            } else {
                NotificationHelper.showBackupNotification(
                    context = app,
                    notificationId = NotificationHelper.ID_EXPORT,
                    title = "Codexia – Export Failed",
                    message = result.exceptionOrNull()?.message ?: "An unknown error occurred.",
                    isOngoing = false
                )
                statusMessage = "Export failed: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }

    fun importLibrary(uri: Uri) {
        viewModelScope.launch {
            isLoading = true

            // Show an ongoing "in-progress" notification with the app icon
            NotificationHelper.showBackupNotification(
                context = app,
                notificationId = NotificationHelper.ID_IMPORT,
                title = "Codexia – Importing Library",
                message = "Restoring your library from CSV…",
                isOngoing = true
            )

            val result = backupRepository.importFromCsv(uri, app) { importedName ->
                app.userPreferences.setUserName(importedName)
            }

            // Replace with result notification
            if (result.isSuccess) {
                NotificationHelper.showBackupNotification(
                    context = app,
                    notificationId = NotificationHelper.ID_IMPORT,
                    title = "Codexia – Import Complete",
                    message = "Your library was imported successfully.",
                    isOngoing = false
                )
                statusMessage = "Library imported successfully!"
            } else {
                NotificationHelper.showBackupNotification(
                    context = app,
                    notificationId = NotificationHelper.ID_IMPORT,
                    title = "Codexia – Import Failed",
                    message = result.exceptionOrNull()?.message ?: "An unknown error occurred.",
                    isOngoing = false
                )
                statusMessage = "Import failed: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }
    
    fun clearStatusMessage() {
        statusMessage = null
    }
}
