package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.FontSizeScale
import com.example.data.model.TimeSlot
import java.util.Calendar

@Composable
fun TimetableGrid(
    timeSlots: List<TimeSlot>,
    courses: List<Course>,
    showWeekend: Boolean,
    fontSizeScale: FontSizeScale,
    fitToScreen: Boolean = true,
    onCellClicked: (dayOfWeek: Int, slotCode: String) -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    selectedSlotKeys: Set<String> = emptySet()
) {
    val days = remember(showWeekend) {
        if (showWeekend) listOf(1, 2, 3, 4, 5, 6, 7) else listOf(1, 2, 3, 4, 5)
    }

    val dayNames = mapOf(
        1 to "週一",
        2 to "週二",
        3 to "週三",
        4 to "週四",
        5 to "週五",
        6 to "週六",
        7 to "週日"
    )

    // Current real-life day of week (1=Mon..7=Sun)
    val currentDayOfWeek = remember {
        val cal = Calendar.getInstance()
        val cDay = cal.get(Calendar.DAY_OF_WEEK)
        if (cDay == Calendar.SUNDAY) 7 else cDay - 1
    }

    val slotOrderMap = remember(timeSlots) {
        timeSlots.associateBy({ it.code }, { it.orderIndex })
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth = maxWidth
            val totalHeight = maxHeight

            // Determine dimensions based on fitToScreen mode
            val headerHeight: Dp = if (fitToScreen) {
                if (totalHeight < 550.dp) 28.dp else 32.dp
            } else {
                36.dp
            }

            val periodColWidth: Dp = if (fitToScreen) {
                if (showWeekend) 38.dp else 44.dp
            } else {
                56.dp
            }

            val dayColWidth: Dp = if (fitToScreen) {
                ((totalWidth - periodColWidth) / days.size).coerceAtLeast(40.dp)
            } else {
                if (showWeekend) 66.dp else 76.dp
            }

            val rowHeight: Dp = if (fitToScreen) {
                val numSlots = timeSlots.size.coerceAtLeast(1)
                val remainingHeight = (totalHeight - headerHeight).coerceAtLeast(100.dp)
                (remainingHeight / numSlots).coerceAtLeast(30.dp)
            } else {
                when (fontSizeScale) {
                    FontSizeScale.SMALL -> 68.dp
                    FontSizeScale.MEDIUM -> 76.dp
                    FontSizeScale.LARGE -> 86.dp
                    FontSizeScale.EXTRA_LARGE -> 96.dp
                }
            }

            val isCompact = fitToScreen && rowHeight < 50.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!fitToScreen) Modifier.horizontalScroll(horizontalScrollState)
                        else Modifier
                    )
            ) {
                // --- Day Header Row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                        .background(MaterialTheme.colorScheme.surface),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Top-left corner spacer for Period column
                    Box(
                        modifier = Modifier
                            .width(periodColWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "節次",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = if (isCompact) 10.sp else 11.sp
                            )
                        )
                    }

                    // Divider between Period header and Days
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Day Columns Headers
                    for (day in days) {
                        val isToday = day == currentDayOfWeek
                        Box(
                            modifier = Modifier
                                .width(dayColWidth)
                                .fillMaxHeight()
                                .border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                )
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = dayNames[day] ?: "",
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        fontSize = if (isCompact) 11.sp else (fontSizeScale.bodySp + 1).sp
                                    )
                                    if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Divider below Day Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                )

                // --- Timetable Content ---
                val contentModifier = if (!fitToScreen) {
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                } else {
                    Modifier.fillMaxSize()
                }

                Row(modifier = contentModifier) {
                    // Left Period Column
                    Column(
                        modifier = Modifier
                            .width(periodColWidth)
                            .fillMaxHeight()
                    ) {
                        for (slot in timeSlots) {
                            Column(
                                modifier = Modifier
                                    .width(periodColWidth)
                                    .height(rowHeight)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = slot.code,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isCompact) 11.sp else (fontSizeScale.titleSp + 1).sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (!isCompact || rowHeight >= 38.dp) {
                                    Text(
                                        text = slot.startTime,
                                        fontSize = if (isCompact) 7.5.sp else (fontSizeScale.bodySp - 2.5f).coerceAtLeast(8f).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = if (isCompact) 9.sp else 11.sp
                                    )
                                    Text(
                                        text = slot.endTime,
                                        fontSize = if (isCompact) 7.5.sp else (fontSizeScale.bodySp - 2.5f).coerceAtLeast(8f).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        lineHeight = if (isCompact) 9.sp else 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Divider between Period column and days
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Day Columns (Merging continuous classes into single blocks)
                    for (day in days) {
                        Column(
                            modifier = Modifier
                                .width(dayColWidth)
                                .fillMaxHeight()
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        ) {
                            var i = 0
                            val totalSlots = timeSlots.size

                            while (i < totalSlots) {
                                val currentSlot = timeSlots[i]
                                val currentSlotKey = "$day:${currentSlot.code}"

                                if (isSelectionMode) {
                                    // Direct Selection Mode: Each slot is individually tapable
                                    val isSelected = currentSlotKey in selectedSlotKeys
                                    Box(
                                        modifier = Modifier
                                            .width(dayColWidth)
                                            .height(rowHeight)
                                            .border(
                                                if (isSelected) 1.5.dp else 0.5.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                            )
                                            .padding(1.5.dp)
                                            .clickable { onCellClicked(day, currentSlot.code) }
                                            .testTag("cell_${day}_${currentSlot.code}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Card(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(6.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "已選擇",
                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = "已選",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "點選加入",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    i++
                                } else {
                                    // Normal Timetable Mode: Merge continuous courses into a single large block
                                    val occupyingCourse = courses.firstOrNull {
                                        it.occupies(day, currentSlot.code, slotOrderMap, timeSlots)
                                    }

                                    if (occupyingCourse == null) {
                                        // Empty slot
                                        Box(
                                            modifier = Modifier
                                                .width(dayColWidth)
                                                .height(rowHeight)
                                                .border(
                                                    0.5.dp,
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                                )
                                                .clickable { onCellClicked(day, currentSlot.code) }
                                                .testTag("cell_${day}_${currentSlot.code}")
                                        )
                                        i++
                                    } else {
                                        // Calculate continuous span for this course on this day
                                        var span = 1
                                        while (i + span < totalSlots) {
                                            val nextSlot = timeSlots[i + span]
                                            val nextOccupying = courses.firstOrNull {
                                                it.occupies(day, nextSlot.code, slotOrderMap, timeSlots)
                                            }
                                            if (nextOccupying?.id == occupyingCourse.id) {
                                                span++
                                            } else {
                                                break
                                            }
                                        }

                                        val blockHeight = rowHeight * span

                                        Box(
                                            modifier = Modifier
                                                .width(dayColWidth)
                                                .height(blockHeight)
                                                .padding(1.5.dp)
                                                .clickable { onCellClicked(day, currentSlot.code) }
                                                .testTag("course_block_${occupyingCourse.id}_${day}_${currentSlot.code}")
                                        ) {
                                            MergedCourseBlockCard(
                                                course = occupyingCourse,
                                                spanCount = span,
                                                fontSizeScale = fontSizeScale,
                                                isCompact = isCompact,
                                                totalHeight = blockHeight
                                            )
                                        }

                                        i += span
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MergedCourseBlockCard(
    course: Course,
    spanCount: Int,
    fontSizeScale: FontSizeScale,
    isCompact: Boolean,
    totalHeight: Dp
) {
    val parsedColor = remember(course.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex))
        } catch (e: Exception) {
            Color(0xFF6750A4)
        }
    }

    // Determine contrast text color based on luminance
    val isColorLight = (0.299 * parsedColor.red + 0.587 * parsedColor.green + 0.114 * parsedColor.blue) > 0.60
    val rawContentTextColor = if (isColorLight) Color(0xFF1D1B20) else Color.White
    val contentTextColor = if (course.isCompleted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
    } else {
        rawContentTextColor
    }
    val secondaryTextColor = if (course.isCompleted) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
    } else {
        if (isColorLight) Color(0xFF49454F) else Color.White.copy(alpha = 0.88f)
    }

    // Completed card: translucent/semi-transparent background (例如變透明), distinct fresh border
    val cardBackground = if (course.isCompleted) {
        parsedColor.copy(alpha = 0.28f)
    } else {
        parsedColor
    }

    val cardBorder = if (course.isCompleted) {
        BorderStroke(1.5.dp, Color(0xFF16A34A).copy(alpha = 0.85f))
    } else null

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (course.isCompleted) 0.dp else 1.5.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Course Name & Completed Check Badge
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = course.courseName,
                        color = contentTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompact && spanCount == 1) 10.5.sp else fontSizeScale.titleSp.sp,
                        lineHeight = if (isCompact && spanCount == 1) 12.sp else (fontSizeScale.titleSp + 2).sp,
                        maxLines = if (spanCount > 1) 3 else 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (course.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f)
                    )

                    // Completion Checkmark Badge
                    if (course.isCompleted) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF16A34A),
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(if (isCompact) 13.dp else 15.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "已修畢",
                                tint = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                // Location - DISPLAYED ONLY ONCE across this continuous block!
                if (course.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(if (isCompact) 9.dp else 10.dp)
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(
                            text = course.location,
                            color = secondaryTextColor,
                            fontSize = if (isCompact) 8.5.sp else fontSizeScale.bodySp.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Bottom: Category tag / Completed badge (and span indicator if multi-period)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (spanCount > 1) {
                    Text(
                        text = "${spanCount}節連堂",
                        color = secondaryTextColor,
                        fontSize = if (isCompact) 7.5.sp else 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (course.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "✓已修畢",
                            color = Color(0xFF15803D),
                            fontSize = if (isCompact) 7.sp else 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = if (course.isCompleted) {
                        Color(0xFF16A34A).copy(alpha = 0.15f)
                    } else {
                        contentTextColor.copy(alpha = 0.20f)
                    }
                ) {
                    Text(
                        text = course.categoryName,
                        color = if (course.isCompleted) Color(0xFF15803D) else contentTextColor,
                        fontSize = if (isCompact) 7.5.sp else (fontSizeScale.bodySp - 2.5f).coerceAtLeast(8f).sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
