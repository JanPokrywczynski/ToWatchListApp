package com.example.moviestowatchlist.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a single item in the OMDb search result list.
 * Each result contains basic metadata used for list rendering or detail fetching.
 *
 * Returned in the "Search" field of the OMDb search response when querying with "s=".
 */
@JsonClass(generateAdapter = true)
data class SearchResult(

    /** Title of the movie or series (e.g., "Inception"). */
    @Json(name = "Title") val title: String,

    /** Release year or range (e.g., "2010", "2014–2018"). */
    @Json(name = "Year") val year: String,

    /** Unique IMDb ID used to fetch detailed information (e.g., "tt1375666"). */
    @Json(name = "imdbID") val imdbId: String,

    /** Type of content: "movie", "series", or "episode". */
    @Json(name = "Type") val type: String,

    /** URL to the poster image (can be "N/A" if not available). */
    @Json(name = "Poster") val poster: String
)



/* https://www.omdbapi.com/?s=batman

    {
      "Title": "Batman Begins",
      "Year": "2005",
      "imdbID": "tt0372784",
      "Type": "movie",
      "Poster": "https://m.media-amazon.com/images/M/MV5BODIyMDdhNTgtNDlmOC00MjUxLWE2NDItODA5MTdkNzY3ZTdhXkEyXkFqcGc@._V1_SX300.jpg"
    }
*/