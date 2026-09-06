package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Course
import com.example.data.model.CourseCategory
import com.example.data.model.Semester
import com.example.data.model.TimeSlot
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    // --- Semesters ---
    @Query("SELECT * FROM semesters ORDER BY orderIndex ASC, id ASC")
    fun getAllSemesters(): Flow<List<Semester>>

    @Query("SELECT * FROM semesters ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllSemestersSync(): List<Semester>

    @Query("SELECT * FROM semesters WHERE id = :id LIMIT 1")
    suspend fun getSemesterById(id: Long): Semester?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemesters(semesters: List<Semester>)

    @Update
    suspend fun updateSemester(semester: Semester)

    @Delete
    suspend fun deleteSemester(semester: Semester)

    @Query("UPDATE semesters SET isCurrent = CASE WHEN id = :activeId THEN 1 ELSE 0 END")
    suspend fun setActiveSemester(activeId: Long)

    // --- Time Slots ---
    @Query("SELECT * FROM time_slots ORDER BY orderIndex ASC, id ASC")
    fun getAllTimeSlots(): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllTimeSlotsSync(): List<TimeSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(slot: TimeSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlots(slots: List<TimeSlot>)

    @Update
    suspend fun updateTimeSlot(slot: TimeSlot)

    @Delete
    suspend fun deleteTimeSlot(slot: TimeSlot)

    @Query("DELETE FROM time_slots")
    suspend fun clearAllTimeSlots()

    // --- Courses ---
    @Query("SELECT * FROM courses WHERE semesterId = :semesterId")
    fun getCoursesForSemester(semesterId: Long): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE semesterId = :semesterId AND dayOfWeek = :dayOfWeek")
    suspend fun getCoursesForDaySync(semesterId: Long, dayOfWeek: Int): List<Course>

    @Query("SELECT * FROM courses WHERE semesterId = :semesterId")
    suspend fun getCoursesForSemesterSync(semesterId: Long): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("DELETE FROM courses WHERE semesterId = :semesterId")
    suspend fun deleteCoursesBySemester(semesterId: Long)

    // --- Categories ---
    @Query("SELECT * FROM course_categories ORDER BY isDefault DESC, id ASC")
    fun getAllCategories(): Flow<List<CourseCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CourseCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CourseCategory>)

    @Update
    suspend fun updateCategory(category: CourseCategory)

    @Delete
    suspend fun deleteCategory(category: CourseCategory)

    // --- Settings ---
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsSync(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettings)

    @Update
    suspend fun updateUserSettings(settings: UserSettings)
}
