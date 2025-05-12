package com.example.moviestowatchlist.data.local.repository

import android.util.Log
import com.example.moviestowatchlist.data.local.Movies.MoviesDao
import com.example.moviestowatchlist.data.local.Movies.MoviesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository that provides an abstraction layer over the MoviesDao.
 * Responsible for executing and logging movie-related database operations.
 */
class MoviesRepository(private val dao: MoviesDao) {

    /** Flow of all stored movies. Automatically updates when database changes. */
    val moviesFlow: Flow<List<MoviesEntity>> = dao.getAllMovies()

    /**
     * Retrieves a specific movie by its IMDb ID.
     */
    fun getMovieById(id: String): Flow<MoviesEntity?> {
        Log.d("MoviesRepository", "Fetching movie by ID: $id")
        return dao.getMovieById(id)
    }

    /**
     * Adds a movie to the database. Skips insertion if already exists.
     */
    suspend fun addMovie(movie: MoviesEntity) {
        withContext(Dispatchers.IO) {
            Log.d("MoviesRepository", "Inserting movie: ${movie.imdbId}")
            dao.insertMovie(movie)
        }
    }

    /**
     * Deletes a specific movie from the database.
     */
    suspend fun deleteMovie(movie: MoviesEntity) {
        withContext(Dispatchers.IO) {
            Log.d("MoviesRepository", "Deleting movie: ${movie.imdbId}")
            dao.deleteMovie(movie)
        }
    }

    /**
     * Updates an existing movie entry in the database.
     */
    suspend fun updateMovie(movie: MoviesEntity) {
        withContext(Dispatchers.IO) {
            Log.d("MoviesRepository", "Updating movie: ${movie.imdbId}")
            dao.updateMovie(movie)
        }
    }

    /**
     * Deletes all movies from the local database.
     */
    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            Log.d("MoviesRepository", "Clearing all movies from the database")
            dao.deleteAllMovies()
        }
    }
}