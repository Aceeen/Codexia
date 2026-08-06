package com.example.codexiabeta.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.codexiabeta.R

object NotificationHelper {

    const val CHANNEL_ID = "codexia_backup_channel"
    private const val CHANNEL_NAME = "Library Backup"
    private const val CHANNEL_DESC = "Notifications for CSV export and import operations"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW  // silent — no sound or vibration
            ).apply {
                description = CHANNEL_DESC
                setSound(null, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showBackupNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        isOngoing: Boolean = false
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Large icon: the full-color app icon (shown expanded / on some launchers)
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.codexia_android_icon_cropped
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_codexia) // monochrome required by Android
            .setLargeIcon(largeIcon)                          // full-color Codexia icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(isOngoing)
            .setAutoCancel(!isOngoing)
            .build()

        manager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    // Stable IDs for each operation
    const val ID_EXPORT = 1001
    const val ID_IMPORT = 1002
}
