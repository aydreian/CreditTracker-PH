package com.example.credittrackph.data.db.converter

import androidx.room.TypeConverter
import com.example.credittrackph.data.model.*

class Converters {
    @TypeConverter fun fromBank(bank: Bank): String = bank.name
    @TypeConverter fun toBank(name: String): Bank = Bank.valueOf(name)

    @TypeConverter fun fromCardType(type: CardType): String = type.name
    @TypeConverter fun toCardType(name: String): CardType = CardType.valueOf(name)

    @TypeConverter fun fromCategory(cat: ExpenseCategory): String = cat.name
    @TypeConverter fun toCategory(name: String): ExpenseCategory = ExpenseCategory.valueOf(name)

    @TypeConverter fun fromSource(src: ExpenseSource): String = src.name
    @TypeConverter fun toSource(name: String): ExpenseSource = ExpenseSource.valueOf(name)

    @TypeConverter fun fromInterestType(type: InterestType): String = type.name
    @TypeConverter fun toInterestType(name: String): InterestType = InterestType.valueOf(name)
}
