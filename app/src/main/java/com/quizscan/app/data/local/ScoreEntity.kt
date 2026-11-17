package com.quizscan.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val dateMillis: Long,
    val category: String,
    val difficulty: String,
    val score: Int
)
