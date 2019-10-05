package com.yllxh.wordcollector.data

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Word::class, Category::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val wordDao: WordDao
    abstract val categoryDao: CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val dbCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val categoryDao: CategoryDao? = INSTANCE?.categoryDao
                Thread {
                    categoryDao?.let {
                        // If there are no categories in the database, insert a Category.
                        if (categoryDao.getAnyCategory().isEmpty()) {
                            categoryDao.insert(Category())
                        }
                    }
                }.start()
            }
        }

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(dbCallback)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}