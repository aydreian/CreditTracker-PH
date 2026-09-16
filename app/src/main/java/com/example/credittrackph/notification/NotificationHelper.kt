package com.example.credittrackph.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.credittrackph.MainActivity
import com.example.credittrackph.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_DUE_DATE = "due_date_reminders"
        const val CHANNEL_NEW_TRANSACTION = "new_transaction"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannels() {
        val dueDateChannel = NotificationChannel(
            CHANNEL_DUE_DATE,
            "Due Date Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alerts when credit card payments are due soon" }

        val transactionChannel = NotificationChannel(
            CHANNEL_NEW_TRANSACTION,
            "New Transaction Detected",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "New bank transactions detected from SMS" }

        notificationManager.createNotificationChannel(dueDateChannel)
        notificationManager.createNotificationChannel(transactionChannel)
    }

    fun sendDueDateNotification(cardLabel: String, amount: Double, daysLeft: Int, notifId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val urgency = when {
            daysLeft <= 1 -> "TODAY!"
            daysLeft <= 3 -> "in $daysLeft days"
            else -> "in $daysLeft days"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_DUE_DATE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Payment Due $urgency")
            .setContentText("$cardLabel: ₱${String.format("%,.2f", amount)} due $urgency")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notifId, notification)
    }

    fun sendNewTransactionNotification(merchant: String, amount: Double, notifId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_TRANSACTION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💳 New Transaction Detected")
            .setContentText("$merchant: ₱${String.format("%,.2f", amount)} - Tap to review")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notifId, notification)
    }
}
