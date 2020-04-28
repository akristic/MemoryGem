package com.akristic.memorygem.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [GemGame::class, GameOptions::class], version = 6, exportSchema = false)
abstract class GemDatabase : RoomDatabase() {

    abstract val gemDatabaseDao: GemDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: GemDatabase? = null

        fun getInstance(context: Context): GemDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            GemDatabase::class.java,
                            "current_game_database"
                    ).fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

