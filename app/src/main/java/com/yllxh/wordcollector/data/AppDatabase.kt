package com.yllxh.wordcollector.data

import android.content.Context
import android.database.Cursor
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

const val DATABASE_NAME = "app_database"
const val DEFAULT_CATEGORY_NAME = "All"

@Database(entities = [Word::class, Category::class], version = 3, exportSchema = false)
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
                Executors.newSingleThreadExecutor().execute {
                    categoryDao?.let {
                        // If there are no categories in the database, insertWord a Category.
                        if (categoryDao.getAnyCategory().isEmpty()) {
                            categoryDao.insert(Category())
                        }
                    }
                }
            }
        }

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    )
                        // Add the migration to the database
                        .addMigrations(migration_1_2)
                        .addMigrations(migration_2_3)
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
        private val migration_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                correctWordCountForAllCategories(database)
            }

            private fun correctWordCountForAllCategories(database: SupportSQLiteDatabase) {
                val categoryCursor = database.query("SELECT name FROM category_table")
                categoryCursor?.use {
                    if (categoryCursor.moveToFirst()) {
                        do {
                            val categoryName = categoryCursor.getString(0)
                            val countCursor = countWordsOfCategory(database, categoryName)
                            countCursor?.use {
                                if (countCursor.moveToFirst()) {
                                    updateCategoryWordCount(countCursor, database, categoryName)
                                }
                            }
                        } while (categoryCursor.moveToNext())
                    }
                }
                correctDefaultCategoryWordCount(database)
            }

            private fun correctDefaultCategoryWordCount(database: SupportSQLiteDatabase){
                val countCursor = database.query("SELECT COUNT(*) FROM word_table")
                countCursor?.use {
                    if (countCursor.moveToFirst()) {
                        updateCategoryWordCount(countCursor, database, DEFAULT_CATEGORY_NAME)
                    }
                }
            }

            private fun updateCategoryWordCount(
                countCursor: Cursor,
                database: SupportSQLiteDatabase,
                categoryName: String?
            ) {
                val wordCount = countCursor.getInt(0)
                updateCategoryWordCount(database, wordCount, categoryName)
            }

            private fun updateCategoryWordCount(
                database: SupportSQLiteDatabase,
                wordCount: Int,
                categoryName: String?
            ) {
                database.execSQL("UPDATE category_table " +
                                        "SET wordCount = $wordCount " +
                                        "WHERE name = '$categoryName'"
                )
            }

            private fun countWordsOfCategory(
                database: SupportSQLiteDatabase,
                categoryName: String?
            ): Cursor? {
                return database.query("SELECT COUNT(category) " +
                            "FROM word_table " +
                            "WHERE category = '$categoryName'"
                )
            }
        }


    }


}