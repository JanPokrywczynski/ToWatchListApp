package com.example.moviestowatchlist.data.repository

import android.util.Log
import com.example.moviestowatchlist.data.local.Series.SeriesDao
import com.example.moviestowatchlist.data.local.Series.SeriesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository responsible for managing local series data.
 * Acts as an abstraction over the SeriesDao and adds coroutine context and logging.
 */
class SeriesRepository(private val dao: SeriesDao) {

    /** Flow representing the full list of stored series. */
    val seriesFlow: Flow<List<SeriesEntity>> = dao.getAllSeries()

    /**
     * Retrieves a single series by its IMDb ID.
     */
    fun getSeriesById(id: String): Flow<SeriesEntity?> {
        Log.d("SeriesRepository", "Fetching series by ID: $id")
        return dao.getSeriesById(id)
    }

    /**
     * Inserts a new series into the database.
     * If a series with the same ID exists, it will be replaced.
     */
    suspend fun addSeries(series: SeriesEntity) {
        withContext(Dispatchers.IO) {
            Log.d("SeriesRepository", "Inserting series: ${series.imdbId}")
            dao.insertSeries(series)
        }
    }

    /**
     * Deletes a specific series from the database.
     */
    suspend fun deleteSeries(series: SeriesEntity) {
        withContext(Dispatchers.IO) {
            Log.d("SeriesRepository", "Deleting series: ${series.imdbId}")
            dao.deleteSeries(series)
        }
    }

    /**
     * Deletes all series from the database.
     */
    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            Log.d("SeriesRepository", "Clearing all series from database")
            dao.deleteAllSeries()
        }
    }
}