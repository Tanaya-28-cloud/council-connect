package com.tanaya.councilconnect.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startAt: Long = 0L,
    val rsvpCount: Int = 0
)