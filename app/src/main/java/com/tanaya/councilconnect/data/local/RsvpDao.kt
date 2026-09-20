package com.tanaya.councilconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RsvpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rsvp: RsvpEntity)

    @Query("SELECT * FROM rsvp_queue WHERE synced = 0")
    suspend fun getPendingRsvps(): List<RsvpEntity>

    @Query("UPDATE rsvp_queue SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}