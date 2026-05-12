package com.example.matrusneh.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, KickEntity::class, WaterEntity::class], version = 3, exportSchema = false)
abstract class MatruSnehDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun kickDao(): KickDao
    abstract fun waterDao(): WaterDao

    companion object {
        @Volatile
        private var INSTANCE: MatruSnehDatabase? = null

        fun getDatabase(context: Context): MatruSnehDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MatruSnehDatabase::class.java,
                    "matrusneh_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
