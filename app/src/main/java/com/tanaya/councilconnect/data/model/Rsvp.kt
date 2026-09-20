package com.tanaya.councilconnect.data.model

data class Rsvp(
    val eventId: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)