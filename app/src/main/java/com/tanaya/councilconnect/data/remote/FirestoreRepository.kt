package com.tanaya.councilconnect.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.tanaya.councilconnect.data.model.CheckIn
import com.tanaya.councilconnect.data.model.Event
import com.tanaya.councilconnect.data.model.Rsvp
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    private val eventsCollection = db.collection("events")
    private val rsvpsCollection = db.collection("rsvps")
    private val checkInsCollection = db.collection("checkIns")

    suspend fun fetchEvents(): List<Event> {
        val snapshot = eventsCollection.get().await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject<Event>()?.copy(
                id = document.id
            )
        }
    }

    suspend fun submitRsvp(rsvp: Rsvp) {
        rsvpsCollection.add(rsvp).await()

        eventsCollection
            .document(rsvp.eventId)
            .update(
                "rsvpCount",
                com.google.firebase.firestore.FieldValue.increment(1)
            )
            .await()
    }

    suspend fun submitCheckIn(checkIn: CheckIn) {
        checkInsCollection.add(checkIn).await()
    }
}