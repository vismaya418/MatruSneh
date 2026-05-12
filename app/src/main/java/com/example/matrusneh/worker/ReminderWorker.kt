package com.example.matrusneh.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.matrusneh.MainActivity
import com.example.matrusneh.data.local.MatruSnehDatabase
import java.util.Calendar

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = MatruSnehDatabase.getDatabase(context)
        val user = database.userDao().getUserSync()

        if (user?.remindersEnabled == true) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isEvenDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % 2 == 0
            val message = when (hour) {
                in 5..11 -> if (isEvenDay) "💧 Good Morning Amma! Drink enough water today." else "🌞 Start your day with healthy hydration."
                in 12..17 -> if (isEvenDay) "🥗 Time for a healthy and nutritious meal." else "🍎 Don’t skip your afternoon nutrition."
                in 18..22 -> if (isEvenDay) "😴 Take proper rest for a healthy pregnancy." else "🌙 Time to relax and sleep well."
                else -> "💕 Stay healthy and take care of yourself today."
            }
            showNotification(message)
        }
        
        return Result.success()
    }

    private fun showNotification(message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "matru_sneh_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Health Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for maternal health"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon later
            .setContentTitle("Matru Sneh Reminder 💕")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
