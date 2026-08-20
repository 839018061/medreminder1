package com.example.medreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adherence_records")
data class AdherenceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drugId: Long,
    val date: String,   // yyyy-MM-dd
    val taken: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
