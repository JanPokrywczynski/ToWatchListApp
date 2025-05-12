package com.example.moviestowatchlist.data.local.Episodes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing episodes in the local Room database.
 * This interface defines queries and operations on the `episodes` table.
 */
@Dao
interface EpisodesDao {

    /**
     * Inserts a list of episodes into the database.
     * If an episode already exists (same primary key), it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodesEntity>)


    /**
     * Updates a specific episode entry in the database.
     */
    @Update
    suspend fun updateEpisode(episode: EpisodesEntity)


    /**
     * Returns a stream (Flow) of episodes for a given series ID,
     * ordered by season and episode number.
     */
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY season, episode")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodesEntity>>


    /**
     * Returns a stream (Flow) of a single episode by its IMDb ID.
     */
    @Query("SELECT * FROM episodes WHERE imdbId = :imdbId LIMIT 1")
    fun getEpisodeById(imdbId: String): Flow<EpisodesEntity?>


    /**
     * Returns the next episode that has not been marked as watched
     * (ordered ascending by season and episode).
     */
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND watched = 0 ORDER BY season ASC, episode ASC LIMIT 1")
    suspend fun getNextUnWatchedEpisode(seriesId: String): EpisodesEntity?


    /**
     * Returns the most recently watched episode
     * (ordered descending by season and episode).
     */
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND watched = 1 ORDER BY season DESC, episode DESC LIMIT 1")
    suspend fun getPreviousWatchedEpisode(seriesId: String): EpisodesEntity?


    /**
     * Deletes all episodes from the database.
     */
    @Query("DELETE FROM episodes")
    suspend fun deleteAllEpisodes()


    /**
     * Deletes all episodes associated with the specified series.
     */
    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesForSeries(seriesId: String)
}