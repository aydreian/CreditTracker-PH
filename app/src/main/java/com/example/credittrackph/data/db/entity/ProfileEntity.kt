package com.example.credittrackph.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isMainUser: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
