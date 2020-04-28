package com.akristic.memorygem.screens.gameOver

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akristic.memorygem.database.GemDatabaseDao

class GameOverModelFactory(
    private val level: Int,
    private val finalScore: Int,
    private val dataSource: GemDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameOverViewModel::class.java)) {
            return GameOverViewModel(level, finalScore, dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}