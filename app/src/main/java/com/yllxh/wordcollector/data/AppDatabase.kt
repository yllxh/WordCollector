package com.yllxh.wordcollector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Word::class, Category::class], version = 2, exportSchema = false)
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
                        // Add the migration to the database
                        .addMigrations(migration_1_2)
                        .addCallback(dbCallback)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE category_table ADD COLUMN wordCount INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }


}