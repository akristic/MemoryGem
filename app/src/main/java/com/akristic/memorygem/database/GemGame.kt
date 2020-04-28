package com.akristic.memorygem.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akristic.memorygem.screens.game.GAME_LEVEL_PROGRESS_SIZE
import com.akristic.memorygem.screens.game.GAME_NUMBER_OF_LIVES
import com.akristic.memorygem.screens.game.GAME_TIME

@Entity(tableName = "gem_current_game_table")
data class GemGame(
    @PrimaryKey(autoGenerate = true)
    var gameId: Long = 0L,

    @ColumnInfo(name = "time_left_milli")
    var timeLeftMilli: Long = GAME_TIME,

    @ColumnInfo(name = "current_score")
    var currentScore: Int = 0,

    @ColumnInfo(name = "current_level")
    var currentLevel: Int = 1,

    @ColumnInfo(name = "current_sequence_size")
    var currentSequenceSize: Int = GAME_LEVEL_PROGRESS_SIZE,

    @ColumnInfo(name = "current_sequence_index")
    var currentSequenceIndex: Int = 0,

    @ColumnInfo(name = "number_of_lives")
    var livesLeft: Int = GAME_NUMBER_OF_LIVES

)

@Entity(tableName = "game_options_table")
data class GameOptions(
    @PrimaryKey(autoGenerate = true)
    var optionsId: Long = 0L,

    @ColumnInfo(name = "game_difficulty")
    var gameDifficulty: Int = 1,

    @ColumnInfo(name = "prank_mode")
    var prankMode: Int = 1

)
