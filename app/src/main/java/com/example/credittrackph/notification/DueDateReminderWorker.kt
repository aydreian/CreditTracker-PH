package com.example.credittrackph.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.credittrackph.data.repository.ExpenseRepository
import com.example.credittrackph.data.repository.CardRepository
import com.example.credittrackph.domain.calculator.DueDateCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

@HiltWorker
class DueDateReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val cardRepository: CardRepository,
    private val notificationHelper: NotificationHelper,
    private val dueDateCalculator: DueDateCalculator
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            notificationHelper.createChannels()
            val now = System.currentTimeMillis()
            val sevenDays = now + (7 * 24 * 60 * 60 * 1000L)
            val upcomingExpenses = expenseRepository
                .getUpcomingDueExpenses(now, sevenDays)
                .firstOrNull() ?: emptyList()

            val cards = cardRepository.getAllCards().firstOrNull() ?: emptyList()

            upcomingExpenses.forEachIndexed { index, expense ->
                val card = cards.find { it.id == expense.cardId }
                val cardLabel = card?.label ?: "Unknown Card"
                val daysLeft = ((expense.dueDate - now) / (24 * 60 * 60 * 1000L)).toInt()
                    .coerceAtLeast(0)
                notificationHelper.sendDueDateNotification(
                    cardLabel = cardLabel,
                    amount = expense.monthlyAmortization,
                    daysLeft = daysLeft,
                    notifId = 1000 + index
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DueDateReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "due_date_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
