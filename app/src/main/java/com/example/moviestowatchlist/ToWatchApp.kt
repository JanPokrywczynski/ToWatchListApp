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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
 * Handles navigation, top bar, and snackbar state.
 *
 * @param navController Navigation controller for handling screen transitions.
 */
@Composable
fun ToWatchApp(
    navController: NavHostController = rememberNavController()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val screenName = backStackEntry?.destination?.route?.substringBefore("/")
    val currentScreen = ToWatchDestinations.valueOf(screenName ?: ToWatchDestinations.SAVED.name)

    var detailTitle by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf("series") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ToWatchTopBar(
                currentScreen = currentScreen,
                dynamicTitle = detailTitle,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {
                    Log.d("ToWatchApp", "Navigating up from ${currentScreen.name}")
                    detailTitle = null
                    navController.navigateUp()
                },
                onSearchClick = {
                    Log.d("ToWatchApp", "Navigating to Search screen")
                    navController.navigate(ToWatchDestinations.SEARCH.name)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        NavHost(
            navController = navController,
            startDestination = ToWatchDestinations.SAVED.name,
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
                .fillMaxSize()
        ) {
            composable(route = ToWatchDestinations.SAVED.name) {
                Log.d("ToWatchApp", "Displaying SavedContentScreen")
                SavedContentScreen(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it },
                    onMoviesClick = { movie ->
                        Log.d("ToWatchApp", "Navigating to Movie details: ${movie.title}")
                        detailTitle = movie.title
                        navController.navigate("${ToWatchDestinations.DETAILS.name}/${movie.imdbId}")
                    },
                    onSeriesClick = { series ->
                        Log.d("ToWatchApp", "Navigating to Series details: ${series.title}")
                        detailTitle = series.title
                        navController.navigate("${ToWatchDestinations.DETAILS.name}/${series.imdbId}")
                    }
                )
            }
            composable(
                route = "${ToWatchDestinations.DETAILS.name}/{imdbId}"
            ) { backStackEntry ->
                val imdbId = backStackEntry.arguments?.getString("imdbId") ?: return@composable
                Log.d("ToWatchApp", "Displaying ContentDetailScreen for $imdbId")
                ContentDetailScreen(
                    imdbId = imdbId,
                )
            }
            composable(route = ToWatchDestinations.SEARCH.name) {
                Log.d("ToWatchApp", "Displaying SearchScreen")
                SearchScreen(onItemClick = { imdbId, title ->
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
            val resolvedTitle = when (currentScreen) {
                ToWatchDestinations.DETAILS -> {
                    stringResource(id = R.string.details, dynamicTitle ?: "")
                }
                else -> stringResource(id = currentScreen.title)
            }
            Text(resolvedTitle)
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (currentScreen != ToWatchDestinations.SEARCH) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
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
    SAVED(R.string.content_to_watch),
    SEARCH(R.string.search),
    DETAILS(R.string.details)
}

