package com.example.moviestowatchlist.ui.screens.ContentDetailScreen

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.moviestowatchlist.MoviesApplication

/**
 * Provides a ViewModel factory for ContentDetailViewModel,
 * allowing access to application-level repositories via MoviesApplication.
 */
object ContentDetailModelProvider {

    /**
     * A custom ViewModelFactory using the new `viewModelFactory` API.
     * Retrieves repositories from MoviesApplication to inject into ContentDetailViewModel.
     */
    val Factory = viewModelFactory {
        initializer {
            // Access the Application context and cast to MoviesApplication
            val application =
                this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MoviesApplication

            // Return a new instance of ContentDetailViewModel with repositories injected
            ContentDetailViewModel(
                moviesRepository = application.movieRepository,
                seriesRepository = application.seriesRepository,
                episodeRepository = application.episodeRepository,
            )

        }
    }
}