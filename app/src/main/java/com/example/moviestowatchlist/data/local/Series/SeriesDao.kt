package com.example.moviestowatchlist.data.local.Series

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for interacting with the 'series' table.
 * Provides methods to insert, update, delete, and query TV series in the local Room database.
 */
@Dao
interface SeriesDao {

    /**
     * Inserts a new series into the database.
     * If a series with the same IMDb ID already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)

    /**
     * Deletes a specific series from the database.
     */
    @Delete
    suspend fun deleteSeries(series: SeriesEntity)

    /**
     * Updates an existing series in the database.
     */
    @Update
    suspend fun updateSeries(series: SeriesEntity)

    /**
     * Retrieves all series stored in the database.
     * Returns a Flow to observe changes reactively.
     */
    @Query("SELECT * FROM series")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    /**
     * Retrieves a single series by its IMDb ID.
     * Returns a Flow to observe updates to this entry.
     */
    @Query("SELECT * FROM series WHERE imdbId = :id LIMIT 1")
    fun getSeriesById(id: String): Flow<SeriesEntity?>

    /**
     * Deletes all entries in the 'series' table.
     */
    @Query("DELETE FROM series")
    suspend fun deleteAllSeries()
}