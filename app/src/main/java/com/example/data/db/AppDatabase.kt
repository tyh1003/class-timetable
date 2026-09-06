package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.TimetableDao
import com.example.data.model.Course
import com.example.data.model.CourseCategory
import com.example.data.model.DefaultData
import com.example.data.model.Semester
import com.example.data.model.TimeSlot
import com.example.data.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Semester::class,
        TimeSlot::class,
        CourseCategory::class,
        Course::class,
        UserSettings::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timetable_database.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database.timetableDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: TimetableDao) {
            dao.insertSemesters(DefaultData.defaultSemesters)
            dao.insertTimeSlots(DefaultData.benchmarkSlots)
            dao.insertCategories(DefaultData.defaultCategories)
            dao.insertUserSettings(
                UserSettings(
                    id = 1,
                    showWeekend = false,
                    fontSizeScaleName = "MEDIUM",
                    activeSemesterId = 1
                )
            )
        }
    }
}
