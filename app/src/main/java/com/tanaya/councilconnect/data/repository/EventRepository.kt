package com.tanaya.councilconnect.data.repository

import com.tanaya.councilconnect.data.local.EventDao
import com.tanaya.councilconnect.data.local.EventEntity
import com.tanaya.councilconnect.data.local.RsvpDao
import com.tanaya.councilconnect.data.local.RsvpEntity
import com.tanaya.councilconnect.data.model.CheckIn
import com.tanaya.councilconnect.data.model.Event
import com.tanaya.councilconnect.data.model.Rsvp
import com.tanaya.councilconnect.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepository(
    private val eventDao: EventDao,
    private val rsvpDao: RsvpDao,
    private val firestoreRepository: FirestoreRepository
) {

    fun observeEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { entity ->
                Event(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    location = entity.location,
                    startAt = entity.startAt,
                    rsvpCount = entity.rsvpCount
                )
            }
        }
    }

    suspend fun refreshEvents() {
        val remoteEvents = firestoreRepository.fetchEvents()

        val entities = remoteEvents.map { event ->
            EventEntity(
                id = event.id,
                title = event.title,
                description = event.description,
                location = event.location,
                startAt = event.startAt,
                rsvpCount = event.rsvpCount
            )
        }

        eventDao.clearAll()
        eventDao.insertAll(entities)
    }

    suspend fun submitRsvp(rsvp: Rsvp) {
        try {
            // Try Firebase first.
            firestoreRepository.submitRsvp(rsvp)
        } catch (e: Exception) {
            // If Firebase fails, save the RSVP locally.
            rsvpDao.insert(
                RsvpEntity(
                    eventId = rsvp.eventId,
                    userId = rsvp.userId,
                    timestamp = rsvp.timestamp,
                    synced = false
                )
            )
        }
    }

    suspend fun submitCheckIn(checkIn: CheckIn) {
        firestoreRepository.submitCheckIn(checkIn)
    }

    suspend fun syncPendingRsvps() {
        val pendingRsvps = rsvpDao.getPendingRsvps()

        for (pending in pendingRsvps) {
            try {
                firestoreRepository.submitRsvp(
                    Rsvp(
                        eventId = pending.eventId,
                        userId = pending.userId,
                        timestamp = pending.timestamp,
                        synced = true
                    )
                )

                rsvpDao.markAsSynced(pending.id)

            } catch (e: Exception) {
                // Stop if the network is still unavailable.
                break
            }
        }
    }
}