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




/* https://www.omdbapi.com/?t=Game%20of%20Thrones&Season=1
{
  "Title": "Game of Thrones",
  "Season": "1",
  "totalSeasons": "8",
  "Episodes": [
    {
      "Title": "Unaired Original Pilot",
      "Released": "N/A",
      "Episode": "0",
      "imdbRating": "N/A",
      "imdbID": "tt31321401"
    },
    {
      "Title": "Winter Is Coming",
      "Released": "2011-04-17",
      "Episode": "1",
      "imdbRating": "8.9",
      "imdbID": "tt1480055"
    },
    {
      "Title": "The Kingsroad",
      "Released": "2011-04-24",
      "Episode": "2",
      "imdbRating": "8.6",
      "imdbID": "tt1668746"
    },
    {
      "Title": "Lord Snow",
      "Released": "2011-05-01",
      "Episode": "3",
      "imdbRating": "8.5",
      "imdbID": "tt1829962"
    },
    {
      "Title": "Cripples, Bastards, and Broken Things",
      "Released": "2011-05-08",
      "Episode": "4",
      "imdbRating": "8.6",
      "imdbID": "tt1829963"
    },
    {
      "Title": "The Wolf and the Lion",
      "Released": "2011-05-15",
      "Episode": "5",
      "imdbRating": "9.0",
      "imdbID": "tt1829964"
    },
    {
      "Title": "A Golden Crown",
      "Released": "2011-05-22",
      "Episode": "6",
      "imdbRating": "9.1",
      "imdbID": "tt1837862"
    },
    {
      "Title": "You Win or You Die",
      "Released": "2011-05-29",
      "Episode": "7",
      "imdbRating": "9.1",
      "imdbID": "tt1837863"
    },
    {
      "Title": "The Pointy End",
      "Released": "2011-06-05",
      "Episode": "8",
      "imdbRating": "8.9",
      "imdbID": "tt1837864"
    },
    {
      "Title": "Baelor",
      "Released": "2011-06-12",
      "Episode": "9",
      "imdbRating": "9.6",
      "imdbID": "tt1851398"
    },
    {
      "Title": "Fire and Blood",
      "Released": "2011-06-19",
      "Episode": "10",
      "imdbRating": "9.4",
      "imdbID": "tt1851397"
    }
  ],
  "Response": "True"
}*/