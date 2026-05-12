package com.example.codexiabeta.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val backupRepository = app.backupRepository

    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf<String?>(null)

    fun exportLibrary(uri: Uri) {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Exporting library... This may take a moment."
            val result = backupRepository.exportToCsv(uri, app)
            
            if (result.isSuccess) {
                statusMessage = "Library exported successfully!"
            } else {
                statusMessage = "Export failed: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }

    fun importLibrary(uri: Uri) {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Importing library... Please wait."
            val result = backupRepository.importFromCsv(uri, app)
            
            if (result.isSuccess) {
                statusMessage = "Library imported successfully!"
            } else {
                statusMessage = "Import failed: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }
    
    fun clearStatusMessage() {
        statusMessage = null
    }
}
