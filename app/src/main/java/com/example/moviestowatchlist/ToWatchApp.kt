package com.example.moviestowatchlist

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moviestowatchlist.ui.screens.ContentDetailScreen.ContentDetailScreen
import com.example.moviestowatchlist.ui.screens.SavedScreen.SavedContentScreen
import com.example.moviestowatchlist.ui.screens.SearchScreen.SearchScreen


/**
 * Main composable function for the application.
 * Handles navigation, top bar.
 *
 * @param navController Navigation controller for handling screen transitions.
 */
@Composable
fun ToWatchApp(
    navController: NavHostController = rememberNavController()
) {
    /**
     * Retrieves the current destination from the navigation stack
     * and determines the active screen enum for use in UI logic (e.g., top bar title).
     */

    // Get the current entry in the navigation back stack.
    // This state is observable and recomposes when navigation changes.
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Extract the screen name from the current route (e.g., "DETAILS/tt1234567" → "DETAILS").
    // This ensures we work with the screen identifier, not route parameters.
    val screenName = backStackEntry
        ?.destination           // NavDestination: holds route info
        ?.route                 // Route string, e.g., "DETAILS/tt1234567"
        ?.substringBefore("/") // Discard everything after the slash

    // Map the extracted screen name to a value from the ToWatchDestinations enum.
    // If the screen name is null (e.g., before navigation starts), default to SAVED screen.
    val currentScreen = ToWatchDestinations.valueOf(
        screenName ?: ToWatchDestinations.SAVED.name
    )

    /**
     * Stores the dynamic title to be shown in the TopAppBar when viewing details of a movie or series.
     *
     * - Null by default (no details shown).
     * - Set when navigating to the ContentDetailScreen.
     * - Used to personalize the title in the DETAILS screen (e.g., "Inception").
     */
    var detailTitle by remember { mutableStateOf<String?>(null) }
    // Holds the current title of the content selected for detail view (or null if none).


    /**
     * Stores the currently selected content type (e.g., "series" or "movie") in the Saved screen.
     *
     * - Used to filter displayed content.
     * - Default value is "series".
     * - Updated when the user selects a different segment.
     */
    var selectedType by remember { mutableStateOf("series") }
    // Controls filtering logic in SavedContentScreen; determines which type is displayed.


    /**
     * Composable layout scaffold that wraps the main structure of the app:
     * - TopAppBar with title and action buttons
     * - Navigation host for screen routing
     * - Support for padding handling from system bars (status bar, nav bar)
     */
    Scaffold(
        modifier = Modifier.fillMaxSize(), // Makes the scaffold fill the entire screen

        // Top bar of the app — dynamic title, back button, search action
        topBar = {
            ToWatchTopBar(
                currentScreen = currentScreen, // Active screen (used to determine title)
                dynamicTitle = detailTitle, // Optional title when on DETAILS screen
                canNavigateBack = navController.previousBackStackEntry != null, // Show back button only if back stack exists

                // Action performed when the back button is pressed
                navigateUp = {
                    Log.d("ToWatchApp", "Navigating up from ${currentScreen.name}")
                    detailTitle = null // Clear title when leaving DETAILS
                    navController.navigateUp() // Navigate back
                },

                // Action performed when the search button is pressed
                onSearchClick = {
                    Log.d("ToWatchApp", "Navigating to Search screen")
                    navController.navigate(ToWatchDestinations.SEARCH.name) // Navigate to search screen
                }
            )
        },


        ) { innerPadding ->
        // Obtain layout direction (LTR / RTL) to handle padding correctly
        val layoutDirection = LocalLayoutDirection.current

        /**
         * Navigation host responsible for switching between screens based on route names.
         * It applies padding from the Scaffold to avoid overlapping system UI.
         */
        NavHost(
            navController = navController,
            startDestination = ToWatchDestinations.SAVED.name, // Initial screen shown at launch
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
                .fillMaxSize() // Ensures the NavHost fills the space below the TopAppBar
        ) {
            /**
             * SAVED screen — displays user's saved content (movies or series)
             */
            composable(route = ToWatchDestinations.SAVED.name) {
                Log.d("ToWatchApp", "Displaying SavedContentScreen")
                SavedContentScreen(
                    selectedType = selectedType, // Current filter: "movie" or "series"
                    onTypeSelected = { selectedType = it }, // Callback when user switches type

                    // User clicked a movie → show details
                    onMoviesClick = { movie ->
                        Log.d("ToWatchApp", "Navigating to Movie details: ${movie.title}")
                        detailTitle = movie.title // Save title for TopAppBar
                        navController.navigate("${ToWatchDestinations.DETAILS.name}/${movie.imdbId}")
                    },

                    // User clicked a series → show details
                    onSeriesClick = { series ->
                        Log.d("ToWatchApp", "Navigating to Series details: ${series.title}")
                        detailTitle = series.title // Save title for TopAppBar
                        navController.navigate("${ToWatchDestinations.DETAILS.name}/${series.imdbId}")
                    }
                )
            }


            /**
             * DETAILS screen — shows content details based on IMDb ID
             */
            composable(
                route = "${ToWatchDestinations.DETAILS.name}/{imdbId}"
            ) { backStackEntry ->
                // Extract the imdbId argument from the navigation route
                val imdbId = backStackEntry.arguments?.getString("imdbId") ?: return@composable

                Log.d("ToWatchApp", "Displaying ContentDetailScreen for $imdbId")

                // Show detailed information about the selected movie or series
                ContentDetailScreen(
                    imdbId = imdbId,
                )
            }


            /**
             * SEARCH screen — allows searching for new movies/series via the API
             */
            composable(route = ToWatchDestinations.SEARCH.name) {
                Log.d("ToWatchApp", "Displaying SearchScreen")
                SearchScreen(onItemClick = { imdbId, title ->
                    // User clicked a search result → go to details
                    Log.d("ToWatchApp", "Navigating from search to details: $title ($imdbId)")
                    detailTitle = title
                    navController.navigate("${ToWatchDestinations.DETAILS.name}/$imdbId")
                })
            }
        }
    }
}


/**
 * Top app bar composable that dynamically shows a title and optional navigation/search actions.
 *
 * @param currentScreen Currently displayed screen destination.
 * @param dynamicTitle Optional dynamic title (e.g., for detail screen).
 * @param canNavigateBack Whether back navigation is available.
 * @param navigateUp Callback to handle navigation up.
 * @param onSearchClick Callback to open search screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToWatchTopBar(
    currentScreen: ToWatchDestinations,
    dynamicTitle: String?,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    onSearchClick: () -> Unit,
) {
    TopAppBar(
        title = {
            // Determine which title to show in the TopAppBar
            val resolvedTitle = when (currentScreen) {
                // If we're on the DETAILS screen, show dynamic title (e.g., "Inception")
                ToWatchDestinations.DETAILS -> {
                    stringResource(id = R.string.details, dynamicTitle ?: "")
                }

                // For all other screens, use predefined string resource from enum
                else -> stringResource(id = currentScreen.title)
            }

            // Render the resolved title in the TopAppBar
            Text(resolvedTitle)
        },

        // Optional back button shown only when navigation stack allows going back
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, // Automatically mirrored for RTL languages
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },

        // Optional actions in the top bar (search icon for all screens except SEARCH itself)
        actions = {
            if (currentScreen != ToWatchDestinations.SEARCH) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                }
            }

        },
    )
}


/**
 * Enum class that defines the available navigation destinations in the app.
 *
 * @property title Resource ID for the screen title.
 */
enum class ToWatchDestinations(@StringRes val title: Int) {
    SAVED(R.string.content_to_watch), // Home screen with saved movies/series
    SEARCH(R.string.search), // Screen for searching content from API
    DETAILS(R.string.details) // Screen showing movie/series details
}

