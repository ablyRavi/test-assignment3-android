package com.example.alarmmanagerdemo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ContentInfoCompat.Flags


var NOTIFICATION_ID =1
class ManageNotification(val context: Context) {
    val CHANNEL_ID = "1"

    fun createNotificationChannel(title:String,body:String) {
        if (title.isNotEmpty() && body.isNotEmpty()) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("body", body)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.channel_name)
                val descriptionText = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
//             Register the channel with the system.
                val notificationManager: NotificationManager =
                    context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)

                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID,
                    intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val cancelIntent = Intent(context, MainActivity::class.java)
                cancelIntent.putExtra("title", title)
                cancelIntent.putExtra("body", body)
                cancelIntent.putExtra("action","cancel")

                val cancelPendingIntent = PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID,
                    cancelIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val defaultSoundUri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.bell_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.bell_icon))
                    .setContentTitle(title)
                    ///commit
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    // Set the intent that fires when the user taps the notification.
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.bell,"Cancel",cancelPendingIntent)
                    .setSound(defaultSoundUri)


                with(NotificationManagerCompat.from(context)) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@with
                    }
                    // notificationId is a unique int for each notification that you must define.
                    notify(NOTIFICATION_ID, builder.build())
                    NOTIFICATION_ID++
                }
            }
        }
    }

}