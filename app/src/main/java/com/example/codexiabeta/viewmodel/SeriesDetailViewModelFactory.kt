package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SeriesDetailViewModelFactory(
    private val application: Application,
    private val seriesId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SeriesDetailViewModel::class.java)) {
            return SeriesDetailViewModel(application, seriesId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
