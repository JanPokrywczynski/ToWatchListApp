package com.example.moviestowatchlist

import android.app.Application
import com.example.moviestowatchlist.data.local.AppDatabase
import com.example.moviestowatchlist.data.local.repository.MoviesRepository
import com.example.moviestowatchlist.data.repository.EpisodesRepository
import com.example.moviestowatchlist.data.repository.SeriesRepository

/**
 * Custom Application class used to initialize global resources
 * such as the Room database and repositories for dependency injection.
 */
class MoviesApplication : Application() {

    /** Singleton instance of the Room database, initialized lazily. */
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    /** Repository for managing movie data. */
    lateinit var movieRepository: MoviesRepository
        private set

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