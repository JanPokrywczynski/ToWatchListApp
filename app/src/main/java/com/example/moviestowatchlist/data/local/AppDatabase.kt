package com.example.moviestowatchlist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moviestowatchlist.data.local.Episodes.EpisodesDao
import com.example.moviestowatchlist.data.local.Episodes.EpisodesEntity
import com.example.moviestowatchlist.data.local.Movies.MoviesDao
import com.example.moviestowatchlist.data.local.Movies.MoviesEntity
import com.example.moviestowatchlist.data.local.Series.SeriesDao
import com.example.moviestowatchlist.data.local.Series.SeriesEntity


/**
 * Main Room database for the application.
 * Holds all tables: movies, series, and episodes.
 * Exposes DAOs for accessing each entity type.
 */
@Database(
    entities = [MoviesEntity::class, SeriesEntity::class, EpisodesEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for movie-related operations. */
    abstract fun movieDao(): MoviesDao

    /** DAO for series-related operations. */
    abstract fun seriesDao(): SeriesDao

    /** DAO for episode-related operations. */
    abstract fun episodeDao(): EpisodesDao

    companion object {
        /** Name of the database file. */
        private const val DATABASE_NAME = "content_database"

        /** Singleton instance (thread-safe). */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of the Room database.
         * If the instance does not yet exist, it is created.
         *
         * Uses destructive migration strategy — all data is wiped when schema changes.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // wipe and rebuild DB on schema version change
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}