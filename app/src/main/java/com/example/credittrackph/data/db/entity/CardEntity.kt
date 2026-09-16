package com.example.credittrackph.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.credittrackph.data.model.Bank
import com.example.credittrackph.data.model.CardType

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,               // User-defined card name (encrypted)
    val lastFourDigits: String,      // Only last 4 digits (encrypted)
    val cardType: CardType,
    val bank: Bank,
    val cardColorArgb: Long,         // Card background color as ARGB long
    val creditLimit: Double,
    val billingCutoffDay: Int,       // Day of month billing cuts off (1-31)
    val dueDay: Int,                 // Day of month payment is due (1-31)
    val createdAt: Long = System.currentTimeMillis()
)
