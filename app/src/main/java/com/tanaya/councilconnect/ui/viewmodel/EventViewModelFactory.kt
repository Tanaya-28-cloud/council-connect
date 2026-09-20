package com.tanaya.councilconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tanaya.councilconnect.data.repository.EventRepository

class EventViewModelFactory(
    private val repository: EventRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}