package com.example.moviestowatchlist.ui.screens.SearchScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviestowatchlist.BuildConfig
import com.example.moviestowatchlist.data.remote.SearchResult
import com.example.moviestowatchlist.data.remote.retrofit.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import android.util.Log


/**
 * Represents the UI state for the search screen.
 */
sealed interface SearchUiState {
    /** No search has been initiated yet. */
    object Idle : SearchUiState

    /** A search query is currently in progress. */
    object Loading : SearchUiState

    /** Search completed successfully with results. */
    data class Success(val results: List<SearchResult>) : SearchUiState

    /** An error occurred during the search. */
    data class Error(val message: String) : SearchUiState
}



/**
 * ViewModel responsible for executing movie/series search queries using OMDb API.
 * Holds a UI state flow to represent loading, success or error states.
 */
class SearchViewModel : ViewModel() {
    /** Internal mutable state holding the current UI state of the search screen. */
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    /** Public immutable state observed by the UI. */
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /** Reference to the Retrofit API service. */
    private val api = RetrofitClient.apiService


    /**
     * Initiates a search query using the given title and type (movie or series).
     *
     * @param query The title of the movie/series to search.
     * @param type The content type to search ("movie" or "series").
     */
    fun search(query: String, type: String) {
        if (query.isBlank()) {
            Log.w("SearchViewModel", "Search query is blank, ignoring request.")
            return
        }
        Log.d("SearchViewModel", "Initiating search for query='$query', type='$type'")
        _uiState.value = SearchUiState.Loading

        viewModelScope.launch {
            try {
                val response = api.searchByType(
                    query = query,
                    type = type,
                    apiKey = BuildConfig.OMDB_API_KEY
                ).awaitResponse()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.response == "True" && body.results.isNotEmpty()) {
                        Log.d("SearchViewModel", "Search success: ${body.results.size} results.")
                        _uiState.value = SearchUiState.Success(body.results)
                    } else {
                        val errorMsg = body?.error ?: "No results found"
                        Log.w("SearchViewModel", "Search returned no results: $errorMsg")
                        _uiState.value = SearchUiState.Error(errorMsg)
                    }
                } else {
                    val errorMsg = "API error: ${response.code()}"
                    Log.e("SearchViewModel", errorMsg)
                    _uiState.value = SearchUiState.Error(errorMsg)                }
            } catch (e: Exception) {
                val exceptionMsg = e.message ?: "Unexpected error"
                Log.e("SearchViewModel", "Exception during search: $exceptionMsg", e)
                _uiState.value = SearchUiState.Error(exceptionMsg)            }
        }
    }
}