package com.akristic.memorygem.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Defines methods for using the GemGame class with Room.
 */
@Dao
interface GemDatabaseDao {

    @Insert
    fun insert(gameOptions: GameOptions)

    @Update
    fun update(gameOptions: GameOptions)

    @Query("SELECT * FROM game_options_table ORDER BY optionsId DESC LIMIT 1")
    fun getGameOptions(): GameOptions?


    @Insert
    fun insert(game: GemGame)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param game new value to write
     */
    @Update
    fun update(game: GemGame)

    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from gem_current_game_table WHERE gameId = :key")
    fun get(key: Long): GemGame?

    /**
     * Deletes all values from the table.
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM gem_current_game_table")
    fun clear()

    /**
     * Selects and returns all rows in the table,
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM gem_current_game_table ORDER BY gameId DESC")
    fun getAllGames(): LiveData<List<GemGame>>

    /**
     * Selects and returns the latest(current) game.
     */
    @Query("SELECT * FROM gem_current_game_table ORDER BY gameId DESC LIMIT 1")
    fun getLastGame(): GemGame?

}

