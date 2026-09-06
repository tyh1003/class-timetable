package com.example.widget

import android.content.Context

object TimetableWidgetPrefs {
    private const val PREFS_NAME = "timetable_widget_prefs"
    private const val PREF_PREFIX_SEMESTER = "widget_semester_"
    private const val PREF_PREFIX_DAY = "widget_day_"

    fun setWidgetSemester(context: Context, appWidgetId: Int, semesterId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(PREF_PREFIX_SEMESTER + appWidgetId, semesterId).apply()
    }

    fun getWidgetSemester(context: Context, appWidgetId: Int, defaultSemesterId: Long): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(PREF_PREFIX_SEMESTER + appWidgetId, defaultSemesterId)
    }

    fun setWidgetDay(context: Context, appWidgetId: Int, dayOfWeek: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_PREFIX_DAY + appWidgetId, dayOfWeek).apply()
    }

    fun getWidgetDay(context: Context, appWidgetId: Int, defaultDay: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(PREF_PREFIX_DAY + appWidgetId, defaultDay)
    }

    fun removeWidgetPrefs(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(PREF_PREFIX_SEMESTER + appWidgetId)
            .remove(PREF_PREFIX_DAY + appWidgetId)
            .apply()
    }
}
