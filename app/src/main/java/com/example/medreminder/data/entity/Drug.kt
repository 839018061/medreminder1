package com.example.medreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drugs")
data class Drug(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String = "",
    val note: String = ""
)
