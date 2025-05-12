package com.example.moviestowatchlist.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the full response from OMDb API for a specific season of a TV series.
 * Includes season-level metadata and a list of episodes.
 */
@JsonClass(generateAdapter = true)
data class SeasonDetail(

    /** List of episodes included in the current season. */
    @Json(name = "Episodes") val episodes: List<Episode>?,

    /** Response status: "True" or "False". */
    @Json(name = "Response") val response: String,

    /** Error message if the response is "False". */
    @Json(name = "Error") val error: String? = null
)

/**
 * Represents a single episode entry in the response for a given season.
 */
@JsonClass(generateAdapter = true)
data class Episode(

    /** Episode title (e.g., "Winter Is Coming"). */
    @Json(name = "Title") val title: String?,

    /** Release date (e.g., "17 Apr 2011"). */
    @Json(name = "Released") val released: String?,

    /** Episode number within the season (as string). */
    @Json(name = "Episode") val episode: String?,

    /** IMDb identifier for the episode (e.g., "tt1480055"). */
    @Json(name = "imdbID") val imdbId: String?,

    /** Season number this episode belongs to (as string). */
    @Json(name = "Season") val season: String?,

    /** Full plot (only available when fetched with full details). */
    @Json(name = "Plot") val plot: String?,

    /** Runtime duration of the episode (e.g., "52 min"). */
    @Json(name = "Runtime") val runtime: String?
)