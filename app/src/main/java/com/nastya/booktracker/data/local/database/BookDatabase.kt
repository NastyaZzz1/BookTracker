package com.nastya.booktracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nastya.booktracker.domain.model.Book
import com.nastya.booktracker.domain.model.DailyReading
import com.nastya.booktracker.data.local.dao.DailyReadingDao
import com.nastya.booktracker.data.local.database.TypeConverter
import com.nastya.booktracker.data.local.dao.BookDao

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