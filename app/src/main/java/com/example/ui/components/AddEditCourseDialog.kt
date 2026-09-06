package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.CourseCategory
import com.example.data.model.DefaultData
import com.example.data.model.TimeSlot

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCourseDialog(
    initialCourse: Course?,
    prefilledDay: Int,
    prefilledSlotCode: String,
    initialSelectedSlots: Set<String> = emptySet(),
    timeSlots: List<TimeSlot>,
    categories: List<CourseCategory>,
    showWeekend: Boolean = false,
    onSave: (Course) -> Unit,
    onDirectGridSelect: ((currentSlots: Set<String>) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var courseName by remember { mutableStateOf(initialCourse?.courseName ?: "") }
    var location by remember { mutableStateOf(initialCourse?.location ?: "") }
    var note by remember { mutableStateOf(initialCourse?.note ?: "") }

    val slotOrderMap = remember(timeSlots) { timeSlots.associateBy({ it.code }, { it.orderIndex }) }

    // Multi-slot selection state
    var selectedSlots by remember {
        val initial = when {
            initialSelectedSlots.isNotEmpty() -> initialSelectedSlots
            initialCourse != null -> initialCourse.getAssignedSlotKeys(slotOrderMap, timeSlots)
            prefilledSlotCode.isNotBlank() -> setOf("$prefilledDay:$prefilledSlotCode")
            else -> setOf("1:${timeSlots.firstOrNull()?.code ?: "1"}")
        }
        mutableStateOf(initial)
    }

    var selectedCategoryName by remember {
        mutableStateOf(initialCourse?.categoryName ?: categories.firstOrNull()?.name ?: "必修")
    }
    var selectedColorHex by remember {
        mutableStateOf(
            initialCourse?.colorHex ?: categories.firstOrNull { it.name == selectedCategoryName }?.colorHex ?: "#C86D51"
        )
    }
    var isCompleted by remember { mutableStateOf(initialCourse?.isCompleted ?: false) }

    var isCourseNameError by remember { mutableStateOf(false) }
    var isSlotsError by remember { mutableStateOf(false) }

    val daysList = if (showWeekend) {
        listOf(
            1 to "週一",
            2 to "週二",
            3 to "週三",
            4 to "週四",
            5 to "週五",
            6 to "週六",
            7 to "週日"
        )
    } else {
        listOf(
            1 to "週一",
            2 to "週二",
            3 to "週三",
            4 to "週四",
            5 to "週五"
        )
    }

    val courseThemeColor = try {
        Color(android.graphics.Color.parseColor(selectedColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialCourse == null) "新增課程" else "編輯課程",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "關閉")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. 課名 (Required)
                OutlinedTextField(
                    value = courseName,
                    onValueChange = {
                        courseName = it
                        if (it.isNotBlank()) isCourseNameError = false
                    },
                    label = { Text("課名 *") },
                    placeholder = { Text("例如：微積分、論文研討") },
                    isError = isCourseNameError,
                    supportingText = if (isCourseNameError) {
                        { Text("請輸入課名") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_course_name")
                )

                // 2. 教室地點 (Optional)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("教室地點") },
                    placeholder = { Text("例如：綜一館 201、工程館 A302") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_course_location")
                )

                // 3. 備註 (Optional)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("備註") },
                    placeholder = { Text("例如：指導教授、非連續時段提醒") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_course_note")
                )

                // 4. 時段設定 (支援多選、非連續節次、直接點選)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSlotsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "上課時段 (可點選多節/非連續)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedSlots.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = courseThemeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "已選 ${selectedSlots.size} 節",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = courseThemeColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Direct screen selection button
                        if (onDirectGridSelect != null) {
                            Button(
                                onClick = { onDirectGridSelect(selectedSlots) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("直接在課表畫面上點選時段", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Selected slots summary tags
                        if (selectedSlots.isEmpty()) {
                            Text(
                                text = "尚未選取任何時段，請點擊下方格子或使用上方「在課表畫面上點選」",
                                fontSize = 12.sp,
                                color = if (isSlotsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // Summary grouped by day
                            val dayNames = daysList.toMap()
                            val grouped = selectedSlots.groupBy {
                                it.split(":").getOrNull(0)?.toIntOrNull() ?: 1
                            }.toSortedMap()

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for ((d, list) in grouped) {
                                    val dayName = dayNames[d] ?: "週$d"
                                    val slotCodes = list.mapNotNull { it.split(":").getOrNull(1) }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = courseThemeColor.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, courseThemeColor.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "$dayName 第 ${slotCodes.joinToString(", ")} 節",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = courseThemeColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Quick actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "點選下方格子切換選取：",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selectedSlots.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { selectedSlots = emptySet() },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("清空", fontSize = 11.sp)
                                }
                            }
                        }

                        // Mini Interactive Matrix for direct in-dialog tapping
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            val hScroll = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(hScroll)
                                    .padding(4.dp)
                            ) {
                                // Header row (Days)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(44.dp)
                                            .height(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("節次", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    for ((d, label) in daysList) {
                                        Box(
                                            modifier = Modifier
                                                .width(52.dp)
                                                .height(28.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }

                                // Slot rows
                                for (slot in timeSlots) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        // Period code cell
                                        Box(
                                            modifier = Modifier
                                                .width(44.dp)
                                                .height(32.dp)
                                                .padding(2.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    RoundedCornerShape(4.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = slot.code,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Day cells
                                        for ((d, _) in daysList) {
                                            val key = "$d:${slot.code}"
                                            val isSelected = key in selectedSlots

                                            Box(
                                                modifier = Modifier
                                                    .width(52.dp)
                                                    .height(32.dp)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isSelected) courseThemeColor
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                                    )
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                                        color = if (isSelected) courseThemeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        val current = selectedSlots.toMutableSet()
                                                        if (key in current) {
                                                            current.remove(key)
                                                        } else {
                                                            current.add(key)
                                                        }
                                                        selectedSlots = current
                                                        isSlotsError = false
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "+",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. 課程類別 (主要分必修、選修、論文，及其他)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "課程類別",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (cat in categories) {
                            val isSelected = selectedCategoryName == cat.name
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(2.dp, catColor) else null,
                                modifier = Modifier.clickable {
                                    selectedCategoryName = cat.name
                                    selectedColorHex = cat.colorHex
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(catColor, CircleShape)
                                    )
                                    Text(
                                        text = cat.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. 課表顯示顏色 (Color Swatches)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "課表顯示顏色",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (colorHex in DefaultData.presetColors) {
                            val c = try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (e: Exception) {
                                Color.Gray
                            }
                            val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = colorHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. 修畢狀態 (Completion Check)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isCompleted) Color(0xFF22C55E) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isCompleted = !isCompleted }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isCompleted) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column {
                                Text(
                                    text = "標記為學期已修畢 / 完成",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isCompleted) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isCompleted) "已打勾完成，課表將呈現修畢徽章！" else "學期末勾選標記，檢視滿滿成就感！",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        androidx.compose.material3.Switch(
                            checked = isCompleted,
                            onCheckedChange = { isCompleted = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (courseName.isBlank()) {
                        isCourseNameError = true
                        return@Button
                    }
                    if (selectedSlots.isEmpty()) {
                        isSlotsError = true
                        return@Button
                    }

                    val sortedKeys = selectedSlots.sorted()
                    val firstKey = sortedKeys.firstOrNull() ?: "1:1"
                    val parts = firstKey.split(":")
                    val firstDay = parts.getOrNull(0)?.toIntOrNull() ?: 1
                    val firstSlot = parts.getOrNull(1) ?: "1"
                    val lastSlotOnFirstDay = sortedKeys
                        .filter { it.startsWith("$firstDay:") }
                        .lastOrNull()
                        ?.split(":")?.getOrNull(1) ?: firstSlot

                    val courseToSave = Course(
                        id = initialCourse?.id ?: 0L,
                        semesterId = initialCourse?.semesterId ?: 0L,
                        courseName = courseName.trim(),
                        location = location.trim(),
                        note = note.trim(),
                        dayOfWeek = firstDay,
                        startSlotCode = firstSlot,
                        endSlotCode = lastSlotOnFirstDay,
                        colorHex = selectedColorHex,
                        categoryName = selectedCategoryName,
                        timeSlotsString = sortedKeys.joinToString(","),
                        isCompleted = isCompleted
                    )
                    onSave(courseToSave)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("save_course_button")
            ) {
                Text(if (initialCourse == null) "儲存課程" else "更新課程")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
