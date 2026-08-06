package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddEditSeriesViewModelFactory(
    private val application: Application,
    private val seriesId: String? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditSeriesViewModel::class.java)) {
            return AddEditSeriesViewModel(application, seriesId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
