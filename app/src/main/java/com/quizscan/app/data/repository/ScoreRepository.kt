package com.quizscan.app.data.repository

import com.quizscan.app.data.local.ScoreDao
import com.quizscan.app.data.local.ScoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ScoreRepository(private val scoreDao: ScoreDao) {

    fun getScoresForUser(userId: String): Flow<List<ScoreEntity>> {
        return scoreDao.getScoresForUser(userId).flowOn(Dispatchers.IO)
    }

    suspend fun saveScore(score: ScoreEntity): Long = withContext(Dispatchers.IO) {
        scoreDao.insertScore(score)
    }
}
