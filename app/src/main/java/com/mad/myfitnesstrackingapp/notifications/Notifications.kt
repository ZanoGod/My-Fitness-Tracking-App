package com.mad.myfitnesstrackingapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CHANNEL_DEFAULT = "channel_default"
    const val CHANNEL_REMINDERS = "channel_reminders"

    fun createAllChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val defaultChannel = NotificationChannel(
                CHANNEL_DEFAULT,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Workout reminders and important alerts"
            }

            manager.createNotificationChannel(defaultChannel)
            manager.createNotificationChannel(reminderChannel)
        }
    }
}
