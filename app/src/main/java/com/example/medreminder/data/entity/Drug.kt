package com.example.medreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 药品 */
@Entity(tableName = "drug")
data class Drug(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,          // 药品名
    val dosage: String,        // 单次剂量，如 "1片"、"5ml"
    val remark: String = "",   // 备注（饭前/饭后等补充）
    val owner: String = "我",  // 用药人（本地多用药人）
    val active: Boolean = true
)
