package com.akristic.memorygem.screens.game


import android.app.Application
import android.os.CountDownTimer
import android.os.Handler
import androidx.lifecycle.*
import com.akristic.memorygem.database.GemDatabaseDao
import com.akristic.memorygem.database.GemGame
import com.akristic.memorygem.animation.Animation
import com.akristic.memorygem.database.GameOptions
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

//buzz patterns
private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val LIFE_LOST_BUZZ_PATTERN = longArrayOf(0, 1000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

// this is number of lives player has at starting new game
const val GAME_NUMBER_OF_LIVES = 3
//this is number of sequence size player has to get right to pass the first level
const val GAME_LEVEL_PROGRESS_SIZE = 3
//this is the number that increase amount of sequence size player has to get right to pass in next level
const val GAME_LEVEL_PROGRESS_INCREASE = 1

// this is number of tiles on screen, has to be matched with number of buttons on screen
val GAME_NUMBER_OF_TILES = 5

//this is how much time player has to press a button
const val GAME_TIME = 5000L

// this is interval of count down, it is set to 0.1 sec (100 milliseconds)
const val COUNT_DOWN_INTERVAL = 100L

// this is when game is over time
const val DONE_TIME = 0L


/**
 * ViewModel for game fragment
 */
class GameViewModel(
    val database: GemDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    //make job to make coroutine so we can load data from database in back threads
    private var loadDataJob: Job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + loadDataJob)
    private var currentGame = MutableLiveData<GemGame?>()
    private var currentGameOptions = MutableLiveData<GameOptions?>()
    //used so we can increase it when we change difficulty in options
    private var GAME_LEVEL_PROGRESS_INCREASE_DIFFICULTY = GAME_LEVEL_PROGRESS_INCREASE

    private fun initializeCurrentGame() {
        uiScope.launch {
            //first settings than load game
            currentGameOptions.value = getCurrentGameOptionsFromDatabase()
            setDataFromCurrentGameOptions()
            currentGame.value = getCurrentGameFromDatabase()
            setDataFromCurrentGame()
        }
    }

    private fun initializeCurrentGameOptions() {
        uiScope.launch {
            currentGameOptions.value = getCurrentGameOptionsFromDatabase()
            setDataFromCurrentGameOptions()
        }
    }

    private suspend fun getCurrentGameFromDatabase(): GemGame? {
        return withContext(Dispatchers.IO) {
            val game = database.getLastGame()
            game
        }
    }

    private suspend fun getCurrentGameOptionsFromDatabase(): GameOptions? {
        return withContext(Dispatchers.IO) {
            val gameOptions = database.getGameOptions()
            gameOptions
        }
    }


    private val _isPrankEnabled = MutableLiveData<Int>()
    val isPrankEnabled: LiveData<Int>
        get() = _isPrankEnabled

    /**
     * Here you can set how game difficulty affects game
     */
    private fun setDataFromCurrentGameOptions() {
        val gameOptions: GameOptions = currentGameOptions.value ?: GameOptions()
        _levelProgressSize.value =
            GAME_LEVEL_PROGRESS_SIZE + gameOptions.gameDifficulty // easy is plus 0, normal is plus 1, hard is plus 2
        GAME_LEVEL_PROGRESS_INCREASE_DIFFICULTY =
            GAME_LEVEL_PROGRESS_INCREASE + gameOptions.gameDifficulty // same as above
        _gameDifficulty.value = gameOptions.gameDifficulty
        _isPrankEnabled.value = gameOptions.prankMode
    }

    /**
     * load data from database if there is current game or set default value in case
     * there is no data in database. If there is no data that means we started new game.
     * Current game is deleted every time when he hit okey button in GameOver Fragment
     */
    private fun setDataFromCurrentGame() {
        if (currentGame.value != null) {
            _playerCurrentIndex.value = currentGame.value?.currentSequenceIndex
            _score.value = currentGame.value?.currentScore
            _numberOfLives.value = currentGame.value?.livesLeft
            _currentTimeLeft.value = currentGame.value?.timeLeftMilli
            _level.value = currentGame.value?.currentLevel
            _levelProgressSize.value = currentGame.value?.currentSequenceSize
        } else {
            _playerCurrentIndex.value = 0
            _score.value = 0
            _numberOfLives.value = GAME_NUMBER_OF_LIVES
            _currentTimeLeft.value = GAME_TIME
            _level.value = 1
        }

    }

    /**
     * save current stats to database using Coroutines
     */
    private fun saveCurrentGame() {
        uiScope.launch {
            val game = GemGame()
            game.livesLeft = _numberOfLives.value ?: GAME_NUMBER_OF_LIVES
            game.currentScore = _score.value ?: 0
            game.currentSequenceIndex = _playerCurrentIndex.value ?: 0
            game.timeLeftMilli = _currentTimeLeft.value ?: GAME_TIME
            game.currentLevel = _level.value ?: 1
            game.currentSequenceSize = _levelProgressSize.value ?: GAME_LEVEL_PROGRESS_SIZE
            insert(game) // do this in background using suspend
        }
    }

    /**
     * insert game to database in back thread
     */
    private suspend fun insert(gemGame: GemGame) {
        withContext(Dispatchers.IO) {
            database.insert(gemGame)
        }
    }

    // These are the four different types of buzzing in the game. Buzz pattern is the number of
    // milliseconds each interval of buzzing and non-buzzing takes.
    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        LIFE_LOST(LIFE_LOST_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    //number of current lives player has
    private val _numberOfLives = MutableLiveData<Int>()
    val numberOfLives: LiveData<Int>
        get() = _numberOfLives

    // player current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    //player current level
    private val _level = MutableLiveData<Int>()
    val level: LiveData<Int>
        get() = _level

    //how many correct tiles player must press to complete current level;
    private val _levelProgressSize = MutableLiveData<Int>()
    val levelProgressSize: LiveData<Int>
        get() = _levelProgressSize

    //how many correct tiles player is currently collected at current level
    private val _levelProgressGemsCollected = MutableLiveData<Int>()
    val levelProgressGemsCollected: LiveData<Int>
        get() = _levelProgressGemsCollected

    //index in array list buttonsSequence that player is in currently while trying to repeat the sequence
    private val _playerCurrentIndex = MutableLiveData<Int>()
    val playerCurrentIndex: LiveData<Int>
        get() = _playerCurrentIndex

    //sequence of random numbers that player must repeat by pressing right tiles(buttons)
    private val _buttonsSequence = MutableLiveData<ArrayList<Int>>()
    val buttonsSequence: LiveData<ArrayList<Int>>
        get() = _buttonsSequence

    // buttons values for observers from 1 to 5
    private val _buttonClicked = MutableLiveData<Int>()
    val buttonClicked: LiveData<Int>
        get() = _buttonClicked

    //animation values for observers
    private val _showAnimationSequence = MutableLiveData<Boolean>()
    val showAnimationSequence: LiveData<Boolean>
        get() = _showAnimationSequence
    private val _showAnimationLevelComplete = MutableLiveData<Boolean>()
    val showAnimationLevelComplete: LiveData<Boolean>
        get() = _showAnimationLevelComplete

    //trigger for checking Fast Start and Need for speed Achievements
    private val _fastAchievementTrigger = MutableLiveData<Boolean>()
    val fastAchievementTrigger: LiveData<Boolean>
        get() = _fastAchievementTrigger

    //game difficulty, easy=0, normal = 1, hard = 2
    private val _gameDifficulty = MutableLiveData<Int>()
    val gameDifficulty: LiveData<Int>
        get() = _gameDifficulty

    // this is counter for time
    private val countDownTimer: CountDownTimer

    //this is time left that is updated so player can see it
    private val _currentTimeLeft = MutableLiveData<Long>()
    val currentTimeLeft: LiveData<Long>
        get() = _currentTimeLeft
    //this is time for display in fragment
    val currentTimeString = Transformations.map(currentTimeLeft) { time ->
        (time / COUNT_DOWN_INTERVAL).toString()
    }

    //this is for displaying score
    val currentScoreString = Transformations.map(score) { scoreString ->
        scoreString.toString()
    }
    //this is for displaying gems collected (In Gems 0 of 5 this is number 0)
    val currentGemsCollectedString = Transformations.map(levelProgressGemsCollected) { gemsString ->
        gemsString.toString()
    }
    //this is for displaying gems collected (In Gems 0 of 5 this is number 5)
    val currentProgressLevelString = Transformations.map(levelProgressSize) { levelString ->
        levelString.toString()
    }
    // this is for displaying current level
    val currentLevelString = Transformations.map(level) { levelString ->
        levelString.toString()
    }


    //if game is over trigger it with this variable
    private val _isGameOver = MutableLiveData<Boolean>()
    val isGameOver: LiveData<Boolean>
        get() = _isGameOver
    //if life is lost trigger it with this variable
    private val _isLifeLost = MutableLiveData<Boolean>()
    val isLifeLost: LiveData<Boolean>
        get() = _isLifeLost
    // Event that triggers the phone to buzz using different patterns, determined by BuzzType
    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz

    fun onNewGameClearDatabase() {
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


    //initialize start values for variables
    init {
        //default values
        _gameDifficulty.value = 1
        _fastAchievementTrigger.value = false
        _isPrankEnabled.value = 1
        _score.value = 0
        _level.value = 1
        _levelProgressGemsCollected.value = 0
        _levelProgressSize.value = GAME_LEVEL_PROGRESS_SIZE
        _numberOfLives.value = GAME_NUMBER_OF_LIVES
        _playerCurrentIndex.value = 0
        _isGameOver.value = false
        _buttonClicked.value = -1
        _showAnimationSequence.value = false

        countDownTimer = object : CountDownTimer(GAME_TIME, COUNT_DOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTimeLeft.value = millisUntilFinished
            }

            override fun onFinish() {
                _currentTimeLeft.value = DONE_TIME
                _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
            }
        }
    }

    // returns new random button number from 0 to number of buttons on screen
    fun addNewRandom(): Int {
        return Random().nextInt(GAME_NUMBER_OF_TILES)
    }

    // reset game over value and make phone vibrate
    fun gameOverComplete() {
        _isGameOver.value = false
        _eventBuzz.value = BuzzType.GAME_OVER
    }

    // reset life lost value and make phone vibrate
    fun lifeLostComplete() {
        _isLifeLost.value = false
        _eventBuzz.value = BuzzType.LIFE_LOST
    }

    //reset on button clicked values false
    fun onButtonClickedComplete() {
        _buttonClicked.value = -1
    }

    // reset vibrate value to default
    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }

    //triggers for animations
    fun onShowAnimationSequenceComplete() {
        _showAnimationSequence.value = false
    }

    fun onShowAnimationCompleteLevelComplete() {
        _showAnimationLevelComplete.value = false
    }

    // set all values after completing a level
    private fun levelCompleted() {
        _fastAchievementTrigger.value = true
        //stop timer
        stopTimer()
        // clear buttons sequence and make new one
        _buttonsSequence.value?.clear()
        //make new sequence by adding to new random button numbers
        _buttonsSequence.value?.add(addNewRandom())
        _buttonsSequence.value?.add(addNewRandom())
        //increase current level and progress
        _level.value = (_level.value ?: 1) + 1
        _levelProgressSize.value =
            (_levelProgressSize.value ?: GAME_LEVEL_PROGRESS_SIZE) + GAME_LEVEL_PROGRESS_INCREASE_DIFFICULTY
        //reset progress value to start
        _playerCurrentIndex.value = 0
        _levelProgressGemsCollected.value = 0
        _eventBuzz.value = BuzzType.CORRECT
        //save game after every level
        saveCurrentGame()
    }

    fun fastAchievementComplete() {
        _fastAchievementTrigger.value = false
    }

    //trigger observers if button is clicked and animation is not showing
    fun onButtonClicked(buttonNumber: Int) {
        _buttonClicked.value = buttonNumber

    }

    fun startTimer() {
        countDownTimer.start()
    }

    fun stopTimer() {
        countDownTimer.cancel()
    }

    internal fun checkIsRightButtonClicked(buttonClicked: Int) {
        startTimer()
        if (_playerCurrentIndex.value ?: 0 < (_buttonsSequence.value?.size ?: 0)) {

            //checking the right button clicked
            if (buttonsSequence.value?.get(_playerCurrentIndex.value ?: 0) == buttonClicked) {

                if (playerCurrentIndex.value ?: 0 >= levelProgressGemsCollected.value ?: 0) {
                    _levelProgressGemsCollected.value = (_playerCurrentIndex.value ?: 0) + 1
                }
                calculateScore()
                _playerCurrentIndex.value = (_playerCurrentIndex.value ?: 0) + 1
                addNewRandomForLastTile()
            } else {
                lifeLost()
            }
        }
    }

    private fun addNewRandomForLastTile(
    ) {
        if (playerCurrentIndex.value == buttonsSequence.value?.size) {
            if (_levelProgressSize.value == playerCurrentIndex.value) {
                levelCompleted()
                _showAnimationLevelComplete.value = true
                val handler = Handler()
                handler.postDelayed({
                    _showAnimationSequence.value = true
                }, (Animation.Duration.ANIMATION_DURATION.milliseconds * 10))

            } else {
                buttonsSequence.value?.add(addNewRandom())
                _playerCurrentIndex.value = 0
                stopTimer()
                val handlerGetReady = Handler()
                handlerGetReady.postDelayed({
                    _showAnimationSequence.value = true
                }, Animation.Duration.ANIMATION_LAST_TILE_DELAY.milliseconds)
            }
        }
    }


    //set all values for starting new game
    internal fun startNewGame() {
        onNewGameClearDatabase()
        _numberOfLives.value = GAME_NUMBER_OF_LIVES
        _score.value = 0
        _level.value = 1
        _buttonsSequence.value = ArrayList(
            listOf(addNewRandom(), addNewRandom())
        )
        initializeCurrentGameOptions()//set settings for game
        _showAnimationSequence.value = true
    }

    fun resumeGame() {
        initializeCurrentGame()
        _buttonsSequence.value = ArrayList(
            listOf(addNewRandom(), addNewRandom())
        )
        _showAnimationSequence.value = true
    }

    fun addExtraLife() {
        _numberOfLives.value = 1
    }


    // set all values when life is lost
    private fun lifeLost() {
        //reduce number of life's by 1
        _numberOfLives.value = (_numberOfLives.value ?: 0) - 1
        //save the game score and stats before launching life lost or game over fragment
        saveCurrentGame()
        //check is it just life lost or it is game over
        if (_numberOfLives.value ?: 0 > 0) {
            //stop timer
            stopTimer()
            //set index in buttons sequence back to first index 0
            _playerCurrentIndex.value = 0
            //delete old button sequence and start new one with size 2
            buttonsSequence.value?.clear()
            buttonsSequence.value?.add(addNewRandom())
            buttonsSequence.value?.add(addNewRandom())
            //trigger life lost fragment
            _isLifeLost.value = true
        } else {
            stopTimer()
            //set index in buttons sequence back to first index 0
            _playerCurrentIndex.value = 0
            //delete old button sequence and start new one with size 2
            buttonsSequence.value?.clear()
            buttonsSequence.value?.add(addNewRandom())
            buttonsSequence.value?.add(addNewRandom())
            //trigger game over fragment
            _isGameOver.value = true
        }
    }


    private fun calculateScore() {
        //score is 1 for first gem, 2 for second, 3 for third and so on...plus time in seconds left rounded
        _score.value = _score.value?.plus(playerCurrentIndex.value?.plus(1) ?: 0)
            ?.plus((currentTimeLeft.value?.toInt() ?: 0).div(1000))
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        loadDataJob.cancel()
    }


}