package com.quizscan.app

import android.app.Application
import com.quizscan.app.data.local.AppDatabase
import com.quizscan.app.data.repository.QuizRepository
import com.quizscan.app.data.repository.ScoreRepository

class QuizScanApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val quizRepository: QuizRepository by lazy { QuizRepository() }
    val scoreRepository: ScoreRepository by lazy { ScoreRepository(database.scoreDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
