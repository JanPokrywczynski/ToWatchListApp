package com.example.moviestowatchlist.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class representing detailed information about a movie, series, or episode
 * retrieved from the OMDb API via the `i=<imdbID>` query.
 */
@JsonClass(generateAdapter = true)
data class ContentDetail(

    /** Title of the content (movie, series, episode). */
    @Json(name = "Title") val title: String?,

    /** Comma-separated genre(s) (e.g., "Drama, Sci-Fi"). */
    @Json(name = "Genre") val genre: String?,

    /** Type of content ("movie", "series", "episode"). */
    @Json(name = "Type") val type: String?,

    /** Main cast list. */
    @Json(name = "Actors") val actors: String?,

    /** Plot summary or episode description. */
    @Json(name = "Plot") val plot: String?,

    /** Poster image URL. */
    @Json(name = "Poster") val poster: String?,

    /** Unique IMDb identifier. */
    @Json(name = "imdbID") val imdbId: String,

    /** Runtime duration (e.g., "42 min"). */
    @Json(name = "Runtime") val runtime: String?,

    /** Total number of seasons (only for series). */
    @Json(name = "totalSeasons") val totalSeasons: String?,

    /** Release date (e.g., "12 Jan 2018"). */
    @Json(name = "Released") val released: String?,

    /** List of ratings from multiple sources (e.g., IMDb, Rotten Tomatoes). */
    @Json(name = "Ratings") val ratings: List<Rating>?,

    /** Response status: "True" or "False". Used for error handling. */
    @Json(name = "Response") val response: String?
)

/**
 * Represents a single rating from a specific source such as IMDb or Metacritic.
 */
@JsonClass(generateAdapter = true)
data class Rating(
    /** Name of the rating source (e.g., "Internet Movie Database"). */
    @Json(name = "Source") val source: String,

    /** Value of the rating (e.g., "7.9/10", "94%"). */
    @Json(name = "Value") val value: String
)