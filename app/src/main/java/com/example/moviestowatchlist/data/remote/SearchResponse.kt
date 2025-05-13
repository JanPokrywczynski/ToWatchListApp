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


/* https://www.omdbapi.com/?s=batman&
{
  "Search": [
    {
      "Title": "Batman Begins",
      "Year": "2005",
      "imdbID": "tt0372784",
      "Type": "movie",
      "Poster": "https://m.media-amazon.com/images/M/MV5BODIyMDdhNTgtNDlmOC00MjUxLWE2NDItODA5MTdkNzY3ZTdhXkEyXkFqcGc@._V1_SX300.jpg"
    },
    {
      "Title": "The Batman",
      "Year": "2022",
      "imdbID": "tt1877830",
      "Type": "movie",
      "Poster": "https://m.media-amazon.com/images/M/MV5BMmU5NGJlMzAtMGNmOC00YjJjLTgyMzUtNjAyYmE4Njg5YWMyXkEyXkFqcGc@._V1_SX300.jpg"
    },
        ...
  ],
  "totalResults": "607",
  "Response": "True"
}*/