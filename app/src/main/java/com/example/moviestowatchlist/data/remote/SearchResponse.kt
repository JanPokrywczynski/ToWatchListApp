package com.example.moviestowatchlist.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
/**
 * Represents the full response returned by OMDb API when performing a search query.
 * Contains a list of results (movies or series), total count, and status info.
 */
@JsonClass(generateAdapter = true)
data class SearchResponse(

    /** List of matched items (movies or series). May be empty if no results found. */
    @Json(name = "Search") val results: List<SearchResult> = emptyList(),

    /** Indicates success or failure: "True" or "False". */
    @Json(name = "Response") val response: String,

    /** Error message if applicable (e.g., "Movie not found!"). Nullable. */
    @Json(name = "Error") val error: String? = null
)