package com.example.moviestowatchlist.ui.screens.ContentDetailScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviestowatchlist.BuildConfig
import com.example.moviestowatchlist.data.local.Episodes.EpisodesEntity
import com.example.moviestowatchlist.data.local.Movies.MoviesEntity
import com.example.moviestowatchlist.data.local.Series.SeriesEntity
import com.example.moviestowatchlist.data.local.repository.MoviesRepository
import com.example.moviestowatchlist.data.remote.ContentDetail
import com.example.moviestowatchlist.data.remote.Episode
import com.example.moviestowatchlist.data.remote.retrofit.RetrofitClient
import com.example.moviestowatchlist.data.repository.EpisodesRepository
import com.example.moviestowatchlist.data.repository.SeriesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.awaitResponse


/**
 * Represents all possible UI states for the Content Detail screen.
 * Used to render different screen content depending on data loading results.
 */
sealed interface ContentDetailUiState {

    /** Indicates that content is currently being loaded (e.g., from API or Room). */
    object Loading : ContentDetailUiState

    /** Indicates that a movie was successfully loaded from the local database. */
    data class MovieLoaded(val movie: MoviesEntity) : ContentDetailUiState

    /** Indicates that a series was successfully loaded from the local database. */
    data class SeriesLoaded(val series: SeriesEntity) : ContentDetailUiState

    /** Indicates that content was successfully loaded from the OMDb API. */
    data class Success(val content: ContentDetail) : ContentDetailUiState

    /** Indicates that no content was found (either locally or via API). */
    data object NotFound : ContentDetailUiState

    /** Indicates that an error occurred while loading content. */
    data class Error(val message: String) : ContentDetailUiState
}






/**
 * Represents all possible UI states for the episode list section of the screen.
 * Used to display loading, success or error messages for episodes of a specific season.
 */
sealed interface EpisodesUiState {
    /** Indicates that episodes are being loaded from the API. */
    data object Loading : EpisodesUiState

    /** Indicates successful loading of a specific season and its episodes. */
    data class Success(val season: Int, val episodes: List<Episode>) : EpisodesUiState

    /** Indicates that an error occurred while loading episodes. */
    data class Error(val message: String) : EpisodesUiState
}






class ContentDetailViewModel(
    private val moviesRepository: MoviesRepository,
    private val seriesRepository: SeriesRepository,
    private val episodeRepository: EpisodesRepository
) : ViewModel() {

    /** Holds the current UI state of the content detail screen (loading, error, success, etc.). */
    private val _uiState = MutableStateFlow<ContentDetailUiState>(ContentDetailUiState.Loading)
    val uiState: StateFlow<ContentDetailUiState> = _uiState.asStateFlow()

    /** State for the episode section: loading, error, or list of episodes for a season. */
    private val _episodesState = MutableStateFlow<EpisodesUiState>(EpisodesUiState.Loading)
    val episodesState: StateFlow<EpisodesUiState> = _episodesState.asStateFlow()


    /** Indicates whether a movie or series is currently being added to the local database. */
    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    /** Indicates whether the current content is already added to the user's watchlist. */
    private val _isAlreadyAdded = MutableStateFlow(false)
    val isAlreadyAdded: StateFlow<Boolean> = _isAlreadyAdded.asStateFlow()

    /** Holds the set of watched episode IMDb IDs for the current series. */
    private val _watchedEpisodes = MutableStateFlow<Set<String>>(emptySet())
    val watchedEpisodes: StateFlow<Set<String>> = _watchedEpisodes.asStateFlow()

    /** Indicates whether the currently displayed movie has been marked as watched. */
    private val _isWatched = MutableStateFlow(false)
    val isWatched: StateFlow<Boolean> = _isWatched

    /** Contains a list of episodes retrieved from the local database for the current series. */
    private val _localEpisodes = MutableStateFlow<List<EpisodesEntity>>(emptyList())
    val localEpisodes: StateFlow<List<EpisodesEntity>> = _localEpisodes.asStateFlow()





    /**
     * Loads detailed content data (movie or series) by IMDb ID.
     * Tries to fetch from cache first, then from OMDb API, and finally falls back to the local Room database.
     *
     * @param imdbId Unique IMDb identifier for the content.
     */
    fun loadContentById(imdbId: String) {
        // Start a coroutine in the ViewModel's lifecycle-aware scope
        viewModelScope.launch {
            Log.d("ContentDetailViewModel", "Loading content for ID: $imdbId")

            // Attempt to retrieve content from in-memory cache
            val cachedContent = ContentDetailCache.get(imdbId)
            if (cachedContent != null) {
                Log.d(
                    "ContentDetailViewModel",
                    "Cache hit for ID: $imdbId, title: ${cachedContent.title}"
                )

                // Emit success state with cached data to UI
                _uiState.value = ContentDetailUiState.Success(cachedContent)
                return@launch // Exit coroutine early
            }

            // No cache hit — set UI state to loading
            _uiState.value = ContentDetailUiState.Loading
            Log.d("ContentDetailViewModel", "No cache found. Fetching from OMDb API...")

            try {
                // Make API request using Retrofit + Moshi
                val response = RetrofitClient.apiService.getContentDetails(
                    imdbId = imdbId,
                    apiKey = BuildConfig.OMDB_API_KEY
                ).awaitResponse() // Suspend until response is received

                Log.d(
                    "ContentDetailViewModel",
                    "API response received. Code: ${response.code()}, success: ${response.isSuccessful}"
                )


                // If response is valid and content is found
                if (response.isSuccessful && response.body() != null && response.body()?.response != "False") {
                    val contentDetail = response.body()!!

                    Log.d(
                        "ContentDetailViewModel",
                        "API success. Caching content. Title: ${contentDetail.title}"
                    )

                    // Save to cache for future use
                    ContentDetailCache.put(imdbId, contentDetail)

                    // Emit success state with fetched content
                    _uiState.value = ContentDetailUiState.Success(contentDetail)
                } else {

                    // API responded but content not found or response invalid
                    Log.w(
                        "ContentDetailViewModel",
                        "API error or empty response. Falling back to local database."
                    )
                    // Fallback to Room database
                    fetchFromLocalDatabase(imdbId)
                }
            } catch (e: Exception) {
                // Network or unexpected error during API request
                Log.e("ContentDetailViewModel", "API request failed: ${e.localizedMessage}", e)

                // Fallback to Room database
                fetchFromLocalDatabase(imdbId)
            }
        }
    }




    /**
     * Attempts to load content from the local Room database if it could not be retrieved from the API.
     * Checks first for a movie, then for a series. Updates the UI state accordingly.
     *
     * @param imdbId The IMDb identifier of the content to load.
     */
    private suspend fun fetchFromLocalDatabase(imdbId: String) {
        Log.d("ContentDetailViewModel", "Searching local database for ID: $imdbId")

        // Check if the content exists as a movie
        val movie = moviesRepository.getMovieById(imdbId).firstOrNull()
        if (movie != null) {
            Log.d("ContentDetailViewModel", "Movie found locally: ${movie.title}")
            // Update UI state with loaded movie data
            _uiState.value = ContentDetailUiState.MovieLoaded(movie)
            return
        }

        // Check if the content exists as a series
        val series = seriesRepository.getSeriesById(imdbId).firstOrNull()
        if (series != null) {
            Log.d("ContentDetailViewModel", "Series found locally: ${series.title}")

            // Trigger episode loading before setting the UI state
            loadEpisodesForSeries(series.imdbId)

            // Update UI state with loaded series data
            _uiState.value = ContentDetailUiState.SeriesLoaded(series)
            return
        }

        // Neither movie nor series found in the database - emit "Not Found" state
        Log.w(
            "ContentDetailViewModel",
            "No matching content found in local database for ID: $imdbId"
        )
        _uiState.value = ContentDetailUiState.NotFound
    }





    /**
     * Loads all episodes for a given series from the local Room database.
     * Updates the internal state to reflect the current list of episodes.
     *
     * @param seriesId The IMDb ID of the series whose episodes should be loaded.
     */
    fun loadEpisodesForSeries(seriesId: String) {
        viewModelScope.launch {
            Log.d(
                "ContentDetailViewModel",
                "Loading episodes from database for seriesId: $seriesId"
            )

            // Collects a Flow<List<EpisodesEntity>> from the repository
            episodeRepository.getEpisodesForSeries(seriesId).collect { episodes ->
                Log.d("ContentDetailViewModel", "Loaded ${episodes.size} episodes from database")
                // Update the StateFlow used by the UI layer
                _localEpisodes.value = episodes
            }
        }
    }






    /**
     * Loads details of a specific season for a given series from the OMDb API.
     * Updates the UI state with a list of episodes, or an error message if loading fails.
     *
     * @param imdbId The IMDb ID of the series.
     * @param season The season number to fetch.
     */
    fun loadSeason(imdbId: String, season: Int) {
        // Indicate loading state before API call
        _episodesState.value = EpisodesUiState.Loading
        Log.d("ContentDetailViewModel", "Requesting season $season for series ID: $imdbId")

        viewModelScope.launch {
            try {
                // Make API call to fetch season details
                val response = RetrofitClient.apiService.getSeasonDetails(
                    imdbId = imdbId,
                    season = season,
                    apiKey = BuildConfig.OMDB_API_KEY
                ).awaitResponse()

                // Extract response body (SeasonDetail object)
                val seasonDetail = response.body()

                // Check response validity: HTTP success + "Response" field == "True"
                if (response.isSuccessful && seasonDetail?.response == "True" && seasonDetail.episodes != null) {
                    Log.d(
                        "ContentDetailViewModel",
                        "Season $season loaded successfully with ${seasonDetail.episodes.size} episodes."
                    )

                    // Update the UI state with successfully loaded episodes
                    _episodesState.value = EpisodesUiState.Success(
                        season = season,
                        episodes = seasonDetail.episodes
                    )
                } else {
                    // Handle logical error returned by API (e.g., "Series not found.")
                    val errorMessage = seasonDetail?.error ?: "Failed to load episodes."
                    Log.w("ContentDetailViewModel", "API error or empty response: $errorMessage")
                    _episodesState.value = EpisodesUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                val message = e.localizedMessage ?: "Unknown error"
                Log.e("ContentDetailViewModel", "Exception while loading season: $message", e)
                _episodesState.value = EpisodesUiState.Error(message)
            }
        }
    }





    /**
     * Saves a movie to the local Room database using data from the API response.
     * After saving, re-checks whether the item is marked as already added.
     *
     * @param content The detailed content object received from OMDb API.
     */
    fun saveMovieLocally(content: ContentDetail) {
        viewModelScope.launch {
            Log.d(
                "ContentDetailViewModel",
                "Saving movie locally: ${content.title} [${content.imdbId}]"
            )

            val entity = MoviesEntity(
                imdbId = content.imdbId,
                title = content.title ?: "Title not available",
                poster = content.poster.takeIf { it != "N/A" },
                released = content.released,
                runtime = content.runtime,
                genre = content.genre,
                plot = content.plot,
                watchedDate = null
            )

            moviesRepository.addMovie(entity)

            Log.d("ContentDetailViewModel", "Movie saved to database. Checking if already added.")
            checkIfAlreadyAdded(content.imdbId)
        }
    }






    /**
     * Saves a series to the local Room database using content data from OMDb.
     * Also fetches and stores minimal episode metadata for all seasons.
     * After saving, marks the content as added and schedules episode detail enrichment.
     *
     * @param content The detailed content object retrieved from the API.
     */
    fun saveSeriesLocally(content: ContentDetail) {
        viewModelScope.launch {
            Log.d(
                "ContentDetailViewModel",
                "Saving series locally: ${content.title} [${content.imdbId}]"
            )
            _isAdding.emit(true)

            // Determine total number of seasons (fallback to 1 if parsing fails)
            val totalSeasons = content.totalSeasons?.toIntOrNull() ?: 1

            // Map API response to SeriesEntity
            val seriesEntity = SeriesEntity(
                imdbId = content.imdbId,
                title = content.title ?: "No title",
                poster = content.poster.takeIf { it != "N/A" },
                totalSeasons = totalSeasons,
                genre = content.genre,
                watchedDate = null,
                released = content.released,
                runtime = content.runtime,
                plot = content.plot
            )
            seriesRepository.addSeries(seriesEntity)
            Log.d("ContentDetailViewModel", "Series saved to database with $totalSeasons season(s)")

            // Prepare container for episodes
            val allEpisodes = mutableListOf<EpisodesEntity>()

            // For each season, fetch basic episode info from OMDb
            for (season in 1..totalSeasons) {
                try {
                    Log.d("ContentDetailViewModel", "Fetching episodes for season $season")
                    val response = RetrofitClient.apiService.getSeasonDetails(
                        imdbId = content.imdbId,
                        season = season,
                        apiKey = BuildConfig.OMDB_API_KEY
                    ).awaitResponse()

                    val seasonDetail = response.body()
                    if (response.isSuccessful && seasonDetail?.response == "True") {
                        val rawEpisodes = seasonDetail.episodes?.mapNotNull { episode ->
                            val episodeNumber =
                                episode.episode?.toIntOrNull() ?: return@mapNotNull null
                            val imdbId = episode.imdbId ?: return@mapNotNull null
                            EpisodesEntity(
                                imdbId = imdbId,
                                title = episode.title ?: "Untitled episode",
                                season = season,
                                episode = episodeNumber,
                                released = episode.released,
                                runtime = null,         // full details will be fetched later
                                plot = null,
                                seriesId = content.imdbId,
                                watched = false
                            )
                        }.orEmpty()
                        allEpisodes.addAll(rawEpisodes)
                        Log.d(
                            "ContentDetailViewModel",
                            "Season $season: added ${rawEpisodes.size} episodes"
                        )
                    } else {
                        Log.w(
                            "ContentDetailViewModel",
                            "Season $season: failed to load or empty response"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        "ContentDetailViewModel",
                        "Error loading season $season: ${e.localizedMessage}"
                    )
                }
            }

            // Save all collected episodes to the database
            episodeRepository.addEpisodes(allEpisodes)
            Log.d("ContentDetailViewModel", "Saved total of ${allEpisodes.size} episodes")

            _isAdding.emit(false)

            // Update "already added" flag for UI
            checkIfAlreadyAdded(content.imdbId)

            // Launch background job to enrich episode metadata (runtime, plot, etc.)
            enrichEpisodesInBackground(content.imdbId)
        }
    }






    /**
     * Loads all watched episodes for a given series from the local database.
     * Filters only the episodes marked as watched and updates the ViewModel state.
     *
     * @param seriesId The IMDb ID of the series.
     */
    fun loadWatchedEpisodes(seriesId: String) {
        viewModelScope.launch {
            Log.d("ContentDetailViewModel", "Loading watched episodes for seriesId: $seriesId")

            episodeRepository.getEpisodesForSeries(seriesId).collect { all ->
                val watchedIds = all.filter { it.watched }.map { it.imdbId }.toSet()
                _watchedEpisodes.value = watchedIds

                Log.d("ContentDetailViewModel", "Found ${watchedIds.size} watched episodes")
            }
        }
    }





    /**
     * Toggles the watched status of a specific episode.
     * Updates the episode in the database and refreshes the watched episode list for the series.
     *
     * @param imdbId The IMDb ID of the episode to update.
     * @param watched True to mark as watched, false to unmark.
     */
    fun toggleEpisodeWatched(imdbId: String, watched: Boolean) {
        viewModelScope.launch {
            Log.d("ContentDetailViewModel", "Toggling watched = $watched for episode: $imdbId")

            // Retrieve the current episode from the database
            val episode = episodeRepository.getEpisodeById(imdbId).firstOrNull() ?: run {
                Log.w("ContentDetailViewModel", "Episode not found: $imdbId")
                return@launch
            }

            // Create an updated copy with the new watched status
            val updated = episode.copy(watched = watched)

            // Persist the updated episode
            episodeRepository.addEpisodes(listOf(updated))
            Log.d("ContentDetailViewModel", "Episode updated: $imdbId (watched = $watched)")

            // Refresh the UI state with the latest watched episodes
            loadWatchedEpisodes(episode.seriesId)
        }
    }





    /**
     * Checks whether the given movie has been marked as watched (based on non-null watchedDate).
     * Updates the UI state accordingly.
     *
     * @param imdbId The IMDb ID of the movie to check.
     */
    fun checkIfMovieWatched(imdbId: String) {
        viewModelScope.launch {
            Log.d("ContentDetailViewModel", "Checking if movie is watched: $imdbId")

            val movie = moviesRepository.getMovieById(imdbId).firstOrNull()
            val isWatched = movie?.watchedDate != null
            _isWatched.value = isWatched

            Log.d("ContentDetailViewModel", "Movie watched = $isWatched")
        }
    }






    /**
     * Toggles the watched status of a movie based on its current `watchedDate` value.
     * If the movie has not been watched, it sets today's date as the watched date.
     * If it has already been watched, the date is cleared.
     * Updates the local database and UI state accordingly.
     *
     * @param imdbId The IMDb ID of the movie to toggle.
     */
    fun toggleMovieWatched(imdbId: String) {
        viewModelScope.launch {
            Log.d("ContentDetailViewModel", "Toggling watched status for movie: $imdbId")

            // Fetch movie from local database
            val movie = moviesRepository.getMovieById(imdbId).firstOrNull() ?: run {
                Log.w("ContentDetailViewModel", "Movie not found: $imdbId")
                return@launch
            }

            // Toggle watchedDate (add or clear)
            val updated = movie.copy(
                watchedDate = if (movie.watchedDate == null)
                    java.time.LocalDate.now().toString()
                else null
            )

            // Save the updated entity
            moviesRepository.updateMovie(updated)
            _isWatched.value = updated.watchedDate != null

            Log.d(
                "ContentDetailViewModel",
                "Movie ${movie.imdbId} marked as ${if (updated.watchedDate != null) "watched" else "not watched"}"
            )
        }
    }





    /**
     * Checks whether the content (movie or series) with the given IMDb ID
     * has already been added to the local database.
     * Updates the UI state to reflect whether the "Add" button should be active.
     *
     * @param imdbId The IMDb ID of the content to check.
     */
    fun checkIfAlreadyAdded(imdbId: String) {
        viewModelScope.launch {
            Log.d("ContentDetailViewModel", "Checking if content is already added: $imdbId")

            // Check presence in local movie and series tables
            val isMovie = moviesRepository.getMovieById(imdbId).firstOrNull() != null
            val isSeries = seriesRepository.getSeriesById(imdbId).firstOrNull() != null

            // Update ViewModel state
            _isAlreadyAdded.value = isMovie || isSeries

            Log.d(
                "ContentDetailViewModel",
                "Content added status for $imdbId: isMovie=$isMovie, isSeries=$isSeries"
            )
        }
    }





    /**
     * Asynchronously enriches locally stored episode data for a given series.
     * Fetches full details (runtime, plot) from the OMDb API only for episodes
     * that have not been marked as `detailsFetched = true`.
     *
     * @param seriesId The IMDb ID of the series whose episodes should be enriched.
     */
    private fun enrichEpisodesInBackground(seriesId: String) {
        viewModelScope.launch {
            episodeRepository.enrichEpisodesForSeries(seriesId)
        }

    }

}




/**
 * In-memory cache for detailed content (movies or series) retrieved from the OMDb API.
 * Each entry is automatically removed after a fixed time interval (default: 5 minutes).
 */
object ContentDetailCache {

    /** Map storing cached content by IMDb ID. */
    private val cache = mutableMapOf<String, ContentDetail>()

    /** Map of timers for auto-clearing cache entries. One job per cached item. */
    private val clearJobs = mutableMapOf<String, Job>()

    /** Time after which an entry should be cleared from cache (5 minutes). */
    private const val CLEAR_DELAY_MILLIS = 5 * 60 * 1000L

    /**
     * Retrieves cached content for the given IMDb ID, or null if not found.
     */
    fun get(imdbId: String): ContentDetail? {
        Log.d("ContentDetailCache", "Trying to get $imdbId from cache")
        return cache[imdbId]
    }

    /**
     * Stores content in cache for the specified IMDb ID.
     */
    fun put(imdbId: String, content: ContentDetail) {
        cache[imdbId] = content
        Log.d("ContentDetailCache", "Added $imdbId to cache")
    }

    /**
     * Starts a timer to automatically clear the cached content after 5 minutes.
     * If a timer already exists for the given IMDb ID, it is cancelled and restarted.
     */
    fun startClearTimer(imdbId: String) {
        clearJobs[imdbId]?.cancel()

        val job = CoroutineScope(Dispatchers.Default).launch {
            Log.d(
                "ContentDetailCache",
                "Started clear timer for $imdbId (delay: $CLEAR_DELAY_MILLIS ms)"
            )
            delay(CLEAR_DELAY_MILLIS)
            cache.remove(imdbId)
            clearJobs.remove(imdbId)
            Log.d("ContentDetailCache", "Cleared $imdbId from cache after timer")
        }
        clearJobs[imdbId] = job
    }

    /**
     * Cancels the auto-clear timer for the specified IMDb ID.
     * Useful if the item is being reused or permanently stored elsewhere.
     */
    fun cancelClearTimer(imdbId: String) {
        clearJobs[imdbId]?.cancel()
        clearJobs.remove(imdbId)
        Log.d("ContentDetailCache", "Cancelled clear timer for $imdbId")
    }
}