package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditCourseDialog
import com.example.ui.components.CategoryManagerDialog
import com.example.ui.components.CourseDetailDialog
import com.example.ui.components.FontSizeDialog
import com.example.ui.components.SemesterManagerDialog
import com.example.ui.components.TimeSlotManagerDialog
import com.example.ui.components.TimetableGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeModal by viewModel.activeModal.collectAsStateWithLifecycle()
    val isGridSelectionMode by viewModel.isGridSelectionMode.collectAsStateWithLifecycle()
    val selectedGridSlots by viewModel.selectedGridSlots.collectAsStateWithLifecycle()

    val slotOrderMap = remember(uiState.timeSlots) {
        uiState.timeSlots.associateBy({ it.code }, { it.orderIndex })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isGridSelectionMode) {
                // In direct grid selection mode
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "點選畫面時段排課",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "直接點擊課表格子加入/移除時段",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.cancelGridSelectionMode() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "取消選取")
                        }
                    },
                    actions = {
                        if (selectedGridSlots.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.clearGridSelection() },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("清空")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                // Normal timetable TopAppBar
                TopAppBar(
                    title = {
                        // Semester Selector Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.openModal(ActiveModal.SemesterManager) }
                                .testTag("semester_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = uiState.activeSemester?.name ?: "選擇學期",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "展開學期選單",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    actions = {
                        // 1. Direct Grid Schedule Mode Trigger Button
                        IconButton(
                            onClick = { viewModel.startGridSelectionMode() },
                            modifier = Modifier.testTag("direct_grid_select_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "直接點選畫面排課",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 2. Weekend toggle chip (五天 / 七天)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (uiState.showWeekend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.toggleWeekend(!uiState.showWeekend) }
                                .padding(end = 4.dp)
                                .testTag("weekend_toggle_button")
                        ) {
                            Text(
                                text = if (uiState.showWeekend) "一~日" else "一~五",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.showWeekend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        // 3. Fit to screen / Scroll mode toggle
                        IconButton(
                            onClick = { viewModel.toggleFitToScreen(!uiState.fitToScreen) },
                            modifier = Modifier.testTag("fit_to_screen_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.fitToScreen) Icons.Default.FitScreen else Icons.Default.ZoomOutMap,
                                contentDescription = if (uiState.fitToScreen) "集中畫面模式 (已開啟)" else "滑動縮放模式",
                                tint = if (uiState.fitToScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 4. Periods / Time slot settings
                        IconButton(
                            onClick = { viewModel.openModal(ActiveModal.TimeSlotManager) },
                            modifier = Modifier.testTag("timeslot_manager_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "時程表設定",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 5. Font size adjustment
                        IconButton(
                            onClick = { viewModel.openModal(ActiveModal.FontSizePicker) },
                            modifier = Modifier.testTag("fontsize_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "字體大小",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 6. Categories & Colors
                        IconButton(
                            onClick = { viewModel.openModal(ActiveModal.CategoryManager) },
                            modifier = Modifier.testTag("category_manager_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "課程類別與顏色",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isGridSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        viewModel.openModal(
                            ActiveModal.AddEditCourse(
                                course = null,
                                prefilledDay = 1,
                                prefilledSlotCode = uiState.timeSlots.firstOrNull()?.code ?: "1"
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_course_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "新增課程")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                TimetableGrid(
                    timeSlots = uiState.timeSlots,
                    courses = uiState.courses,
                    showWeekend = uiState.showWeekend,
                    fontSizeScale = uiState.fontSizeScale,
                    fitToScreen = uiState.fitToScreen,
                    isSelectionMode = isGridSelectionMode,
                    selectedSlotKeys = selectedGridSlots,
                    onCellClicked = { dayOfWeek, slotCode ->
                        viewModel.onCellClicked(dayOfWeek, slotCode)
                    }
                )
            }

            // Floating Bottom Banner when in Grid Selection Mode
            if (isGridSelectionMode) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "已選取 ${selectedGridSlots.size} 個時段",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (selectedGridSlots.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "多選/非連續",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (selectedGridSlots.isEmpty()) "點選上方任意課表格子即可加入時段（如：週一 5, 6 與週五 9）"
                            else formatGridSlotsSummary(selectedGridSlots),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.cancelGridSelectionMode() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("取消")
                            }
                            Button(
                                onClick = { viewModel.finishGridSelectionAndOpenCourseForm() },
                                enabled = selectedGridSlots.isNotEmpty(),
                                modifier = Modifier.weight(1.6f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("下一步：填寫課程")
                            }
                        }
                    }
                }
            }
        }

        // --- Handle Modals & Bottom Sheets ---
        when (val modal = activeModal) {
            is ActiveModal.None -> {}

            is ActiveModal.AddEditCourse -> {
                AddEditCourseDialog(
                    initialCourse = modal.course,
                    prefilledDay = modal.prefilledDay,
                    prefilledSlotCode = modal.prefilledSlotCode,
                    initialSelectedSlots = modal.initialSelectedSlots,
                    timeSlots = uiState.timeSlots,
                    categories = uiState.categories,
                    showWeekend = uiState.showWeekend,
                    onSave = { course -> viewModel.saveCourse(course) },
                    onDirectGridSelect = { currentSlots ->
                        viewModel.startGridSelectionMode(courseToEdit = modal.course, initialSlots = currentSlots)
                    },
                    onDismiss = { viewModel.closeModal() }
                )
            }

            is ActiveModal.CourseDetail -> {
                CourseDetailDialog(
                    course = modal.course,
                    timeSlots = uiState.timeSlots,
                    onEdit = { course ->
                        val initialSlots = course.getAssignedSlotKeys(slotOrderMap, uiState.timeSlots)
                        viewModel.openModal(
                            ActiveModal.AddEditCourse(
                                course = course,
                                prefilledDay = course.dayOfWeek,
                                prefilledSlotCode = course.startSlotCode,
                                initialSelectedSlots = initialSlots
                            )
                        )
                    },
                    onDelete = { course -> viewModel.deleteCourse(course) },
                    onToggleCompleted = { course -> viewModel.toggleCourseCompleted(course) },
                    onDismiss = { viewModel.closeModal() }
                )
            }

            is ActiveModal.SemesterManager -> {
                SemesterManagerDialog(
                    semesters = uiState.semesters,
                    activeSemesterId = uiState.activeSemester?.id ?: 1L,
                    onSelectSemester = { id -> viewModel.setActiveSemester(id) },
                    onAddSemester = { name -> viewModel.addSemester(name) },
                    onRenameSemester = { id, name -> viewModel.renameSemester(id, name) },
                    onDeleteSemester = { sem -> viewModel.deleteSemester(sem) },
                    onDismiss = { viewModel.closeModal() }
                )
            }

            is ActiveModal.TimeSlotManager -> {
                TimeSlotManagerDialog(
                    timeSlots = uiState.timeSlots,
                    onAddSlot = { code, start, end -> viewModel.addTimeSlot(code, start, end) },
                    onUpdateSlot = { slot -> viewModel.updateTimeSlot(slot) },
                    onDeleteSlot = { slot -> viewModel.deleteTimeSlot(slot) },
                    onResetToBenchmark = { viewModel.resetToBenchmarkSlots() },
                    onDismiss = { viewModel.closeModal() }
                )
            }

            is ActiveModal.FontSizePicker -> {
                FontSizeDialog(
                    currentScale = uiState.fontSizeScale,
                    onSelectScale = { scale -> viewModel.setFontSize(scale) },
                    onDismiss = { viewModel.closeModal() }
                )
            }

            is ActiveModal.CategoryManager -> {
                CategoryManagerDialog(
                    categories = uiState.categories,
                    onAddCategory = { name, colorHex -> viewModel.addCategory(name, colorHex) },
                    onDeleteCategory = { cat -> viewModel.deleteCategory(cat) },
                    onDismiss = { viewModel.closeModal() }
                )
            }
        }
    }
}

private fun formatGridSlotsSummary(slots: Set<String>): String {
    val dayNames = mapOf(1 to "週一", 2 to "週二", 3 to "週三", 4 to "週四", 5 to "週五", 6 to "週六", 7 to "週日")
    val grouped = slots.groupBy {
        it.split(":").getOrNull(0)?.toIntOrNull() ?: 1
    }.toSortedMap()

    return grouped.entries.joinToString("、") { (d, list) ->
        val dayName = dayNames[d] ?: "週$d"
        val codes = list.mapNotNull { it.split(":").getOrNull(1) }
        "$dayName 第 ${codes.joinToString(", ")} 節"
    }
}
