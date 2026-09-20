package com.tanaya.councilconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val location: String,
    val startAt: Long,
    val rsvpCount: Int
)