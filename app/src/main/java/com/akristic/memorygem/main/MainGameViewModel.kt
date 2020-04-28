package com.akristic.memorygem.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akristic.memorygem.database.GemDatabaseDao
import com.akristic.memorygem.database.GemGame
import kotlinx.coroutines.*



class MainGameViewModel(
    val database: GemDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    //make job to make coroutine so we can load data from database in back threads
    private var loadDataJob: Job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + loadDataJob)
    private var _currentGame = MutableLiveData<GemGame?>()


    val currentGame: LiveData<GemGame?>
        get() = _currentGame



    fun initializeCurrentGame() {
        uiScope.launch {
            _currentGame.value = getCurrentGameFromDatabase()
        }
    }


    private suspend fun getCurrentGameFromDatabase(): GemGame? {
        return withContext(Dispatchers.IO) {
            val game = database.getLastGame()
            game
        }
    }

    private val _isSignIn = MutableLiveData<Boolean>()
    val isSignIn: LiveData<Boolean>
        get() = _isSignIn

    private val _enableExtraLife = MutableLiveData<Boolean>()
    val enableExtraLife: LiveData<Boolean>
        get() = _enableExtraLife

    private val _extraLife = MutableLiveData<Int>()
    val extraLife: LiveData<Int>
        get() = _extraLife
    // if game is started this is how we will know it
    private val _isGameStarted = MutableLiveData<Boolean>()
    val isGameStarted: LiveData<Boolean>
        get() = _isGameStarted

    init {
        initializeCurrentGame()
        _enableExtraLife.value = true
        _isSignIn.value = false
        _extraLife.value = 0
        _isGameStarted.value = false
    }

    fun setIsSignIn(isSignIn: Boolean) {
        _isSignIn.value = isSignIn
    }

    fun addExtraLife(numberOfLives: Int) {
        _extraLife.value = numberOfLives
        _enableExtraLife.value = false
    }

    fun addExtraLifeComplete() {
        _extraLife.value = 0
    }

    fun enableExtraLife() {
        _enableExtraLife.value = true
    }

    fun isGameStarted(value: Boolean) {
        _isGameStarted.value = value
    }
}