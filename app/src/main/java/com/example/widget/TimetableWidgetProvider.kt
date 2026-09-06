package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.DefaultData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimetableWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            TimetableWidgetPrefs.removeWidgetPrefs(context, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TimetableWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val views = buildRemoteViews(context, appWidgetId)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    android.util.Log.e("TimetableWidget", "Failed to update widget $appWidgetId", e)
                }
            }
        }

        suspend fun updateAppWidgetSync(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = buildRemoteViews(context, appWidgetId)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                android.util.Log.e("TimetableWidget", "Failed to sync-update widget $appWidgetId", e)
            }
        }

        private suspend fun buildRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
            val db = AppDatabase.getDatabase(context)
            val dao = db.timetableDao()

            var allSemesters = dao.getAllSemestersSync()
            if (allSemesters.isEmpty()) {
                dao.insertSemesters(DefaultData.defaultSemesters)
                allSemesters = dao.getAllSemestersSync()
            }

            val settings = dao.getUserSettingsSync()
            val defaultSemesterId = settings?.activeSemesterId ?: allSemesters.firstOrNull()?.id ?: 1L
            val semesterId = TimetableWidgetPrefs.getWidgetSemester(context, appWidgetId, defaultSemesterId)
            val semester = dao.getSemesterById(semesterId)
                ?: allSemesters.firstOrNull { it.id == defaultSemesterId }
                ?: allSemesters.firstOrNull()

            val semesterName = semester?.name ?: "學期課表"
            val targetSemesterId = semester?.id ?: defaultSemesterId

            val allSemesterCourses = dao.getCoursesForSemesterSync(targetSemesterId)
            var timeSlots = dao.getAllTimeSlotsSync()
            if (timeSlots.isEmpty()) {
                dao.insertTimeSlots(DefaultData.benchmarkSlots)
                timeSlots = dao.getAllTimeSlotsSync()
            }

            // Render full timetable grid bitmap (looks like the app's full timetable interface)
            val showWeekend = settings?.showWeekend ?: false
            val bitmap = TimetableBitmapRenderer.renderTimetableBitmap(
                context = context,
                courses = allSemesterCourses,
                timeSlots = timeSlots,
                showWeekend = showWeekend,
                width = 720,
                height = 480
            )

            val views = RemoteViews(context.packageName, R.layout.widget_timetable)
            views.setTextViewText(R.id.widget_semester_name, semesterName)
            views.setImageViewBitmap(R.id.widget_timetable_image, bitmap)

            // Clicking on the widget or button opens the app to this semester's timetable
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_SEMESTER_ID", targetSemesterId)
                data = android.net.Uri.parse("timetable://widget/$appWidgetId/open")
            }
            val pendingOpenIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingOpenIntent)
            views.setOnClickPendingIntent(R.id.widget_btn_open, pendingOpenIntent)
            views.setOnClickPendingIntent(R.id.widget_timetable_image, pendingOpenIntent)

            return views
        }
    }
}
