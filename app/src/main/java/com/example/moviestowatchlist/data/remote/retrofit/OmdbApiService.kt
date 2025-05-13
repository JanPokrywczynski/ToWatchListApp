package com.example.moviestowatchlist.data.remote.retrofit

import com.example.moviestowatchlist.data.remote.ContentDetail
import com.example.moviestowatchlist.data.remote.SearchResponse
import com.example.moviestowatchlist.data.remote.SeasonDetail
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

/**
 * Singleton Retrofit client for accessing the OMDb API.
 * Uses Moshi for JSON deserialization.
 */
object RetrofitClient {

    /** Base URL for the OMDb API */
    private const val BASE_URL = "https://www.omdbapi.com/"

    /**
     * Builds and returns a configured Retrofit instance.
     *
     * - Uses Moshi as JSON converter
     * - Uses the base OMDb API URL
     */
    private fun getClient(): Retrofit {
        val moshi = Moshi.Builder().build() // Builds a Moshi instance (default adapters)

        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Sets the base URL (e.g., "https://www.omdbapi.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Moshi handles JSON parsing
            .build()
    }

    /**
     * Lazily initialized instance of the OmdbApiService interface.
     * Used to perform API calls to the OMDb service.
     */
    val apiService: OmdbApiService by lazy {
        getClient().create(OmdbApiService::class.java)
    }
}


/**
 * Retrofit interface that defines all accessible OMDb API endpoints.
 */
interface OmdbApiService {

    /**
     * Searches for movies or series based on type.
     *
     * @param query Search string
     * @param type "movie" or "series"
     * @param apiKey Your OMDb API key
     */
    @GET("/") // Root endpoint, parameters passed via @Query
    fun searchByType(
        @Query("s") query: String,
        @Query("type") type: String,
        @Query("apikey") apiKey: String
    ): Call<SearchResponse>

    /**
     * Fetches full content details by IMDb ID (movie, episode or series).
     *
     * @param imdbId IMDb identifier (e.g., "tt1234567")
     * @param plot Level of detail ("full" or "short")
     * @param apiKey Your OMDb API key
     */
    @GET("/")
    fun getContentDetails(
        @Query("i") imdbId: String,
        @Query("plot") plot: String = "full",
        @Query("apikey") apiKey: String
    ): Call<ContentDetail>

    /**
     * Fetches season details for a given series and season number.
     *
     * @param imdbId IMDb ID of the series
     * @param season Season number
     * @param apiKey Your OMDb API key
     */
    @GET("/")
    fun getSeasonDetails(
        @Query("i") imdbId: String,
        @Query("Season") season: Int,
        @Query("apikey") apiKey: String
    ): Call<SeasonDetail>
}