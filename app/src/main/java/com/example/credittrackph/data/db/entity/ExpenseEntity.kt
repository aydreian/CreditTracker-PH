package com.example.credittrackph.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.data.model.ExpenseSource
import com.example.credittrackph.data.model.InterestType

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = CardEntity::class,
        parentColumns = ["id"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cardId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val merchantName: String,
    val category: ExpenseCategory,
    val amount: Double,
    val currency: String = "PHP",
    val purchaseDate: Long,
    // Installment fields
    val isInstallment: Boolean = false,
    val totalInstallmentMonths: Int = 1,
    val currentInstallmentMonth: Int = 1,
    val interestType: InterestType = InterestType.ZERO_PERCENT,
    val monthlyAmortization: Double = 0.0,
    val totalInterest: Double = 0.0,
    val interestRateMonthly: Double = 0.0,
    // Due date
    val dueDate: Long,
    val isPaid: Boolean = false,
    // Metadata
    val note: String = "",
    val source: ExpenseSource = ExpenseSource.MANUAL,
    val rawSmsText: String = "",
    val profileId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
