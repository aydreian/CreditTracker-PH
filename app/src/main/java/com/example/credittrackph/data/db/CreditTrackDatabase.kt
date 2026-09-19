package com.example.credittrackph.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.credittrackph.data.db.converter.Converters
import com.example.credittrackph.data.db.dao.CardDao
import com.example.credittrackph.data.db.dao.ExpenseDao
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.db.entity.ExpenseEntity

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.example.credittrackph.data.db.dao.ProfileDao
import com.example.credittrackph.data.db.entity.ProfileEntity

@Database(
    entities = [CardEntity::class, ExpenseEntity::class, ProfileEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CreditTrackDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun profileDao(): ProfileDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isMainUser` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                database.execSQL("ALTER TABLE `expenses` ADD COLUMN `profileId` INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `cards` ADD COLUMN `monthlyBudgetCap` REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}
