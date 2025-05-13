@file:OptIn(ExperimentalFoundationApi::class)

package com.example.moviestowatchlist.ui.screens.SavedScreen

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.moviestowatchlist.R
import com.example.moviestowatchlist.data.local.Movies.MoviesEntity
import com.example.moviestowatchlist.data.local.Series.SeriesEntity
import kotlinx.coroutines.delay


/**
 * Composable function that displays the main screen for viewing saved movies and series.
 * It allows switching between movie and series tabs, supports swipe gestures,
 * handles lazy loading of content, and provides per-item interactions such as delete, expand, and toggle watched state.
 *
 * @param selectedType The currently selected content type ("movie" or "series").
 * @param onTypeSelected Callback to change the selected content type from the UI (e.g., swipe or tab change).
 * @param onMoviesClick Callback when a movie item is clicked (e.g., navigate to detail view).
 * @param onSeriesClick Callback when a series item is clicked (e.g., navigate to detail view).
 * @param modifier Modifier to apply external padding or styling.
 * @param contentPadding Padding passed from parent layout (usually from Scaffold).
 * @param viewModel The ViewModel responsible for managing the screen's UI state and data logic.
 */
@Composable
fun SavedContentScreen(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    onMoviesClick: (MoviesEntity) -> Unit,
    onSeriesClick: (SeriesEntity) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: SavedContentViewModel = viewModel(factory = SavedContentModelProvider.Factory),
) {
    // Observes the current UI state from the ViewModel (loading, success, error, etc.)
    val uiState by viewModel.uiState.collectAsState()

    // Stores the ID of the currently expanded item (to show delete/share/IMDb buttons)
    var expandedItemId by remember { mutableStateOf<String?>(null) }

    // Stores the last swipe direction (-1 for right-to-left, 1 for left-to-right)
    var swipeDirection by remember { mutableIntStateOf(0) }

    // Loads content corresponding to the selected type every time it changes
    LaunchedEffect(selectedType) {
        Log.d("SavedContentScreen", "Selected type changed: $selectedType")
        viewModel.loadContentOfType(selectedType)
    }




    // Root column layout for the SavedContent screen.
    // It contains the tab bar and the animated content section (movies or series).
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            // Detects horizontal swipe gestures to switch between tabs
            .pointerInput(selectedType) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) {
                        swipeDirection = 1
                        onTypeSelected("movie")
                        Log.d(
                            "SavedContentScreen",
                            "Swiped right → switching to movies"
                        )
                    } else if (dragAmount < -50) {
                        swipeDirection = -1
                        onTypeSelected("series")
                        Log.d(
                            "SavedContentScreen",
                            "Swiped left → switching to series"
                        )
                    }
                }
            }
    ) {


        // Tab row to select between movies and series manually
        PrimaryTextTabs(
            selectedType = selectedType,
            onTabSelected = onTypeSelected,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        /**
         * Animated container that swaps between movie and series lists.
         * Applies a directional slide + fade animation when the tab changes.
         * This makes content transitions visually smooth and intuitive.
         */
        AnimatedContent(
            targetState = selectedType,
            transitionSpec = {
                if (targetState == "movie") {
                    // Transition: slide from left and fade in, when navigating to 'movie' tab
                    (slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(600)
                    ) + fadeIn(animationSpec = tween(800))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(600)
                        ) + fadeOut(animationSpec = tween(800))
                    )
                } else {
                    // Transition: slide from right and fade in, when navigating to 'series' tab
                    (slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(600)
                    ) + fadeIn(animationSpec = tween(800))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(600)
                        ) + fadeOut(animationSpec = tween(800))
                    )
                }
            },
            modifier = Modifier.weight(1f),
        ) { targetType ->


            // React to current UI state emitted from the ViewModel
            when (val currentState = uiState) {
                // Show a loading indicator while content is being fetched
                is SavedContentUiState.Loading -> CircularProgressIndicator()


                // If a list of movies has been successfully loaded
                is SavedContentUiState.MoviesSuccess -> {

                    // Ensure that the current tab is 'movie' before rendering movie list
                    if (targetType == "movie") {
                        val movies = currentState.movies


                        // LazyColumn renders a scrollable list of movie items
                        LazyColumn {
                            items(movies, key = { it.imdbId }) { movie ->
                                // State to control visibility during delete animation
                                var visible by remember(movie.imdbId) { mutableStateOf(true) }

                                // Flag to indicate that deletion was requested
                                var pendingDelete by remember { mutableStateOf(false) }

                                // If user requested deletion, delay and then call delete
                                if (pendingDelete) {
                                    LaunchedEffect(Unit) {
                                        delay(300)
                                        viewModel.deleteMovie(movie)
                                        Log.d(
                                            "SavedContentScreen",
                                            "Movie deleted: ${movie.title}"
                                        )
                                    }
                                }


                                // Animate item disappearance (shrink and fade out)
                                AnimatedVisibility(
                                    visible = visible,
                                    exit = shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        animationSpec = tween(600)
                                    ) + fadeOut(animationSpec = tween(600))
                                ) {
                                    // Render individual movie item with all interactions
                                    MovieItem(
                                        movie = movie,
                                        isExpanded = expandedItemId == movie.imdbId,
                                        onClick = { onMoviesClick(movie) },
                                        onExpandToggle = {
                                            expandedItemId = if (it) movie.imdbId else null
                                        },
                                        onWatchedToggle = {
                                            viewModel.toggleMovieWatched(movie)
                                            Log.d(
                                                "SavedContentScreen",
                                                "Toggled watched: ${movie.title}"
                                            )
                                        },
                                        onDelete = {
                                            visible = false
                                            pendingDelete = true
                                        }
                                    )
                                }
                            }

                            // Add space at the bottom of the list respecting padding from Scaffold
                            item {
                                Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding() + 16.dp))
                            }
                        }
                    } else {
                        // Defensive fallback (unlikely to happen) – mismatch between tab and data
                        Text(stringResource(R.string.loading_movies))
                    }
                }


                // If a list of series (with episodes) has been successfully loaded
                is SavedContentUiState.SeriesWithEpisodesSuccess -> {
                    // Render only if the currently selected tab is 'series'
                    if (targetType == "series") {
                        val seriesList = currentState.seriesList

                        // Scrollable list of series items
                        LazyColumn {
                            items(seriesList, key = { it.series.imdbId }) { seriesWithEpisodes ->

                                // Controls visibility for exit animation during deletion
                                var visible by remember(seriesWithEpisodes.series.imdbId) {
                                    mutableStateOf(
                                        true
                                    )
                                }

                                // Flag indicating a pending delete action
                                var pendingDelete by remember { mutableStateOf(false) }

                                // Trigger deletion with delay to allow animation
                                if (pendingDelete) {
                                    LaunchedEffect(Unit) {
                                        delay(300)
                                        viewModel.deleteSeries(seriesWithEpisodes.series)
                                        Log.d(
                                            "SavedContentScreen",
                                            "Series deleted: ${seriesWithEpisodes.series.title}"
                                        )
                                    }
                                }


                                // Animated disappearance on delete
                                AnimatedVisibility(
                                    visible = visible,
                                    exit = shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        animationSpec = tween(600)
                                    ) + fadeOut(animationSpec = tween(600))
                                ) {
                                    // Render series card with interaction callbacks
                                    SeriesItem(
                                        seriesWithEpisodes = seriesWithEpisodes,
                                        isExpanded = expandedItemId == seriesWithEpisodes.series.imdbId,
                                        onClick = { onSeriesClick(seriesWithEpisodes.series) },
                                        onExpandToggle = {
                                            expandedItemId =
                                                if (it) seriesWithEpisodes.series.imdbId else null
                                        },
                                        onWatchedToggle = {
                                            viewModel.markNextEpisodeAsWatched(seriesWithEpisodes.series.imdbId)
                                            Log.d(
                                                "SavedContentScreen",
                                                "Marked next episode as watched: ${seriesWithEpisodes.series.title}"
                                            )
                                        },
                                        onUnwatchedToggle = {
                                            viewModel.markPreviousEpisodeAsUnwatched(
                                                seriesWithEpisodes.series.imdbId
                                            )
                                            Log.d(
                                                "SavedContentScreen",
                                                "Unmarked last watched episode: ${seriesWithEpisodes.series.title}"
                                            )
                                        },
                                        onDelete = {
                                            visible = false
                                            pendingDelete = true
                                        }
                                    )
                                }
                            }

                            // Extra padding at the bottom of the list to account for Scaffold insets
                            item {
                                Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding() + 16.dp))
                            }
                        }
                    } else {
                        // Defensive fallback – prevents mismatch rendering
                        Text(stringResource(R.string.loading_series))
                    }
                }


                // If there is no saved content to display (empty state)
                is SavedContentUiState.Empty -> {
                    Log.d(
                        "SavedContentScreen",
                        "Empty watchlist for type: $selectedType"
                    )
                    AnimatedContent(
                        targetState = selectedType,
                        transitionSpec = {
                            // Slide/fade animation direction depends on selected type
                            (slideInHorizontally(
                                initialOffsetX = { if (targetState == "movie") -it else it },
                                animationSpec = tween(600)
                            ) + fadeIn(tween(600))) togetherWith
                                    (slideOutHorizontally(
                                        targetOffsetX = { if (targetState == "movie") it else -it },
                                        animationSpec = tween(600)
                                    ) + fadeOut(tween(600)))
                        },
                    ) { target ->
                        // Display icon and guidance text for empty state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (target == "movie") Icons.Default.Movie else Icons.Default.Tv,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No ${if (target == "movie") "movies" else "series"} in your watchlist.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.tap_the_icon_above_to_search_and_add_new_items_to_your_watchlist),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }


                // If an error occurred while loading saved content
                is SavedContentUiState.Error -> {
                    val message = currentState.message
                    Log.e("SavedContentScreen", "Error loading content: $message")
                    Text("Error: $message")
                }
            }
        }
    }
}




/**
 * Composable representing a single saved movie item in the watchlist.
 *
 * The item supports:
 * - Expand/collapse via long click
 * - Toggling the watched status via icon button
 * - Deleting the item with confirmation animation
 * - Showing additional actions (IMDb, share, delete) when expanded
 *
 * @param movie The movie data to be displayed
 * @param isExpanded Whether the item is currently expanded to show action buttons
 * @param onClick Callback for item click (navigates to detail screen)
 * @param onExpandToggle Called when item is long-clicked to expand/collapse
 * @param onWatchedToggle Toggles the watched status of the movie
 * @param onDelete Called when the movie should be deleted from local database
 */
@Composable
fun MovieItem(
    movie: MoviesEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onExpandToggle: (Boolean) -> Unit,
    onWatchedToggle: () -> Unit,
    onDelete: () -> Unit
) {
    // Determine whether the movie has been marked as watched
    val isWatched = movie.watchedDate != null

    // Adjust background color based on watched status
    val backgroundColor = if (isWatched)
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.surface


    val context = LocalContext.current

    // Movie card layout with click and long-click support
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onExpandToggle(!isExpanded) }
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column {
            // Top row with poster and movie info
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .height(IntrinsicSize.Min)
                    .animateContentSize()
            ) {
                // Poster image
                AsyncImage(
                    model = movie.poster,
                    contentDescription = "${movie.title} poster",
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(2f / 3f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(8.dp))


                // Movie details (title, runtime, genre, plot)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(movie.title, style = MaterialTheme.typography.titleMedium)
                    movie.runtime?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    movie.genre?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    movie.plot?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))


                // Watched toggle icon
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    IconButton(onClick = onWatchedToggle) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = if (isWatched) stringResource(R.string.watched) else stringResource(
                                R.string.mark_as_watched
                            )
                        )
                    }
                }
            }


            // Bottom row with action buttons (only visible when expanded)
            AnimatedVisibility(visible = isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { context.openInImdb(movie.imdbId) }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.imdb),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.open_in_imdb))
                    }
                    TextButton(onClick = { context.shareMovie(movie.imdbId) }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.share),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.share))
                    }
                    TextButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}





/**
 * Composable representing a single saved series item with progress and controls.
 *
 * Displays poster, current episode, progress bar, and interactive actions.
 * The card expands on long-press to show IMDb, share, and delete options.
 *
 * @param seriesWithEpisodes The full data object containing series and its episodes
 * @param isExpanded Whether the card is currently expanded
 * @param onClick Callback when the card is clicked (navigates to detail screen)
 * @param onExpandToggle Called to toggle expanded/collapsed state
 * @param onWatchedToggle Called to mark next episode as watched
 * @param onUnwatchedToggle Called to mark previous episode as unwatched
 * @param onDelete Called to delete the series and its episodes
 */
@Composable
fun SeriesItem(
    seriesWithEpisodes: SeriesWithEpisodes,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onExpandToggle: (Boolean) -> Unit,
    onWatchedToggle: () -> Unit,
    onUnwatchedToggle: () -> Unit,
    onDelete: () -> Unit
) {
    // Determine if the series is fully watched
    val isFinished = seriesWithEpisodes.watchedCount == seriesWithEpisodes.totalEpisodes

    // Background color changes based on watched status
    val backgroundColor = if (isFinished)
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.surface

    // Smooth animation for progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = if (seriesWithEpisodes.totalEpisodes == 0) 0f
        else seriesWithEpisodes.watchedCount / seriesWithEpisodes.totalEpisodes.toFloat(),
        animationSpec = tween(durationMillis = 500)
    )

    val context = LocalContext.current

    // Main card for series
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onExpandToggle(!isExpanded) }
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column {
            // Row with image and main information
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .height(IntrinsicSize.Min)
                    .animateContentSize()
            ) {
                // Poster image
                AsyncImage(
                    model = seriesWithEpisodes.series.poster,
                    contentDescription = "${seriesWithEpisodes.series.title} poster",
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(2f / 3f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Series textual information
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        seriesWithEpisodes.series.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    // Display current episode info if available
                    seriesWithEpisodes.currentEpisode?.let { episode ->
                        Text(
                            "S${episode.season}E${episode.episode} • ${episode.title}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            listOfNotNull(
                                episode.runtime,
                                seriesWithEpisodes.series.genre
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress bar based on watched episodes
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Plot or loading message
                    seriesWithEpisodes.currentEpisode?.let { episode ->
                        if (!episode.detailsFetched) {
                            Text(
                                text = stringResource(R.string.fetching_episode_details),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = episode.plot ?: "No description available",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Control buttons: mark watched/unwatched
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    IconButton(onClick = onWatchedToggle) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = if (isFinished) "Watched" else "Mark next episode"
                        )
                    }
                    if (seriesWithEpisodes.watchedCount > 0) {
                        IconButton(onClick = onUnwatchedToggle) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = stringResource(R.string.undo_last_episode)
                            )
                        }
                    }
                }
            }

            // Bottom row with actions (only when expanded)
            AnimatedVisibility(visible = isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { context.openInImdb(seriesWithEpisodes.series.imdbId) }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.imdb),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.open_in_imdb))
                    }
                    TextButton(onClick = { context.shareSeries(seriesWithEpisodes.series.imdbId) }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.share),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.share))
                    }
                    TextButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}





/**
 * Opens the IMDb page for the given content in the browser.
 *
 * @param imdbId The IMDb identifier of the movie or series.
 */
fun Context.openInImdb(imdbId: String) {
    Log.d("IntentAction", "Opening IMDb page for ID: $imdbId")

    // Create a view Intent with the IMDb URL based on the content's ID
    val intent = Intent(Intent.ACTION_VIEW, "https://www.imdb.com/title/$imdbId/".toUri())

    // Launch the intent using the current context (usually an Activity)
    startActivity(intent)
}


/**
 * Shares a link to the given series via available sharing apps.
 *
 * @param imdbId The IMDb identifier of the series.
 */
fun Context.shareSeries(imdbId: String) {
    Log.d("IntentAction", "Sharing series with ID: $imdbId")

    // Create an intent to share plain text
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain" // MIME type for text
        putExtra(Intent.EXTRA_TEXT,
            getString(R.string.check_out_this_series_https_www_imdb_com_title, imdbId))
    }

    // Launch the Android share sheet so the user can pick an app to share with
    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
}


/**
 * Shares a link to the given movie via available sharing apps.
 *
 * @param imdbId The IMDb identifier of the movie.
 */
fun Context.shareMovie(imdbId: String) {
    Log.d("IntentAction", "Sharing movie with ID: $imdbId")

    // Create an intent to share plain text
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain" // MIME type for text
        putExtra(Intent.EXTRA_TEXT,
            getString(R.string.check_out_this_movie_https_www_imdb_com_title, imdbId))
    }

    // Open the system chooser for selecting a sharing app
    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
}


/**
 * Composable displaying a tab row to switch between Movies and Series.
 *
 * @param selectedType The currently selected tab type ("movie" or "series")
 * @param onTabSelected Callback invoked when a tab is clicked
 * @param modifier Modifier to apply layout and styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryTextTabs(
    selectedType: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Define available tabs with internal key and label
    val tabs = listOf(
        "movie" to "Movies",
        "series" to "Series"
    )

    // Determine the currently selected tab index
    val selectedIndex = tabs.indexOfFirst { it.first == selectedType }.coerceAtLeast(0)

    Column(modifier = modifier) {
        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, (type, label) ->
                val icon = when (type) {
                    "movie" -> Icons.Default.Movie
                    "series" -> Icons.Default.Tv
                    else -> null
                }

                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(type) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }
        }
    }
}
