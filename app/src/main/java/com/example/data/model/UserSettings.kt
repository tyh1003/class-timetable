package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FontSizeScale(val label: String, val scaleFactor: Float, val titleSp: Float, val bodySp: Float) {
    SMALL("小", 0.85f, 11f, 9.5f),
    MEDIUM("中", 1.0f, 13f, 11f),
    LARGE("大", 1.15f, 15f, 12.5f),
    EXTRA_LARGE("特大", 1.3f, 17f, 14f)
}

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val showWeekend: Boolean = false, // false = 一~五, true = 一~日
    val fontSizeScaleName: String = FontSizeScale.MEDIUM.name,
    val activeSemesterId: Long = 1,
    @ColumnInfo(defaultValue = "1")
    val fitToScreen: Boolean = true // 整體集中在單一畫面，不上下左右滑
) {
    val fontSizeScale: FontSizeScale
        get() = try {
            FontSizeScale.valueOf(fontSizeScaleName)
        } catch (e: Exception) {
            FontSizeScale.MEDIUM
        }
}
