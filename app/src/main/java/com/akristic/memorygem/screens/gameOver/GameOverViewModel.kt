package com.akristic.memorygem.screens.gameOver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akristic.memorygem.database.GemDatabaseDao
import kotlinx.coroutines.*


class GameOverViewModel(
    level: Int,
    finalScore: Int,
    val database: GemDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private var loadDataJob: Job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + loadDataJob)

    private val _level = MutableLiveData<Int>()
    val level: LiveData<Int>
        get() = _level

    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    init {
        _score.value = finalScore
        _level.value = level

    }

    fun onGameOver() {
        uiScope.launch {
            clearCurrentGame()
        }
    }
    /**
     * delete current game table from database
     */
    private suspend fun clearCurrentGame() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadDataJob.cancel()
    }
}