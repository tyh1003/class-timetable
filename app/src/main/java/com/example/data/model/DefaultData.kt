package com.example.data.model

object DefaultData {
    val defaultSemesters = listOf(
        Semester(id = 1, name = "這學期 (113-1)", isCurrent = true, orderIndex = 0),
        Semester(id = 2, name = "下學期 (113-2)", isCurrent = false, orderIndex = 1)
    )

    // Benchmark time slots as explicitly specified by user:
    // y 6:00 ~ 6:50, z 7:00 ~ 7:50
    // 1 8:00 ~ 8:50, 2 9:00 ~ 9:50, 3 10:10 ~ 11:00, 4 11:10 ~ 12:00, n 12:20 ~ 13:10
    // 5 13:20 ~ 14:10, 6 14:20 ~ 15:10, 7 15:30 ~ 16:20, 8 16:30 ~ 17:20, 9 17:30 ~ 18:20
    // a 18:30 ~ 19:20, b 19:30 ~ 20:20, c 20:30 ~ 21:20, d 21:30 ~ 22:20
    val benchmarkSlots = listOf(
        TimeSlot(id = 1, semesterId = 0, code = "y", startTime = "06:00", endTime = "06:50", orderIndex = 0),
        TimeSlot(id = 2, semesterId = 0, code = "z", startTime = "07:00", endTime = "07:50", orderIndex = 1),
        TimeSlot(id = 3, semesterId = 0, code = "1", startTime = "08:00", endTime = "08:50", orderIndex = 2),
        TimeSlot(id = 4, semesterId = 0, code = "2", startTime = "09:00", endTime = "09:50", orderIndex = 3),
        TimeSlot(id = 5, semesterId = 0, code = "3", startTime = "10:10", endTime = "11:00", orderIndex = 4),
        TimeSlot(id = 6, semesterId = 0, code = "4", startTime = "11:10", endTime = "12:00", orderIndex = 5),
        TimeSlot(id = 7, semesterId = 0, code = "n", startTime = "12:20", endTime = "13:10", orderIndex = 6),
        TimeSlot(id = 8, semesterId = 0, code = "5", startTime = "13:20", endTime = "14:10", orderIndex = 7),
        TimeSlot(id = 9, semesterId = 0, code = "6", startTime = "14:20", endTime = "15:10", orderIndex = 8),
        TimeSlot(id = 10, semesterId = 0, code = "7", startTime = "15:30", endTime = "16:20", orderIndex = 9),
        TimeSlot(id = 11, semesterId = 0, code = "8", startTime = "16:30", endTime = "17:20", orderIndex = 10),
        TimeSlot(id = 12, semesterId = 0, code = "9", startTime = "17:30", endTime = "18:20", orderIndex = 11),
        TimeSlot(id = 13, semesterId = 0, code = "a", startTime = "18:30", endTime = "19:20", orderIndex = 12),
        TimeSlot(id = 14, semesterId = 0, code = "b", startTime = "19:30", endTime = "20:20", orderIndex = 13),
        TimeSlot(id = 15, semesterId = 0, code = "c", startTime = "20:30", endTime = "21:20", orderIndex = 14),
        TimeSlot(id = 16, semesterId = 0, code = "d", startTime = "21:30", endTime = "22:20", orderIndex = 15)
    )

    // Colors: 主要分必修 選修 論文 三個顏色 以及可以新增其他的 (Natural Tones)
    val defaultCategories = listOf(
        CourseCategory(id = 1, name = "必修", colorHex = "#E06D85", isDefault = true),
        CourseCategory(id = 2, name = "選修", colorHex = "#5E84C7", isDefault = true),
        CourseCategory(id = 3, name = "論文", colorHex = "#588B6B", isDefault = true),
        CourseCategory(id = 4, name = "通識", colorHex = "#D97736", isDefault = false),
        CourseCategory(id = 5, name = "其他", colorHex = "#8D7B68", isDefault = false)
    )

    val presetColors = listOf(
        "#E06D85", // Natural Rose / Terracotta
        "#D97736", // Warm Earth Amber
        "#E6A052", // Sand / Ochre
        "#588B6B", // Sage Green
        "#3E8E7E", // Pine / Eucalyptus
        "#5E84C7", // River / Periwinkle Blue
        "#7B68EE", // Iris / Soft Lavender
        "#8D7B68", // Timber / Earth
        "#9C6B82", // Dusty Mauve
        "#607D8B", // Slate Grey
        "#C86D51"  // Clay Red
    )
}
