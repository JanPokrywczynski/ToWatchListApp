package com.example.moviestowatchlist.data.local.Episodes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.moviestowatchlist.data.local.Series.SeriesEntity

/**
 * Represents a single episode of a TV series in the local Room database.
 * Each episode is linked to its parent series via a foreign key (seriesId).
 */
@Entity(
    tableName = "episodes",
    foreignKeys = [ForeignKey(
        entity = SeriesEntity::class,
        parentColumns = ["imdbId"],
        childColumns = ["seriesId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["seriesId"])]
)
data class EpisodesEntity(


    /** Unique IMDb identifier for the episode, serves as the primary key. */
    @PrimaryKey val imdbId: String,

    /** Title of the episode. */
    val title: String,

    /** Season number the episode belongs to. */
    val season: Int,

    /** Episode number within the season. */
    val episode: Int,

    /** Release date as a string (can be null if not available). */
    val released: String?,

    /** Runtime as a string (e.g., "45 min"). Nullable. */
    val runtime: String?,

    /** Short plot or description of the episode. Nullable. */
    val plot: String?,

    /** Foreign key referencing the IMDb ID of the parent series. */
    val seriesId: String,

    /** Indicates whether the user has marked this episode as watched. */
    val watched: Boolean = false,

    /** Indicates whether detailed metadata has been fetched (e.g., plot, runtime). */
    val detailsFetched: Boolean = false
)