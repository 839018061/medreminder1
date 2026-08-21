package com.example.medreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medreminder.data.dao.DrugDao
import com.example.medreminder.data.dao.RecordDao
import com.example.medreminder.data.dao.ScheduleDao
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug

@Database(
    entities = [Drug::class, DoseSchedule::class, AdherenceRecord::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** v1 -> v2：adherence_record 新增 snooze_count / taken_at 列，drug 新增 owner 列，保留历史数据 */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE adherence_record ADD COLUMN snooze_count INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE adherence_record ADD COLUMN taken_at INTEGER"
                )
                db.execSQL(
                    "ALTER TABLE drug ADD COLUMN owner TEXT NOT NULL DEFAULT '我'"
                )
                // 存量记录标记为已结束状态（保留原 TAKEN/LATE/MISSED），无 PENDING
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medication.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
