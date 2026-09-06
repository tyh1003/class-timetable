package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Course
import com.example.data.model.CourseCategory
import com.example.data.model.FontSizeScale
import com.example.data.model.Semester
import com.example.data.model.TimeSlot
import com.example.data.model.UserSettings
import com.example.data.repository.TimetableRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TimetableUiState(
    val semesters: List<Semester> = emptyList(),
    val activeSemester: Semester? = null,
    val timeSlots: List<TimeSlot> = emptyList(),
    val courses: List<Course> = emptyList(),
    val categories: List<CourseCategory> = emptyList(),
    val showWeekend: Boolean = false,
    val fontSizeScale: FontSizeScale = FontSizeScale.MEDIUM,
    val fitToScreen: Boolean = true, // 集中單一畫面，不上下左右滑
    val isLoading: Boolean = true
)

sealed interface ActiveModal {
    object None : ActiveModal
    data class AddEditCourse(
        val course: Course? = null,
        val prefilledDay: Int = 1,
        val prefilledSlotCode: String = "1",
        val initialSelectedSlots: Set<String> = emptySet()
    ) : ActiveModal
    data class CourseDetail(val course: Course) : ActiveModal
    object SemesterManager : ActiveModal
    object TimeSlotManager : ActiveModal
    object CategoryManager : ActiveModal
    object FontSizePicker : ActiveModal
}

class TimetableViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimetableRepository

    private val _activeModal = MutableStateFlow<ActiveModal>(ActiveModal.None)
    val activeModal: StateFlow<ActiveModal> = _activeModal.asStateFlow()

    private val _activeSemesterId = MutableStateFlow<Long>(1L)

    // Direct Grid Selection Mode (直接點選課表畫面排課)
    private val _isGridSelectionMode = MutableStateFlow(false)
    val isGridSelectionMode: StateFlow<Boolean> = _isGridSelectionMode.asStateFlow()

    private val _selectedGridSlots = MutableStateFlow<Set<String>>(emptySet())
    val selectedGridSlots: StateFlow<Set<String>> = _selectedGridSlots.asStateFlow()

    private var editingCourseInGridMode: Course? = null

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = TimetableRepository(application, db.timetableDao())

        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TimetableUiState> = combine(
        repository.allSemesters,
        repository.allTimeSlots,
        repository.allCategories,
        repository.userSettings,
        _activeSemesterId
    ) { semesters, timeSlots, categories, settings, manualSemesterId ->
        val activeId = if (manualSemesterId > 0 && semesters.any { it.id == manualSemesterId }) {
            manualSemesterId
        } else {
            settings?.activeSemesterId ?: semesters.firstOrNull { it.isCurrent }?.id ?: semesters.firstOrNull()?.id ?: 1L
        }
        val activeSem = semesters.firstOrNull { it.id == activeId } ?: semesters.firstOrNull()
        val weekend = settings?.showWeekend ?: false
        val font = settings?.fontSizeScale ?: FontSizeScale.MEDIUM
        val fit = settings?.fitToScreen ?: true

        Tuple5(semesters, activeSem, timeSlots, categories, Quadruple(weekend, font, activeId, fit))
    }.flatMapLatest { tuple ->
        val (semesters, activeSem, timeSlots, categories, settingsQuad) = tuple
        val (weekend, font, activeId, fit) = settingsQuad

        repository.getCoursesForSemester(activeId).combine(MutableStateFlow(Unit)) { courses, _ ->
            TimetableUiState(
                semesters = semesters,
                activeSemester = activeSem,
                timeSlots = timeSlots,
                courses = courses,
                categories = categories,
                showWeekend = weekend,
                fontSizeScale = font,
                fitToScreen = fit,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimetableUiState()
    )

    fun openModal(modal: ActiveModal) {
        _activeModal.value = modal
    }

    fun closeModal() {
        _activeModal.value = ActiveModal.None
    }

    fun startGridSelectionMode(courseToEdit: Course? = null, initialSlots: Set<String> = emptySet()) {
        editingCourseInGridMode = courseToEdit
        val slots = if (courseToEdit != null && initialSlots.isEmpty()) {
            val slotOrderMap = uiState.value.timeSlots.associateBy({ it.code }, { it.orderIndex })
            courseToEdit.getAssignedSlotKeys(slotOrderMap, uiState.value.timeSlots)
        } else {
            initialSlots
        }
        _selectedGridSlots.value = slots
        _isGridSelectionMode.value = true
        _activeModal.value = ActiveModal.None
    }

    fun toggleGridSlot(day: Int, slotCode: String) {
        val key = "$day:$slotCode"
        val current = _selectedGridSlots.value.toMutableSet()
        if (key in current) {
            current.remove(key)
        } else {
            current.add(key)
        }
        _selectedGridSlots.value = current
    }

    fun clearGridSelection() {
        _selectedGridSlots.value = emptySet()
    }

    fun cancelGridSelectionMode() {
        _isGridSelectionMode.value = false
        _selectedGridSlots.value = emptySet()
        editingCourseInGridMode = null
    }

    fun finishGridSelectionAndOpenCourseForm() {
        val slots = _selectedGridSlots.value
        val course = editingCourseInGridMode
        _isGridSelectionMode.value = false
        _selectedGridSlots.value = emptySet()
        editingCourseInGridMode = null

        val firstSlotKey = slots.firstOrNull() ?: "1:1"
        val parts = firstSlotKey.split(":")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val code = parts.getOrNull(1) ?: "1"

        _activeModal.value = ActiveModal.AddEditCourse(
            course = course,
            prefilledDay = day,
            prefilledSlotCode = code,
            initialSelectedSlots = slots
        )
    }

    fun onCellClicked(dayOfWeek: Int, slotCode: String) {
        if (_isGridSelectionMode.value) {
            toggleGridSlot(dayOfWeek, slotCode)
            return
        }

        val currentCourses = uiState.value.courses
        val slots = uiState.value.timeSlots
        val slotOrderMap = slots.associateBy({ it.code }, { it.orderIndex })

        // Find if a course occupies this day and slot
        val occupyingCourse = currentCourses.firstOrNull { course ->
            course.occupies(dayOfWeek, slotCode, slotOrderMap, slots)
        }

        if (occupyingCourse != null) {
            _activeModal.value = ActiveModal.CourseDetail(occupyingCourse)
        } else {
            // Tap empty cell: open AddEditCourse with this cell pre-selected
            _activeModal.value = ActiveModal.AddEditCourse(
                course = null,
                prefilledDay = dayOfWeek,
                prefilledSlotCode = slotCode,
                initialSelectedSlots = setOf("$dayOfWeek:$slotCode")
            )
        }
    }

    fun setActiveSemester(id: Long) {
        _activeSemesterId.value = id
        viewModelScope.launch {
            repository.setActiveSemester(id)
        }
    }

    fun addSemester(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = repository.addSemester(name)
            setActiveSemester(newId)
        }
    }

    fun renameSemester(id: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renameSemester(id, newName)
        }
    }

    fun deleteSemester(semester: Semester) {
        viewModelScope.launch {
            repository.deleteSemester(semester)
        }
    }

    fun toggleWeekend(show: Boolean) {
        viewModelScope.launch {
            repository.toggleShowWeekend(show)
        }
    }

    fun toggleFitToScreen(fit: Boolean) {
        viewModelScope.launch {
            repository.toggleFitToScreen(fit)
        }
    }

    fun setFontSize(scale: FontSizeScale) {
        viewModelScope.launch {
            repository.updateFontSize(scale)
        }
    }

    fun toggleCourseCompleted(course: Course) {
        val targetCompleted = course.isCompleted
        viewModelScope.launch {
            repository.setCourseCompleted(course, targetCompleted)
            val currentModal = _activeModal.value
            if (currentModal is ActiveModal.CourseDetail && currentModal.course.id == course.id) {
                _activeModal.value = ActiveModal.CourseDetail(course.copy(isCompleted = targetCompleted))
            }
        }
    }

    fun setCourseCompleted(course: Course, completed: Boolean) {
        viewModelScope.launch {
            repository.setCourseCompleted(course, completed)
        }
    }

    fun saveCourse(course: Course) {
        viewModelScope.launch {
            val semesterId = uiState.value.activeSemester?.id ?: 1L
            repository.saveCourse(course.copy(semesterId = semesterId))
            closeModal()
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            closeModal()
        }
    }

    fun addTimeSlot(code: String, startTime: String, endTime: String) {
        if (code.isBlank() || startTime.isBlank() || endTime.isBlank()) return
        viewModelScope.launch {
            repository.addTimeSlot(code, startTime, endTime)
        }
    }

    fun updateTimeSlot(slot: TimeSlot) {
        viewModelScope.launch {
            repository.updateTimeSlot(slot)
        }
    }

    fun deleteTimeSlot(slot: TimeSlot) {
        viewModelScope.launch {
            repository.deleteTimeSlot(slot)
        }
    }

    fun resetToBenchmarkSlots() {
        viewModelScope.launch {
            repository.resetToBenchmarkSlots()
        }
    }

    fun addCategory(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCategory(name, colorHex)
        }
    }

    fun deleteCategory(category: CourseCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}

// Helper tuple for combining 5 flows
private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
