package com.example.credittrackph.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.credittrackph.data.repository.CardRepository
import com.example.credittrackph.data.repository.ExpenseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cardRepository: CardRepository,
    private val expenseRepository: ExpenseRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            notificationHelper.createChannels()
            val cards = cardRepository.getAllCards().firstOrNull() ?: emptyList()
            val cappedCards = cards.filter { it.monthlyBudgetCap > 0.0 }
            if (cappedCards.isEmpty()) return Result.success()

            val allExpenses = expenseRepository.getAllExpenses().firstOrNull() ?: emptyList()
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)

            cappedCards.forEachIndexed { index, card ->
                val spentThisMonth = allExpenses.filter { exp ->
                    val expCal = Calendar.getInstance().apply { timeInMillis = exp.purchaseDate }
                    exp.cardId == card.id &&
                        expCal.get(Calendar.YEAR) == currentYear &&
                        expCal.get(Calendar.MONTH) == currentMonth
                }.sumOf { it.monthlyAmortization }

                val ratio = spentThisMonth / card.monthlyBudgetCap
                if (ratio >= 0.80) {
                    notificationHelper.sendBudgetAlertNotification(
                        cardLabel = card.label,
                        spent = spentThisMonth,
                        cap = card.monthlyBudgetCap,
                        notifId = 2000 + index
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BudgetAlertWorker>(
                12, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "budget_alert_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
