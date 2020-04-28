package com.akristic.memorygem.screens.options

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akristic.memorygem.database.GameOptions
import com.akristic.memorygem.database.GemDatabaseDao
import kotlinx.coroutines.*

class OptionsViewModel(
    val database: GemDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    //game difficulty 0 is easy, 1 is normal and 2 is hard
    enum class GameDifficulty(val difficulty: Int) {
        EASY(0),
        NORMAL(1),
        HARD(2)
    }

    //make job to make coroutine so we can load data from database in back threads
    private var loadDataJob: Job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + loadDataJob)
    private var currentOptions = MutableLiveData<GameOptions?>()

    //game difficulty 0 is easy, 1 is normal and 2 is hard
    private val _gameDifficulty = MutableLiveData<Int>()
    val gameDifficulty: LiveData<Int>
        get() = _gameDifficulty

    //if prank mode is enabled is set to 1 and if it is disabled is set to 0
    private val _prankModeEnabled = MutableLiveData<Int>()
    val prankModeEnabled: LiveData<Int>
        get() = _prankModeEnabled

    init {
        initializeCurrentGameOptions()
    }

    private fun initializeCurrentGameOptions() {
        uiScope.launch {
            currentOptions.value = getCurrentGameOptionsFromDatabase()
            setDataFromCurrentGameOptions()
        }
    }

    private suspend fun getCurrentGameOptionsFromDatabase(): GameOptions? {
        return withContext(Dispatchers.IO) {
            val options = database.getGameOptions()
            options
        }
    }

    private fun setDataFromCurrentGameOptions() {
        if (currentOptions.value != null) {
            _gameDifficulty.value = currentOptions.value?.gameDifficulty
            _prankModeEnabled.value = currentOptions.value?.prankMode
        } else {
            _gameDifficulty.value = 1
            _prankModeEnabled.value = 1
        }
    }

    fun prankModeButtonClicked() {
        if (_prankModeEnabled.value == 1) {
            _prankModeEnabled.value = 0
        } else {
            _prankModeEnabled.value = 1
        }
    }

    fun setGameDifficulty(difficulty: Int) {
        _gameDifficulty.value = difficulty
    }

    fun okButtonClicked() {
        uiScope.launch {
            if (currentOptions.value != null){
                val gameOptions = currentOptions.value
                gameOptions?.gameDifficulty = _gameDifficulty.value ?: GameDifficulty.NORMAL.difficulty
                gameOptions?.prankMode = _prankModeEnabled.value ?: 1
                update(gameOptions ?: GameOptions()) // do this in background using suspend
            }else{
                val gameOptions = GameOptions()
                gameOptions.gameDifficulty = _gameDifficulty.value ?: GameDifficulty.NORMAL.difficulty
                gameOptions.prankMode = _prankModeEnabled.value ?: 1
                insert(gameOptions) // do this in background using suspend
            }
       }
    }

    /**
     * insert game options to database in back thread
     */
    private suspend fun insert(gameOptions: GameOptions) {
        withContext(Dispatchers.IO) {
            database.insert(gameOptions)
        }
    }

    /**
     * update game options to database in back thread
     */
    private suspend fun update(gameOptions: GameOptions) {
        withContext(Dispatchers.IO) {
            database.update(gameOptions)
        }
    }
}