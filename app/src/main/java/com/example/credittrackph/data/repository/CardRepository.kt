package com.example.credittrackph.data.repository

import com.example.credittrackph.data.db.dao.CardDao
import com.example.credittrackph.data.db.entity.CardEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao
) {
    fun getAllCards(): Flow<List<CardEntity>> = cardDao.getAllCards()
    fun getCardById(id: Int): Flow<CardEntity?> = cardDao.getCardById(id)
    suspend fun insertCard(card: CardEntity): Long = cardDao.insertCard(card)
    suspend fun updateCard(card: CardEntity) = cardDao.updateCard(card)
    suspend fun deleteCard(card: CardEntity) = cardDao.deleteCard(card)
    suspend fun getCardCount(): Int = cardDao.getCardCount()
}
