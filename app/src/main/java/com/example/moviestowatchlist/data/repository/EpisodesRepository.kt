package com.example.moviestowatchlist.data.repository

import android.util.Log
import com.example.moviestowatchlist.data.local.Episodes.EpisodesDao
import com.example.moviestowatchlist.data.local.Episodes.EpisodesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository layer for managing episode-related operations.
 * Serves as an abstraction over the DAO, providing coroutine context and logging.
 */
class EpisodesRepository(private val dao: EpisodesDao) {

    /**
     * Returns a stream of all episodes for a given series ID.
     */
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodesEntity>> {
        Log.d("EpisodesRepository", "Fetching episodes for seriesId: $seriesId")
        return dao.getEpisodesForSeries(seriesId)
    }

    /**
     * Returns a stream of a specific episode by its IMDb ID.
     */
    fun getEpisodeById(id: String): Flow<EpisodesEntity?> {
        Log.d("EpisodesRepository", "Fetching episode by ID: $id")
        return dao.getEpisodeById(id)
    }

    /**
     * Inserts or replaces a list of episodes in the database.
     */
    suspend fun addEpisodes(episodes: List<EpisodesEntity>) {
        withContext(Dispatchers.IO) {
            Log.d("EpisodesRepository", "Inserting ${episodes.size} episodes")
            dao.insertEpisodes(episodes)
        }
    }

    /**
     * Marks the next unwatched episode in the series as watched.
     * If no unwatched episode is found, logs this fact.
     */
    suspend fun markNextEpisodeAsWatched(seriesId: String) {
        val next = dao.getNextUnWatchedEpisode(seriesId)
        next?.let {
            Log.d("EpisodesRepository", "Marking episode ${it.imdbId} as watched")
            dao.updateEpisode(it.copy(watched = true))
        } ?: Log.d("EpisodesRepository", "No next unwatched episode for seriesId: $seriesId")
    }

    /**
     * Marks the last watched episode in the series as unwatched.
     * If no watched episode is found, logs this fact.
     */
    suspend fun markPreviousEpisodeAsUnwatched(seriesId: String) {
        val previous = dao.getPreviousWatchedEpisode(seriesId)
        previous?.let {
            Log.d("EpisodesRepository", "Marking episode ${it.imdbId} as unwatched")
            dao.updateEpisode(it.copy(watched = false))
        } ?: Log.d("EpisodesRepository", "No previously watched episode for seriesId: $seriesId")
    }



    /**
     * Deletes all episodes associated with a specific series.
     */
    suspend fun clearEpisodesForSeries(seriesId: String) {
        withContext(Dispatchers.IO) {
            Log.d("EpisodesRepository", "Deleting episodes for seriesId: $seriesId")
            dao.deleteEpisodesForSeries(seriesId)
        }
    }
}