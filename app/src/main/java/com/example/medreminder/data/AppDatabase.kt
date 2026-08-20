package com.example.medreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.medreminder.data.dao.DrugDao
import com.example.medreminder.data.dao.RecordDao
import com.example.medreminder.data.dao.ScheduleDao
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug

@Database(
    entities = [Drug::class, DoseSchedule::class, AdherenceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medication.db"
                ).build().also { INSTANCE = it }
            }
    }
}
