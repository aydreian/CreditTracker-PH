package com.example.credittrackph.di

import android.content.Context
import androidx.room.Room
import com.example.credittrackph.data.db.CreditTrackDatabase
import com.example.credittrackph.data.db.dao.CardDao
import com.example.credittrackph.data.db.dao.ExpenseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database module — currently using unencrypted Room for the initial build.
 * SQLCipher encryption will be re-enabled once the base build is stable.
 * The EncryptionManager is still used for field-level encryption of sensitive text.
 */
import com.example.credittrackph.data.db.dao.ProfileDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CreditTrackDatabase {
        return Room.databaseBuilder(
            context,
            CreditTrackDatabase::class.java,
            "credittrack.db"
        )
            .addMigrations(CreditTrackDatabase.MIGRATION_1_2, CreditTrackDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideCardDao(db: CreditTrackDatabase): CardDao = db.cardDao()

    @Provides
    fun provideExpenseDao(db: CreditTrackDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideProfileDao(db: CreditTrackDatabase): ProfileDao = db.profileDao()
}
