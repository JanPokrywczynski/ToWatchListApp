package com.example.moviestowatchlist.data.local.Movies

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a movie stored in the local Room database.
 * Each entry corresponds to a single movie the user wants to track.
 */
@Entity(tableName = "movies")
data class MoviesEntity(

    /** Unique IMDb identifier of the movie (serves as primary key). */
    @PrimaryKey val imdbId: String,

    /** Title of the movie. */
    val title: String,

    /** URL to the movie poster. Nullable. */
    val poster: String?,

    /** Release date of the movie. Nullable. */
    val released: String?,

    /** Runtime of the movie (e.g., "120 min"). Nullable. */
    val runtime: String?,

    /** Genre(s) of the movie. Nullable. */
    val genre: String?,

    /** Short plot or description of the movie. Nullable. */
    val plot: String?,

    /** Indicates whether the user has watched the movie. */
    val watched: Boolean = false,

    /** Date when the user marked the movie as watched. Nullable. */
    val watchedDate: String?
)