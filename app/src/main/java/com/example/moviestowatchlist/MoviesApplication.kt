package com.example.moviestowatchlist

import android.app.Application
import com.example.moviestowatchlist.data.local.AppDatabase
import com.example.moviestowatchlist.data.local.repository.MoviesRepository
import com.example.moviestowatchlist.data.repository.EpisodesRepository
import com.example.moviestowatchlist.data.repository.SeriesRepository

/**
 * Custom Application class used to initialize global resources
 * such as the Room database and repositories for dependency injection.
 *
 * This class is instantiated once during the application's lifetime and can
 * be used to expose shared dependencies (e.g., Room database, repositories)
 * to other parts of the app without needing a full dependency injection framework.
 */
class MoviesApplication : Application() {

    /**
     * Lazily initialized singleton instance of the Room database.
     * The database is created only when it is first accessed.
     *
     * Uses the application context to avoid memory leaks and ensure consistent lifecycle.
     */
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    /** Repository for managing movie data. */
    lateinit var movieRepository: MoviesRepository
        private set // Only this class can modify the reference

    /** Repository for managing series data. */
    lateinit var seriesRepository: SeriesRepository
        private set

    /** Repository for managing episode data. */
    lateinit var episodeRepository: EpisodesRepository
        private set

    /**
     * Called when the application is first created.
     * Initializes repository instances using DAOs from the Room database.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize repositories using DAOs from the Room database
        movieRepository = MoviesRepository(database.movieDao())
        seriesRepository = SeriesRepository(database.seriesDao())
        episodeRepository = EpisodesRepository(database.episodeDao())
    }
}