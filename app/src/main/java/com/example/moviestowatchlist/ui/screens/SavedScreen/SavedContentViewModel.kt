package com.example.moviestowatchlist.ui.screens.SavedScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviestowatchlist.data.local.Movies.MoviesEntity
import com.example.moviestowatchlist.data.local.Series.SeriesEntity
import com.example.moviestowatchlist.data.local.repository.MoviesRepository
import com.example.moviestowatchlist.data.repository.EpisodesRepository
import com.example.moviestowatchlist.data.repository.SeriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Represents the possible UI states for the SavedContent screen.
 * Used to model loading, success (movies/series), empty, and error cases.
 */
sealed interface SavedContentUiState {

    /** UI is currently loading saved content. */
    object Loading : SavedContentUiState

    /** UI successfully loaded a list of saved movies. */
    data class MoviesSuccess(val movies: List<MoviesEntity>) : SavedContentUiState

    /** UI successfully loaded a list of saved series with their associated episodes. */
    data class SeriesWithEpisodesSuccess(val seriesList: List<SeriesWithEpisodes>) :
        SavedContentUiState

    /** UI found no saved content to display. */
    object Empty : SavedContentUiState

    /** UI encountered an error while loading saved content. */
    data class Error(val message: String) : SavedContentUiState
}





/**
 * ViewModel responsible for managing UI state of the SavedContentScreen.
 * Observes locally stored movies or series (with episodes), and updates the state accordingly.
 *
 * @param moviesRepository Repository providing access to saved movies.
 * @param seriesRepository Repository providing access to saved series.
 * @param episodeRepository Repository providing access to saved episodes.
 */
class SavedContentViewModel(
    private val moviesRepository: MoviesRepository,
    private val seriesRepository: SeriesRepository,
    private val episodeRepository: EpisodesRepository
) : ViewModel() {

    /** Currently selected content type: either "movie" or "series". */
    private val _selectedType = MutableStateFlow("series")

    /** Represents the current UI state of the SavedContentScreen. */
    private val _uiState = MutableStateFlow<SavedContentUiState>(SavedContentUiState.Loading)
    val uiState: StateFlow<SavedContentUiState> = _uiState.asStateFlow()


    /** Starts observing the selected type immediately after ViewModel is initialized. */
    init {
        observeContent()
    }


    /**
     * Observes changes in the selected content type and updates the UI state accordingly.
     *
     * If "movie" is selected:
     *   - Observes a list of locally stored movies from the repository.
     *   - Maps it to a MoviesSuccess or Empty UI state.
     *
     * If "series" is selected:
     *   - Calls a separate method to observe series and their episodes.
     *   - Does not emit anything directly in this flow, as state is updated elsewhere.
     *
     * All emitted UI states are collected and pushed to the _uiState flow.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeContent() {

        // Launch a coroutine tied to the lifecycle of the ViewModel.
        // This ensures cleanup when the ViewModel is cleared (e.g., when the screen is closed).
        viewModelScope.launch {

            // Observe changes to the selected content type (e.g., "movie" or "series").
            // Whenever the selected type changes, flatMapLatest cancels the previous flow and starts a new one.
            _selectedType
                .flatMapLatest { type ->
                    // This block will re-execute each time _selectedType.value changes.
                    // Based on the value, a new data flow (e.g., from a repository) will be started.

                    Log.d("SavedContentViewModel", "Selected type changed to: $type")


                    when (type) {
                        "movie" -> {
                            // Map movies list into a MoviesSuccess UI state
                            moviesRepository.moviesFlow.map {
                                Log.d("SavedContentViewModel", "Loaded ${it.size} saved movies.")
                                SavedContentUiState.MoviesSuccess(it)
                            }
                        }


                        "series" -> {
                            // Start observing series separately (state will be updated internally)
                            observeSeriesWithEpisodes()
                            flowOf() // Return an empty flow here — nothing to emit directly
                        }

                        else -> flowOf(SavedContentUiState.Empty)
                    }
                }

                // Catch any exception that occurs in the upstream flow (e.g., from Room or mapping)
                // Logs the error and updates the UI state with an error message to inform the user
                .catch { e ->
                    Log.e("SavedContentViewModel", "Error during content observation: ${e.message}")
                    _uiState.value = SavedContentUiState.Error(e.message ?: "Unknown error")
                }


                // Collect the emitted UI state from the flow and update the screen accordingly
                .collect { state ->

                    // Only handle movie-related state here. Series state is managed in a separate observer.
                    if (state is SavedContentUiState.MoviesSuccess) {
                        _uiState.value =
                            if (state.movies.isEmpty()) {
                                Log.d("SavedContentViewModel", "No saved movies found.")

                                SavedContentUiState.Empty
                            } else {
                                state
                            }
                    } else if (state is SavedContentUiState.Empty) {
                        Log.d("SavedContentViewModel", "Saved content is empty.")
                        _uiState.value = state
                    }
                }
        }
    }





    /**
     * Updates the currently selected type of content.
     * Triggers content observation based on the new type.
     *
     * @param type Either "movie" or "series".
     */
    private fun updateSelectedType(type: String) {
        Log.d("SavedContentViewModel", "updateSelectedType: $type")
        _selectedType.value = type
    }





    /**
     * Marks the next unwatched episode in the given series as watched.
     *
     * @param seriesId IMDb ID of the series.
     */
    fun markNextEpisodeAsWatched(seriesId: String) {
        viewModelScope.launch {
            Log.d("SavedContentViewModel", "Marking next episode as watched for seriesId=$seriesId")
            episodeRepository.markNextEpisodeAsWatched(seriesId)
        }
    }




    /**
     * Marks the last watched episode in the given series as unwatched.
     *
     * @param seriesId IMDb ID of the series.
     */
    fun markPreviousEpisodeAsUnwatched(seriesId: String) {
        viewModelScope.launch {
            Log.d(
                "SavedContentViewModel",
                "Unmarking previous episode as watched for seriesId=$seriesId"
            )
            episodeRepository.markPreviousEpisodeAsUnwatched(seriesId)
        }
    }




    /**
     * Toggles the watched state of a given movie.
     * If the movie was unwatched, it sets the current date as watchedDate.
     * If the movie was watched, it clears the watchedDate.
     *
     * @param movie Movie entity to be toggled.
     */
    fun toggleMovieWatched(movie: MoviesEntity) {
        viewModelScope.launch {
            val updated = movie.copy(
                watchedDate = if (movie.watchedDate == null) java.time.LocalDate.now()
                    .toString() else null
            )
            Log.d(
                "SavedContentViewModel",
                "Toggling watched state for movie=${movie.title}, now watched=${updated.watchedDate != null}"
            )
            moviesRepository.updateMovie(updated)
        }
    }




    /**
     * Deletes a saved movie from the local database.
     *
     * @param movie Movie entity to delete.
     */
    fun deleteMovie(movie: MoviesEntity) {
        viewModelScope.launch {
            Log.d("SavedContentViewModel", "Deleting movie: ${movie.title}")
            moviesRepository.deleteMovie(movie)
        }
    }




    /**
     * Deletes a saved series and all its episodes from the local database.
     *
     * @param series Series entity to delete.
     */
    fun deleteSeries(series: SeriesEntity) {
        viewModelScope.launch {
            Log.d("SavedContentViewModel", "Deleting series: ${series.title}")
            seriesRepository.deleteSeries(series)
            episodeRepository.clearEpisodesForSeries(series.imdbId)
        }
    }





    /**
     * Observes the list of locally stored series and their episodes.
     * For each series, it combines the corresponding list of episodes,
     * updates the UI state, and asynchronously enriches episodes if needed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSeriesWithEpisodes() {
        viewModelScope.launch {
            // Prevent enrichment from being launched multiple times for the same series
            val enrichmentStarted = mutableSetOf<String>()


            // Start observing the list of saved series from the repository.
            seriesRepository.seriesFlow
                .flatMapLatest { seriesList ->
                    // If the list of series is empty, emit an empty UI state and stop.
                    if (seriesList.isEmpty()) {
                        _uiState.value = SavedContentUiState.Empty
                        return@flatMapLatest flowOf(emptyList())
                    }

                    // For each series, retrieve its list of episodes and combine them into SeriesWithEpisodes.
                    // The result is a Flow<List<SeriesWithEpisodes>>.
                    combine(
                        seriesList.map { series ->
                            episodeRepository.getEpisodesForSeries(series.imdbId)
                                .map { episodes ->
                                    SeriesWithEpisodes(series, episodes)
                                }
                        }
                    ) { it.toList() } // Combine result into a single list
                }


                // Collect the combined list of SeriesWithEpisodes and update the UI accordingly.
                .collect { seriesWithEpisodes ->
                    Log.d(
                        "SavedContentViewModel",
                        "Updated series: ${seriesWithEpisodes.map { it.series.title }}"
                    )

                    // Update UI state with loaded series and their episodes
                    _uiState.value = if (seriesWithEpisodes.isEmpty())
                        SavedContentUiState.Empty
                    else
                        SavedContentUiState.SeriesWithEpisodesSuccess(seriesWithEpisodes)



                    // For each series, start background enrichment if it hasn't been started yet.
                    seriesWithEpisodes.forEach { seriesWith ->
                        val seriesId = seriesWith.series.imdbId
                        if (seriesId !in enrichmentStarted) {
                            enrichmentStarted.add(seriesId)
                            enrichEpisodesInBackground(seriesId) // Launch background enrichment
                        }
                    }
                }
        }
    }


    /**
     * Updates the selected content type (either "movie" or "series") to trigger corresponding observation.
     * This will cause the ViewModel to reload the appropriate content flow.
     */
    fun loadContentOfType(type: String) {
        when (type) {
            "movie" -> updateSelectedType("movie")
            "series" -> updateSelectedType("series")
        }
    }


    /**
     * Enriches episode data for a given series in the background.
     * For each episode with incomplete details, it fetches full information from the OMDb API
     * and updates the local database.
     *
     * @param seriesId The IMDb ID of the series for which to enrich episode data.
     */
    private fun enrichEpisodesInBackground(seriesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            episodeRepository.enrichEpisodesForSeries(seriesId)
        }
    }
}