package com.example.moviestowatchlist.data.local.Series

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a TV series in the local Room database.
 * Each record corresponds to one unique series identified by its IMDb ID.
 */
@Entity(tableName = "series")
data class SeriesEntity(

    /** Unique IMDb identifier for the series (serves as the primary key). */
    @PrimaryKey val imdbId: String,

    /** Title of the TV series. */
    val title: String,

    /** URL or path to the poster image. Nullable. */
    val poster: String?,

    /** Total number of seasons available for the series. */
    val totalSeasons: Int,

    /** Genre(s) of the series, e.g., "Drama, Sci-Fi". Nullable. */
    val genre: String?,

    /** Date when the user marked the series as watched. Nullable. */
    val watchedDate: String?,

    /** Release date of the first episode or season. Nullable. */
    val released: String?,

    /** Typical runtime per episode (e.g., "45 min"). Nullable. */
    val runtime: String?,

    /** Short description or plot of the series. Nullable. */
    val plot: String?
)