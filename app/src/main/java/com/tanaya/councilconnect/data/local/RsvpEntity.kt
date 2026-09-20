package com.tanaya.councilconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rsvp_queue")
data class RsvpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventId: String,
    val userId: String,
    val timestamp: Long,
    val synced: Boolean = false
)