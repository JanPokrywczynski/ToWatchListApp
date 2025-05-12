package com.example.moviestowatchlist.data.local.Movies

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing local movie data in the Room database.
 */
@Dao
interface MoviesDao {

    /**
     * Inserts a movie into the database.
     * If a movie with the same ID already exists, it is ignored (not replaced).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovie(movie: MoviesEntity)

    /**
     * Deletes a specific movie from the database.
     */
    @Delete
    suspend fun deleteMovie(movie: MoviesEntity)

    /**
     * Updates the given movie entry in the database.
     */
    @Update
    suspend fun updateMovie(movie: MoviesEntity)

    /**
     * Returns a stream (Flow) of all stored movies.
     */
    @Query("SELECT * FROM movies")
    fun getAllMovies(): Flow<List<MoviesEntity>>

    /**
     * Retrieves a movie by its IMDb ID.
     */
    @Query("SELECT * FROM movies WHERE imdbId = :id LIMIT 1")
    fun getMovieById(id: String): Flow<MoviesEntity?>

    /**
     * Deletes all movies from the database.
     */
    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()
}