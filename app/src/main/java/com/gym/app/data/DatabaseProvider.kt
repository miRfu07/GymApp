package com.gym.app.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: GymDatabase? = null

    fun getDatabase(context: Context): GymDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                GymDatabase::class.java,
                "gym_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
