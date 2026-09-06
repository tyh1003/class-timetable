package com.example.data.repository

import android.content.Context
import com.example.data.dao.TimetableDao
import com.example.data.model.Course
import com.example.data.model.CourseCategory
import com.example.data.model.DefaultData
import com.example.data.model.FontSizeScale
import com.example.data.model.Semester
import com.example.data.model.TimeSlot
import com.example.data.model.UserSettings
import com.example.widget.TimetableWidgetProvider
import kotlinx.coroutines.flow.Flow

class TimetableRepository(
    private val context: Context,
    private val dao: TimetableDao
) {
    val allSemesters: Flow<List<Semester>> = dao.getAllSemesters()
    val allTimeSlots: Flow<List<TimeSlot>> = dao.getAllTimeSlots()
    val allCategories: Flow<List<CourseCategory>> = dao.getAllCategories()
    val userSettings: Flow<UserSettings?> = dao.getUserSettings()

    fun getCoursesForSemester(semesterId: Long): Flow<List<Course>> {
        return dao.getCoursesForSemester(semesterId)
    }

    suspend fun ensureInitialized() {
        val existingSemesters = dao.getSemesterById(1)
        if (existingSemesters == null) {
            dao.insertSemesters(DefaultData.defaultSemesters)
        }
        val existingSlots = dao.getAllTimeSlotsSync()
        if (existingSlots.isEmpty()) {
            dao.insertTimeSlots(DefaultData.benchmarkSlots)
        }
        val settings = dao.getUserSettingsSync()
        if (settings == null) {
            dao.insertUserSettings(
                UserSettings(
                    id = 1,
                    showWeekend = false,
                    fontSizeScaleName = FontSizeScale.MEDIUM.name,
                    activeSemesterId = 1
                )
            )
        }
    }

    // --- Semesters ---
    suspend fun addSemester(name: String): Long {
        val newSemester = Semester(name = name.trim(), isCurrent = false)
        val id = dao.insertSemester(newSemester)
        notifyWidget()
        return id
    }

    suspend fun renameSemester(id: Long, newName: String) {
        val current = dao.getSemesterById(id) ?: return
        dao.updateSemester(current.copy(name = newName.trim()))
        notifyWidget()
    }

    suspend fun deleteSemester(semester: Semester) {
        dao.deleteCoursesBySemester(semester.id)
        dao.deleteSemester(semester)
        val settings = dao.getUserSettingsSync()
        if (settings?.activeSemesterId == semester.id) {
            // Find another semester or create default
            val remaining = dao.getSemesterById(1)
            val fallbackId = remaining?.id ?: 1L
            dao.updateUserSettings(settings.copy(activeSemesterId = fallbackId))
            dao.setActiveSemester(fallbackId)
        }
        notifyWidget()
    }

    suspend fun setActiveSemester(id: Long) {
        dao.setActiveSemester(id)
        val settings = dao.getUserSettingsSync() ?: UserSettings(id = 1)
        dao.updateUserSettings(settings.copy(activeSemesterId = id))
        notifyWidget()
    }

    // --- Time Slots ---
    suspend fun addTimeSlot(code: String, startTime: String, endTime: String) {
        val currentSlots = dao.getAllTimeSlotsSync()
        val nextOrder = (currentSlots.maxOfOrNull { it.orderIndex } ?: 0) + 1
        dao.insertTimeSlot(
            TimeSlot(
                code = code.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                orderIndex = nextOrder
            )
        )
        notifyWidget()
    }

    suspend fun updateTimeSlot(slot: TimeSlot) {
        dao.updateTimeSlot(slot)
        notifyWidget()
    }

    suspend fun deleteTimeSlot(slot: TimeSlot) {
        dao.deleteTimeSlot(slot)
        notifyWidget()
    }

    suspend fun resetToBenchmarkSlots() {
        dao.clearAllTimeSlots()
        dao.insertTimeSlots(DefaultData.benchmarkSlots)
        notifyWidget()
    }

    // --- Courses ---
    suspend fun saveCourse(course: Course): Long {
        val id = if (course.id == 0L) {
            dao.insertCourse(course)
        } else {
            dao.updateCourse(course)
            course.id
        }
        notifyWidget()
        return id
    }

    suspend fun toggleCourseCompleted(course: Course) {
        val updated = course.copy(isCompleted = !course.isCompleted)
        dao.updateCourse(updated)
        notifyWidget()
    }

    suspend fun setCourseCompleted(course: Course, completed: Boolean) {
        val updated = course.copy(isCompleted = completed)
        dao.updateCourse(updated)
        notifyWidget()
    }

    suspend fun deleteCourse(course: Course) {
        dao.deleteCourse(course)
        notifyWidget()
    }

    // --- Categories ---
    suspend fun addCategory(name: String, colorHex: String): Long {
        return dao.insertCategory(
            CourseCategory(
                name = name.trim(),
                colorHex = colorHex,
                isDefault = false
            )
        )
    }

    suspend fun deleteCategory(category: CourseCategory) {
        if (!category.isDefault) {
            dao.deleteCategory(category)
        }
    }

    // --- Settings ---
    suspend fun toggleShowWeekend(show: Boolean) {
        val current = dao.getUserSettingsSync() ?: UserSettings(id = 1)
        dao.updateUserSettings(current.copy(showWeekend = show))
    }

    suspend fun toggleFitToScreen(fit: Boolean) {
        val current = dao.getUserSettingsSync() ?: UserSettings(id = 1)
        dao.updateUserSettings(current.copy(fitToScreen = fit))
    }

    suspend fun updateFontSize(fontSizeScale: FontSizeScale) {
        val current = dao.getUserSettingsSync() ?: UserSettings(id = 1)
        dao.updateUserSettings(current.copy(fontSizeScaleName = fontSizeScale.name))
    }

    private fun notifyWidget() {
        try {
            TimetableWidgetProvider.updateAllWidgets(context)
        } catch (e: Exception) {
            // Non-blocking
        }
    }
}
