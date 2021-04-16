package com.mebk.pan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mebk.pan.R

fun createNotification(context: Context, msg: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = NOTIFICATION_DOWNLOAD_NAME
        val describe = NOTIFICATION_DOWNLOAD_DESCRIBE
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_DOWNLOAD_CHANNEL_ID, name, importance).apply {
            description = describe
        }
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    val builder = NotificationCompat.Builder(context, NOTIFICATION_DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_DOWNLOAD_TITLE)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    NotificationManagerCompat.from(context).notify(NOTIFICATION_DOWNLOAD_ID, builder.build())
}