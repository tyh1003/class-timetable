package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.data.model.Course
import com.example.data.model.TimeSlot

object TimetableBitmapRenderer {

    /**
     * Renders the complete timetable grid into a high-quality Bitmap
     * representing the app's full timetable view.
     */
    fun renderTimetableBitmap(
        context: Context,
        courses: List<Course>,
        timeSlots: List<TimeSlot>,
        showWeekend: Boolean = false,
        width: Int = 720,
        height: Int = 480
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)

        // 1. Fill clean background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val days = if (showWeekend || courses.any { it.occupiesDay(6) || it.occupiesDay(7) }) {
            listOf(1, 2, 3, 4, 5, 6, 7)
        } else {
            listOf(1, 2, 3, 4, 5)
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

        val headerHeight = 36f
        val periodColWidth = if (days.size > 5) 44f else 52f
        val numDays = days.size
        val dayColWidth = (width - periodColWidth) / numDays
        val numSlots = timeSlots.size.coerceAtLeast(1)
        val rowHeight = (height - headerHeight) / numSlots

        // Grid lines paint
        val gridPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        // Header bg paint
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight, headerBgPaint)

        // Time col bg paint
        val timeColBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, headerHeight, periodColWidth, height.toFloat(), timeColBgPaint)

        // Text paints
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val slotCodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2563EB")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val slotTimePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
            textAlign = Paint.Align.CENTER
        }

        // Draw day headers
        val headerBaseline = headerHeight / 2f + headerTextPaint.textSize / 3f
        for ((idx, day) in days.withIndex()) {
            val cx = periodColWidth + idx * dayColWidth + dayColWidth / 2f
            canvas.drawText(dayNames[day] ?: "週$day", cx, headerBaseline, headerTextPaint)
        }

        // Draw time slots in time column
        for ((i, slot) in timeSlots.withIndex()) {
            val topY = headerHeight + i * rowHeight
            val centerY = topY + rowHeight / 2f
            canvas.drawText(slot.code, periodColWidth / 2f, centerY - 2f, slotCodePaint)
            if (rowHeight >= 28f) {
                canvas.drawText(slot.startTime, periodColWidth / 2f, centerY + 11f, slotTimePaint)
            }
        }

        // Draw grid lines
        canvas.drawLine(0f, headerHeight, width.toFloat(), headerHeight, gridPaint)
        canvas.drawLine(periodColWidth, 0f, periodColWidth, height.toFloat(), gridPaint)

        for (d in 1 until numDays) {
            val x = periodColWidth + d * dayColWidth
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        }
        for (r in 1 until numSlots) {
            val y = headerHeight + r * rowHeight
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }

        // Slot Order map
        val slotOrderMap = timeSlots.associateBy({ it.code }, { it.orderIndex })

        // Course card paints
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val courseNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 13f
        }
        val locationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
        }
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw Courses with continuous slot merging
        for ((dayIdx, day) in days.withIndex()) {
            var i = 0
            while (i < numSlots) {
                val currentSlot = timeSlots[i]
                val course = courses.firstOrNull { it.occupies(day, currentSlot.code, slotOrderMap, timeSlots) }

                if (course == null) {
                    i++
                } else {
                    var span = 1
                    while (i + span < numSlots) {
                        val nextSlot = timeSlots[i + span]
                        val nextCourse = courses.firstOrNull { it.occupies(day, nextSlot.code, slotOrderMap, timeSlots) }
                        if (nextCourse?.id == course.id) {
                            span++
                        } else {
                            break
                        }
                    }

                    val left = periodColWidth + dayIdx * dayColWidth + 2f
                    val right = periodColWidth + (dayIdx + 1) * dayColWidth - 2f
                    val top = headerHeight + i * rowHeight + 2f
                    val bottom = headerHeight + (i + span) * rowHeight - 2f

                    val cardRect = RectF(left, top, right, bottom)
                    val cornerRadius = 7f

                    val parsedColor = try {
                        Color.parseColor(course.colorHex)
                    } catch (e: Exception) {
                        Color.parseColor("#3B82F6")
                    }

                    val isCompleted = course.isCompleted

                    if (isCompleted) {
                        // Translucent background
                        val red = Color.red(parsedColor)
                        val green = Color.green(parsedColor)
                        val blue = Color.blue(parsedColor)
                        cardPaint.color = Color.argb(80, red, green, blue) // Semi-transparent
                        cardPaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint)

                        // Green border for completed
                        borderPaint.color = Color.parseColor("#16A34A")
                        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)

                        courseNamePaint.color = Color.parseColor("#1E293B")
                        locationPaint.color = Color.parseColor("#64748B")
                    } else {
                        cardPaint.color = parsedColor
                        cardPaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint)

                        val luminance = (0.299 * Color.red(parsedColor) + 0.587 * Color.green(parsedColor) + 0.114 * Color.blue(parsedColor)) / 255.0
                        val isLight = luminance > 0.65
                        courseNamePaint.color = if (isLight) Color.parseColor("#0F172A") else Color.WHITE
                        locationPaint.color = if (isLight) Color.parseColor("#334155") else Color.argb(220, 255, 255, 255)
                    }

                    // Content inside card
                    val cardWidth = right - left
                    val cardHeight = bottom - top

                    val padX = 4f
                    var curY = top + 14f

                    // If completed, draw small green checkmark in top-right
                    if (isCompleted) {
                        val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#16A34A")
                            style = Paint.Style.FILL
                        }
                        val checkTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.WHITE
                            textSize = 8.5f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            textAlign = Paint.Align.CENTER
                        }
                        val circleR = 5.5f
                        val cx = right - padX - circleR
                        val cy = top + padX + circleR
                        canvas.drawCircle(cx, cy, circleR, checkPaint)
                        canvas.drawText("✓", cx, cy + 3f, checkTextPaint)
                    }

                    // Draw Course Name (truncate if needed)
                    val maxNameChars = if (cardWidth < 60f) 4 else if (cardWidth < 90f) 6 else 8
                    val displayName = if (course.courseName.length > maxNameChars) {
                        course.courseName.take(maxNameChars) + "…"
                    } else {
                        course.courseName
                    }
                    canvas.drawText(displayName, left + padX, curY, courseNamePaint)

                    // Draw Location if space permits
                    if (course.location.isNotBlank() && cardHeight >= 36f) {
                        curY += 13f
                        val maxLocChars = if (cardWidth < 60f) 4 else 7
                        val displayLoc = if (course.location.length > maxLocChars) {
                            course.location.take(maxLocChars) + "…"
                        } else {
                            course.location
                        }
                        canvas.drawText(displayLoc, left + padX, curY, locationPaint)
                    }

                    // Draw span indicator if span > 1 and height permits
                    if (span > 1 && cardHeight >= 55f) {
                        curY += 12f
                        val spanText = "${span}節連堂"
                        badgeTextPaint.color = locationPaint.color
                        canvas.drawText(spanText, left + padX, curY, badgeTextPaint)
                    }

                    i += span
                }
            }
        }

        return bitmap
    }
}
