package com.tanaya.councilconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tanaya.councilconnect.data.model.CheckIn
import com.tanaya.councilconnect.data.model.Event
import com.tanaya.councilconnect.data.model.Rsvp
import com.tanaya.councilconnect.data.repository.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

    val events: StateFlow<List<Event>> =
        repository.observeEvents()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun refreshEvents() {
        viewModelScope.launch {
            try {
                repository.syncPendingRsvps()
                repository.refreshEvents()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitRsvp(event: Event) {
        viewModelScope.launch {
            try {
                val userId =
                    FirebaseAuth.getInstance().currentUser?.uid
                        ?: "guest-user"

                val rsvp = Rsvp(
                    eventId = event.id,
                    userId = userId,
                    timestamp = System.currentTimeMillis(),
                    synced = true
                )

                repository.submitRsvp(rsvp)
                repository.refreshEvents()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitCheckIn(event: Event) {
        viewModelScope.launch {
            try {
                val userId =
                    FirebaseAuth.getInstance().currentUser?.uid
                        ?: "guest-user"

                val checkIn = CheckIn(
                    eventId = event.id,
                    userId = userId,
                    timestamp = System.currentTimeMillis()
                )

                repository.submitCheckIn(checkIn)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}