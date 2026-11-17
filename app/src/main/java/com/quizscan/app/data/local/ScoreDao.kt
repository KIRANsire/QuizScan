package com.quizscan.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity): Long

    @Query("SELECT * FROM scores WHERE userId = :userId ORDER BY dateMillis DESC")
    fun getScoresForUser(userId: String): Flow<List<ScoreEntity>>
}
