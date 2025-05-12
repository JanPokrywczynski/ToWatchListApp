package com.example.moviestowatchlist.ui.screens.SavedScreen

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.moviestowatchlist.MoviesApplication

/**
 * Provides a custom ViewModel factory for [SavedContentViewModel].
 * This factory enables access to repositories from the [MoviesApplication] context,
 * which is required for dependency injection in the ViewModel.
 */
object SavedContentModelProvider {

    /**
     * A lazily initialized factory used to create an instance of [SavedContentViewModel].
     * Retrieves the application context and injects required repositories.
     */
    val Factory = viewModelFactory {
        initializer {
            // Retrieve application instance from the ViewModel creation context
            val application =
                this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MoviesApplication

            // Construct ViewModel with access to repositories stored in application
            SavedContentViewModel(
                moviesRepository = application.movieRepository,
                seriesRepository = application.seriesRepository,
                episodeRepository = application.episodeRepository
            )
        }
    }
}