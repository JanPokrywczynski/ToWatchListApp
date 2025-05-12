package com.example.moviestowatchlist.ui.screens.SavedScreen

import com.example.moviestowatchlist.data.local.Episodes.EpisodesEntity
import com.example.moviestowatchlist.data.local.Series.SeriesEntity


/**
 * Wrapper data class representing a series along with its associated episodes.
 *
 * @property series The series entity stored locally.
 * @property episodes The list of episodes related to this series.
 */
data class SeriesWithEpisodes(
    val series: SeriesEntity,
    val episodes: List<EpisodesEntity>
) {
    /**
     * Returns the next unwatched episode in chronological order (by season and episode number).
     * If all episodes are watched, returns null.
     */
    val currentEpisode: EpisodesEntity?
        get() = episodes
            .filter { !it.watched }
            .minByOrNull { it.season * 100 + it.episode }

    /**
     * Returns the number of episodes that have been marked as watched.
     */
    val watchedCount: Int
        get() = episodes.count { it.watched }

    /**
     * Returns the total number of episodes in the series.
     */
    val totalEpisodes: Int
        get() = episodes.size
}