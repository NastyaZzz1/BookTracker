package com.nastya.booktracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Book::class, DailyReading::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class BookDatabase : RoomDatabase()  {
    abstract val bookDao: BookDao
    abstract val dailyReadingDao: DailyReadingDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getInstance(context: Context): BookDatabase {
            synchronized(this) {
               var instance = INSTANCE
               if(instance == null) {
                   instance = Room.databaseBuilder(
                       context.applicationContext,
                       BookDatabase::class.java,
                       "book_database"
                   ).build()
                   INSTANCE = instance
               }
                return instance
            }
        }
    }
}