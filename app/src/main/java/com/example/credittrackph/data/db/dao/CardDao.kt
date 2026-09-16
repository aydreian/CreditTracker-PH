package com.example.credittrackph.data.db.dao

import androidx.room.*
import com.example.credittrackph.data.db.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    suspend fun getAllCardsSync(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCardById(cardId: Int): Flow<CardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun getCardCount(): Int
}
