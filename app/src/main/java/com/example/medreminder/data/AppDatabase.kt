package com.example.medreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.medreminder.data.dao.DrugDao
import com.example.medreminder.data.dao.RecordDao
import com.example.medreminder.data.dao.ScheduleDao
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import com.example.medreminder.reminder.AlarmScheduler

@Database(
    entities = [Drug::class, DoseSchedule::class, AdherenceRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medication_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Schedules stored as JSON string in a dedicated table
class Converters {
    @androidx.room.TypeConverter
    fun fromStringList(value: String): List<String> = value.split("||")

    @androidx.room.TypeConverter
    fun toStringList(list: List<String>): String = list.joinToString("||")
}
