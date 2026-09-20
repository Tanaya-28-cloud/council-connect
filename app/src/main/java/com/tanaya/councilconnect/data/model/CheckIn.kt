package com.tanaya.councilconnect.data.model

data class CheckIn(
    val eventId: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)