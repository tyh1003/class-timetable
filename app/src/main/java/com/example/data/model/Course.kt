package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val semesterId: Long,
    val courseName: String,
    val location: String = "",
    val note: String = "",
    val dayOfWeek: Int = 1, // 1: Mon, 2: Tue, 3: Wed, 4: Thu, 5: Fri, 6: Sat, 7: Sun
    val startSlotCode: String = "",
    val endSlotCode: String = "",
    val colorHex: String,
    val categoryName: String = "必修",
    @ColumnInfo(defaultValue = "")
    val timeSlotsString: String = "", // Formatted as "day:slotCode,day:slotCode", e.g. "1:5,1:6,5:9"
    @ColumnInfo(defaultValue = "0")
    val isCompleted: Boolean = false // 學期末完成/修畢打勾標記
) {
    fun occupiesDay(day: Int): Boolean {
        if (timeSlotsString.isNotBlank()) {
            return timeSlotsString.split(",").any { it.trim().startsWith("$day:") }
        }
        return dayOfWeek == day
    }
    /**
     * Returns a set of "day:slotCode" strings representing all time slots for this course.
     * Supports both new multi-slot format and legacy single day/slot ranges.
     */
    fun getAssignedSlotKeys(
        slotOrderMap: Map<String, Int> = emptyMap(),
        allSlots: List<TimeSlot> = emptyList()
    ): Set<String> {
        if (timeSlotsString.isNotBlank()) {
            return timeSlotsString.split(",")
                .map { it.trim() }
                .filter { it.contains(":") }
                .toSet()
        }

        // Legacy fallback
        if (startSlotCode.isBlank()) return emptySet()
        val sOrder = slotOrderMap[startSlotCode] ?: -1
        val eOrder = slotOrderMap[endSlotCode] ?: sOrder
        val result = mutableSetOf<String>()

        if (allSlots.isNotEmpty() && sOrder != -1 && eOrder != -1) {
            val minOrder = minOf(sOrder, eOrder)
            val maxOrder = maxOf(sOrder, eOrder)
            for (slot in allSlots) {
                if (slot.orderIndex in minOrder..maxOrder) {
                    result.add("$dayOfWeek:${slot.code}")
                }
            }
        } else {
            result.add("$dayOfWeek:$startSlotCode")
            if (endSlotCode.isNotBlank() && endSlotCode != startSlotCode) {
                result.add("$dayOfWeek:$endSlotCode")
            }
        }
        return result
    }

    /**
     * Checks if this course occupies the given day of week and slot code.
     */
    fun occupies(
        day: Int,
        slotCode: String,
        slotOrderMap: Map<String, Int> = emptyMap(),
        allSlots: List<TimeSlot> = emptyList()
    ): Boolean {
        if (timeSlotsString.isNotBlank()) {
            val keys = timeSlotsString.split(",").map { it.trim() }
            return "$day:$slotCode" in keys
        }

        if (dayOfWeek != day) return false
        val sOrder = slotOrderMap[startSlotCode] ?: -1
        val eOrder = slotOrderMap[endSlotCode] ?: sOrder
        val targetOrder = slotOrderMap[slotCode] ?: -1

        return if (targetOrder != -1 && sOrder != -1 && eOrder != -1) {
            targetOrder in minOf(sOrder, eOrder)..maxOf(sOrder, eOrder)
        } else {
            startSlotCode == slotCode || endSlotCode == slotCode
        }
    }

    /**
     * Returns a human-friendly multi-line or comma-separated summary of all class times.
     * e.g.:
     * 週一 第 5~6 節 (13:20 ~ 15:10)
     * 週五 第 9 節 (17:30 ~ 18:20)
     */
    fun formatScheduleSummary(timeSlots: List<TimeSlot>): List<String> {
        val slotMap = timeSlots.associateBy { it.code }
        val slotOrderMap = timeSlots.associateBy({ it.code }, { it.orderIndex })
        val keys = getAssignedSlotKeys(slotOrderMap, timeSlots)

        val dayNames = mapOf(
            1 to "週一",
            2 to "週二",
            3 to "週三",
            4 to "週四",
            5 to "週五",
            6 to "週六",
            7 to "週日"
        )

        // Group by day of week
        val dayGrouped = keys.mapNotNull { key ->
            val parts = key.split(":")
            if (parts.size == 2) {
                val day = parts[0].toIntOrNull()
                val code = parts[1]
                if (day != null) Triple(day, code, slotOrderMap[code] ?: 0) else null
            } else null
        }.groupBy({ it.first }, { Pair(it.second, it.third) })

        val lines = mutableListOf<String>()

        for ((day, slotPairs) in dayGrouped.toSortedMap()) {
            val sortedCodes = slotPairs.sortedBy { it.second }.map { it.first }
            val dayName = dayNames[day] ?: "週$day"

            if (sortedCodes.isEmpty()) continue

            val firstSlot = slotMap[sortedCodes.first()]
            val lastSlot = slotMap[sortedCodes.last()]

            val periodText = if (sortedCodes.size == 1) {
                "第 ${sortedCodes.first()} 節"
            } else {
                // Check if consecutive
                val firstOrder = slotOrderMap[sortedCodes.first()] ?: 0
                val lastOrder = slotOrderMap[sortedCodes.last()] ?: 0
                if (lastOrder - firstOrder + 1 == sortedCodes.size) {
                    "第 ${sortedCodes.first()} ~ ${sortedCodes.last()} 節"
                } else {
                    "第 ${sortedCodes.joinToString(", ")} 節"
                }
            }

            val timeText = if (firstSlot != null && lastSlot != null) {
                " (${firstSlot.startTime} ~ ${lastSlot.endTime})"
            } else ""

            lines.add("$dayName $periodText$timeText")
        }

        if (lines.isEmpty() && startSlotCode.isNotBlank()) {
            val dayName = dayNames[dayOfWeek] ?: "週$dayOfWeek"
            val periodText = if (startSlotCode == endSlotCode || endSlotCode.isBlank()) {
                "第 $startSlotCode 節"
            } else {
                "第 $startSlotCode ~ $endSlotCode 節"
            }
            val firstSlot = slotMap[startSlotCode]
            val lastSlot = slotMap[endSlotCode.ifBlank { startSlotCode }]
            val timeText = if (firstSlot != null && lastSlot != null) {
                " (${firstSlot.startTime} ~ ${lastSlot.endTime})"
            } else ""
            lines.add("$dayName $periodText$timeText")
        }

        return lines
    }
}

