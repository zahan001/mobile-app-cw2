package com.example.cw2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log

@Database(entities = [League::class, Club::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leagueDao(): LeagueDao
    abstract fun clubDao(): ClubDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "football_leagues.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

