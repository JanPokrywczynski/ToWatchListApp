package com.example.moviestowatchlist.ui.screens.ContentDetailScreen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.moviestowatchlist.data.local.Movies.MoviesEntity
import com.example.moviestowatchlist.data.remote.ContentDetail
import com.example.moviestowatchlist.data.remote.Episode
import com.example.moviestowatchlist.data.remote.Rating



/**
 * Main screen displaying detailed content information based on the given IMDb ID.
 * Handles both movies and series (online or from local storage), episode lists,
 * watched status, and user interactions such as marking content as watched or saving it locally.
 *
 * @param imdbId The IMDb identifier of the movie or series to display.
 * @param viewModel ViewModel providing UI state and business logic for this screen.
 */
@Composable
fun ContentDetailScreen(
    imdbId: String,
    viewModel: ContentDetailViewModel = viewModel(factory = ContentDetailModelProvider.Factory)
) {

    /**
     * Triggered once when the screen is first composed.
     * Loads content by ID, checks if it was added or marked as watched,
     * and cancels the cache clearing timer to keep data in memory.
     */
    LaunchedEffect(imdbId) {
        viewModel.loadContentById(imdbId)
        viewModel.checkIfAlreadyAdded(imdbId)
        viewModel.checkIfMovieWatched(imdbId)
        ContentDetailCache.cancelClearTimer(imdbId)
    }


    /**
     * Called when the composable leaves the composition.
     * Starts a timer to automatically clear cached content after a delay.
     */
    DisposableEffect(Unit) {
        onDispose {
            ContentDetailCache.startClearTimer(imdbId)
        }
    }

    /**
     * Holds the current state of the content detail screen.
     * Determines what to show: loading spinner, movie/series data, error message, etc.
     * Possible values: Loading, Success, MovieLoaded, SeriesLoaded, NotFound, Error.
     */
    val uiState by viewModel.uiState.collectAsState()

    /**
     * Represents the current state of the episode list for the selected series season.
     * Used to populate the episode section or show loading/error for specific seasons.
     * Possible values: Loading, Success(season + episodes), Error(message).
     */
    val episodesUiState by viewModel.episodesState.collectAsState()

    /**
     * Indicates whether a movie or series is currently being added to the local database.
     * Used to display a progress indicator (e.g. spinner) during saving operation.
     */
    val isAdding by viewModel.isAdding.collectAsState()

    /**
     * True if the current content (movie or series) already exists in the local database.
     * Used to disable or change the "Add to Watchlist" button state.
     */
    val isAlreadyAdded by viewModel.isAlreadyAdded.collectAsState()

    /**
     * Contains the set of episode IMDb IDs that have been marked as watched by the user.
     * Used to display checkboxes or icons in the episode list accordingly.
     */
    val watchedEpisodes by viewModel.watchedEpisodes.collectAsState()

    /**
     * True if the current movie has a non-null `watchedDate`, meaning it's been marked as watched.
     * Used to toggle the UI state of the "Mark as Watched" button or indicator.
     */
    val isWatched by viewModel.isWatched.collectAsState()


    /**
     * Responds to a successful content load from the OMDb API (online).
     * If the loaded content is a series, this effect loads:
     * - season 1 episode list (only if not already loaded)
     * - set of watched episodes from the local database
     */
    LaunchedEffect(uiState) {
        if (uiState is ContentDetailUiState.Success &&
            (uiState as ContentDetailUiState.Success).content.type == "series"
        ) {
            val content = (uiState as ContentDetailUiState.Success).content
            Log.d("ContentDetailScreen", "Series loaded via API: ${content.title}")

            // Load season 1 only if no episodes have been loaded yet
            if (episodesUiState !is EpisodesUiState.Success) {
                Log.d("ContentDetailScreen", "Loading default season 1")
                viewModel.loadSeason(content.imdbId, season = 1)
            }

            // Fetch watched episodes from local storage
            viewModel.loadWatchedEpisodes(content.imdbId)
        }
    }

    /**
     * Responds to a successful content load from the local database (Room).
     * Reloads:
     * - full episode list from local database
     * - set of watched episodes for the given series
     */
    LaunchedEffect(uiState) {
        if (uiState is ContentDetailUiState.SeriesLoaded) {
            val series = (uiState as ContentDetailUiState.SeriesLoaded).series
            Log.d("ContentDetailScreen", "Series loaded from Room DB: ${series.title}")

            viewModel.loadEpisodesForSeries(series.imdbId)
            viewModel.loadWatchedEpisodes(series.imdbId)
        }
    }

    /**
     * Render UI based on the current state of the content detail.
     * Each branch handles a different representation: loading, error, or loaded content.
     */
    when (uiState) {

        /**
         * Render UI based on the current state of the content detail.
         * Each branch handles a different representation: loading, error, or loaded content.
         */
        is ContentDetailUiState.Loading -> {
            Log.d("ContentDetailScreen", "State: Loading")
            CircularProgressIndicator()
        }

        /**
         * Display full content detail retrieved from the OMDb API.
         * Supports both movies and series (online). For series, episode selection is enabled.
         */
        is ContentDetailUiState.Success -> {
            val content = (uiState as ContentDetailUiState.Success).content
            Log.d("ContentDetailScreen", "State: Success → ${content.title} [${content.type}]")
            ContentDetail(
                content = content,
                episodesUiState = episodesUiState,
                onSeasonSelected = { season ->
                    viewModel.loadSeason(
                        (uiState as ContentDetailUiState.Success).content.imdbId,
                        season
                    )
                },
                onMarkToWatch = {
                    if (content.type == "movie") {
                        viewModel.saveMovieLocally(content)
                    } else if (content.type == "series") {
                        viewModel.saveSeriesLocally(content)
                    }
                },
                isAlreadyAdded = isAlreadyAdded,
                watchedEpisodes = watchedEpisodes,
                onEpisodeWatchedToggle = { imdbIdt, watched ->
                    viewModel.toggleEpisodeWatched(imdbIdt, watched)
                },
                isWatched = isWatched,
                onMarkWatched = { viewModel.toggleMovieWatched(imdbId) },
                showMarkAsWatched = content.type == "movie"
            )
        }

        /**
         * Display movie detail using locally stored data (Room).
         * This state is used when offline or working with previously saved content.
         */
        is ContentDetailUiState.MovieLoaded -> {
            val movie = (uiState as ContentDetailUiState.MovieLoaded).movie
            Log.d("ContentDetailScreen", "State: MovieLoaded → ${movie.title}")
            ContentDetailFromMovieEntity(
                movie = movie,
                isAlreadyAdded = isAlreadyAdded
            )
        }

        /**
         * Display series detail and episodes from local database (Room).
         * Allows for offline episode browsing and watched tracking.
         */
        is ContentDetailUiState.SeriesLoaded -> {
            val series = (uiState as ContentDetailUiState.SeriesLoaded).series
            Log.d("ContentDetailScreen", "State: SeriesLoaded → ${series.title}")
            val episodes by viewModel.localEpisodes.collectAsState()
            val episodesBySeason = remember(episodes) {
                episodes.groupBy { it.season }
            }
            val selectedSeason = remember { mutableIntStateOf(1) }

            // Transform local episode data into UI-friendly format
            val seasonEpisodes = remember(episodesBySeason, selectedSeason.intValue) {
                episodesBySeason[selectedSeason.intValue]
                    .orEmpty()
                    .sortedBy { it.episode }
                    .map {
                        Episode(
                            title = it.title,
                            released = it.released,
                            episode = it.episode.toString(),
                            imdbId = it.imdbId,
                            plot = it.plot,
                            runtime = it.runtime,
                            season = it.season.toString()
                        )
                    }
            }
            // Map SeriesEntity to ContentDetail model used in UI
            val content = remember(series) {
                ContentDetail(
                    title = series.title,
                    genre = series.genre,
                    type = "series",
                    actors = null,
                    plot = series.plot,
                    poster = series.poster,
                    imdbId = series.imdbId,
                    runtime = series.runtime,
                    totalSeasons = series.totalSeasons.toString(),
                    released = series.released,
                    ratings = emptyList(),
                    response = "True"
                )
            }

            ContentDetail(
                content = content,
                episodesUiState = EpisodesUiState.Success(
                    season = selectedSeason.intValue,
                    episodes = seasonEpisodes
                ),
                onSeasonSelected = { selectedSeason.intValue = it },
                isAlreadyAdded = isAlreadyAdded,
                watchedEpisodes = watchedEpisodes,
                onEpisodeWatchedToggle = { imdbIdt, watched ->
                    viewModel.toggleEpisodeWatched(imdbIdt, watched)
                },
                onMarkToWatch = {}, // Already stored locally
                showMarkAsWatched = false
            )
        }

        /**
         * Display a simple message when content could not be found.
         */
        is ContentDetailUiState.NotFound -> {
            Log.w("ContentDetailScreen", "State: NotFound")
            Text(text = "Content not found")
        }

        /**
         * Display an error message when something went wrong.
         */
        is ContentDetailUiState.Error -> {
            val message = (uiState as ContentDetailUiState.Error).message
            Log.e("ContentDetailScreen", "State: Error → $message")
            Text(text = "Error: $message")
        }
    }

    /**
     * Overlay full-screen progress indicator when adding content to the watchlist.
     */
    if (isAdding) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Log.d("ContentDetailScreen", "Showing progress: adding content")
            CircularProgressIndicator()
        }
    }


}





/**
 * Composable screen for displaying detailed information about a movie or series.
 * Includes poster, metadata, ratings, episode list, and watchlist controls.
 *
 * @param content The main content data (movie or series) to display.
 * @param episodesUiState Current state of episode data (used for series only).
 * @param onSeasonSelected Callback triggered when a different season is selected.
 * @param isAlreadyAdded Whether the content has already been added to the watchlist.
 * @param watchedEpisodes Set of episode IMDb IDs marked as watched.
 * @param onEpisodeWatchedToggle Callback for toggling the watched status of an episode.
 * @param modifier Optional modifier for layout customization.
 * @param onMarkToWatch Callback for adding content to the local watchlist.
 * @param onMarkWatched Callback for marking a movie as watched.
 * @param isWatched Whether the movie has been marked as watched.
 * @param showMarkAsWatched Whether to show the "Mark as Watched" button (movies only).
 */
@Composable
fun ContentDetail(
    content: ContentDetail,
    episodesUiState: EpisodesUiState,
    onSeasonSelected: (Int) -> Unit,
    isAlreadyAdded: Boolean,
    watchedEpisodes: Set<String>,
    onEpisodeWatchedToggle: (imdbId: String, watched: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onMarkToWatch: () -> Unit = {},
    onMarkWatched: () -> Unit = {},
    isWatched: Boolean = false,
    showMarkAsWatched: Boolean = false

) {

    // UI state: toggle for expanded description and ratings
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isRatingsExpanded by remember { mutableStateOf(false) }

    // UI state: currently selected tab (info / episodes)
    var selectedTab by remember { mutableStateOf("info") }





    // --- Main column layout ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .animateContentSize()
    ) {
        // --- Poster image ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(content.poster)
                    .crossfade(true)
                    .build(),
                contentDescription = "${content.title} poster",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(2f / 3f)
                    .padding(4.dp)
            )
        }

        // --- Basic content info: title, genre, runtime, etc. ---
        ContentInformations(content)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Watchlist control buttons ---
        DetailsButtons(
            onMarkToWatch = onMarkToWatch,
            onMarkWatched = onMarkWatched,
            isAlreadyAdded = isAlreadyAdded,
            showMarkAsWatched = showMarkAsWatched,
            isWatched = isWatched
        )
        Spacer(modifier = Modifier.height(8.dp))


        // --- Series-only: tab switcher (Info / Episodes) ---
        if (content.type == "series") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ContentTabs(
                    isSeries = true,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        // --- Main content area depending on type and selected tab ---
        Column(modifier = Modifier.fillMaxWidth()) {
            when {
                // Movie, or Info tab in series
                content.type == "series" && selectedTab == "info" ||
                        content.type == "movie" -> {
                    ContentInformationsBlock(
                        content = content,
                        isDescriptionExpanded = isDescriptionExpanded,
                        isRatingsExpanded = isRatingsExpanded,
                        onExpandDesc = { isDescriptionExpanded = !isDescriptionExpanded },
                        onExpandRatings = { isRatingsExpanded = !isRatingsExpanded }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Episodes tab in series
                content.type == "series" && selectedTab == "episodes" -> {
                    EpisodesSection(
                        totalSeasons = content.totalSeasons?.toIntOrNull() ?: 1,
                        selectedSeason = when (episodesUiState) {
                            is EpisodesUiState.Success -> episodesUiState.season
                            else -> 1
                        },
                        episodesUiState = episodesUiState,
                        onSeasonSelected = onSeasonSelected,
                        watchedEpisodes = watchedEpisodes,
                        onEpisodeWatchedToggle = onEpisodeWatchedToggle
                    )
                }
            }
        }
    }
}





/**
 * Composable that displays a section for selecting a season and listing episodes of a series.
 * Includes a season dropdown, a loading/error handler, and a list of episode items with watch toggles.
 *
 * @param totalSeasons Total number of seasons available in the series.
 * @param selectedSeason Currently selected season (used to highlight in dropdown).
 * @param episodesUiState Current UI state for the episodes (loading, error, or loaded list).
 * @param onSeasonSelected Callback triggered when user selects a different season.
 * @param onEpisodeWatchedToggle Callback triggered when a user marks/unmarks an episode as watched.
 * @param watchedEpisodes Set of IMDb IDs of episodes that are marked as watched.
 */
@Composable
fun EpisodesSection(
    totalSeasons: Int,
    selectedSeason: Int,
    episodesUiState: EpisodesUiState,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeWatchedToggle: (imdbId: String, watched: Boolean) -> Unit = { _, _ -> },
    watchedEpisodes: Set<String> = emptySet()
) {
    // State for controlling the visibility of the season dropdown menu
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // --- Label above season selector ---
        Text(
            text = "Select Season",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // --- Season dropdown menu ---
        Box {
            Button(onClick = { isDropdownExpanded = true }) {
                Text("Season $selectedSeason")
            }

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                for (season in 1..totalSeasons) {
                    DropdownMenuItem(
                        text = { Text("Season $season") },
                        onClick = {
                            isDropdownExpanded = false
                            onSeasonSelected(season)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Main episode content area ---
        when (episodesUiState) {
            // Show loading spinner while episodes are being fetched
            is EpisodesUiState.Loading -> CircularProgressIndicator()

            // Show error message if episode fetch failed
            is EpisodesUiState.Error -> Text(
                text = "Error: ${episodesUiState.message}",
                color = MaterialTheme.colorScheme.error
            )
            // Show list of episodes when successfully loaded
            is EpisodesUiState.Success -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    episodesUiState.episodes.forEach { episode ->
                        EpisodeItem(
                            episodeTitle = episode.title.orEmpty(),
                            episodeNumber = episode.episode.orEmpty(),
                            watched = watchedEpisodes.contains(episode.imdbId),
                            onWatchedToggle = { checked ->
                                episode.imdbId?.let { imdbId ->
                                    onEpisodeWatchedToggle(imdbId, checked)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}




/**
 * Displays a single episode row with its number, title, and a checkbox for "watched" status.
 *
 * @param episodeTitle Title of the episode (e.g., "The Arrival").
 * @param episodeNumber Episode number within the season (e.g., "1").
 * @param watched Whether the episode is marked as watched.
 * @param onWatchedToggle Callback triggered when user toggles watched status.
 */
@Composable
fun EpisodeItem(
    episodeTitle: String,
    episodeNumber: String,
    watched: Boolean,
    onWatchedToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Episode number and title
        Text(
            text = "E$episodeNumber: $episodeTitle",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )



        // Right: Circular checkbox indicating watched status
        RoundedCheckbox(
            checked = watched,
            onCheckedChange = onWatchedToggle
        )
    }
}




/**
 * A circular checkbox styled with a filled background and a check icon.
 * Changes color depending on the checked state and triggers a callback on click.
 *
 * @param checked Whether the checkbox is currently checked.
 * @param onCheckedChange Callback triggered when the checkbox is clicked.
 */
@Composable
fun RoundedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Determine background and icon color based on state
    val backgroundColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = if (checked) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }



    // Wrapper that handles the click
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        // Circular card with background
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.size(28.dp)
        ) {
            // Show check icon only if checked
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Watched",
                    tint = iconTint,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(18.dp)
                )
            }
        }
    }
}




/**
 * Displays key metadata for the given content (movie or series).
 * Shows release date, runtime, and genre, each with an icon and label.
 *
 * @param content The content object containing the metadata to display.
 */
@Composable
fun ContentInformations(
    content: ContentDetail,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // --- Release date ---
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Release Date",
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = content.released ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }



        // --- Runtime ---
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Runtime",
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = content.runtime ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }



        // --- Genre ---
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = "Genre",
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = content.genre ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}




/**
 * Composable row of action buttons used in the content detail screen.
 * Displays:
 * - "Add to Watchlist" button (disabled if already added),
 * - "Mark as Watched" button (optional, shown only for movies).
 *
 * @param onMarkToWatch Called when the user taps the "Add to Watchlist" button.
 * @param onMarkWatched Called when the user taps the "Mark as Watched" button.
 * @param isAlreadyAdded Whether the content is already in the watchlist (disables the first button).
 * @param showMarkAsWatched Whether to show the "Mark as Watched" button (movies only).
 * @param isWatched Whether the content has already been marked as watched.
 */
@Composable
fun DetailsButtons(
    onMarkToWatch: () -> Unit = {},
    onMarkWatched: () -> Unit = {},
    isAlreadyAdded: Boolean,
    showMarkAsWatched: Boolean = false,
    isWatched: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Button: Add to Watchlist ---
        Button(
            onClick = onMarkToWatch,
            enabled = !isAlreadyAdded,
            modifier = Modifier.weight(1f)

        ) {
            Text("Add to Watchlist")
        }


        // --- Button: Mark/Unmark as Watched (only for movies) ---
        if (showMarkAsWatched) {
            Button(
                onClick = onMarkWatched,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWatched) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                    contentColor = if (isWatched) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (isWatched) "Unmark Watched" else "Mark as Watched")
            }
        }
    }
}


/**
 * Displays the ratings section for the content (movie or series).
 * Always shows the first rating (e.g., IMDb), and allows the user to expand to see all ratings.
 *
 * @param content The content whose ratings will be displayed.
 * @param expanded Whether the ratings list is expanded to show all entries.
 * @param onExpandToggle Callback to toggle the expanded state.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentRatings(
    content: ContentDetail,
    expanded: Boolean,
    onExpandToggle: () -> Unit
) {
    // Extract and split ratings
    val ratings = content.ratings.orEmpty()
    val firstRating = ratings.firstOrNull()
    val remainingRatings = ratings.drop(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize()
    ) {
        // --- Section title ---
        Text(
            text = "Ratings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )



        // --- Always show the first rating (e.g. IMDb) ---
        firstRating?.let {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingItem(rating = it)
            }
        }



        // --- Conditionally show the rest of the ratings (with animation) ---
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                remainingRatings.forEach { rating ->
                    RatingItem(rating = rating)
                }
            }
        }



        // --- Toggle: Show more / Show less ---
        if (ratings.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Show less" else "Show more"
                )
            }
        }
    }
}

/**
 * Displays a single rating item inside a styled card.
 * Shows the source of the rating (e.g., IMDb, Rotten Tomatoes) and its value.
 *
 * @param rating The rating object containing source and value.
 * @param modifier Optional modifier for external styling.
 */
@Composable
private fun RatingItem(
    rating: Rating,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .padding(4.dp)
            .wrapContentWidth() // Width adapts to content, not forced full width
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // --- Rating source (e.g., IMDb, Metacritic) ---
            Text(
                text = rating.source,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Rating value (e.g., 8.4/10) ---
            Text(
                text = rating.value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



/**
 * Composable displaying a text description (plot) of the content.
 * Initially truncated to 3 lines with option to expand/collapse.
 *
 * @param plot The plot or description of the movie/series.
 * @param expanded Whether the full text should be shown.
 * @param onExpandToggle Callback triggered when the expand/collapse icon is clicked.
 */
@Composable
fun ContentDescription(
    plot: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize() // Smooth height changes when expanding
    ) {
        // --- Section title ---
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- Plot text: limited lines unless expanded ---
        AnimatedVisibility(
            visible = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = plot,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // --- Optional spacing when expanded (for smoother layout) ---
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
        }


        // --- Expand/Collapse icon ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Show less" else "Show more"
            )
        }
    }
}


/**
 * Composable displaying a list of actors from the given string.
 * If the input is null or blank, nothing is rendered.
 *
 * @param actors A comma-separated list of actor names (e.g., "Tom Hanks, Meg Ryan").
 */
@Composable
fun ContentActors(
    actors: String?
) {
    // Do not render anything if no actor data is available
    if (actors.isNullOrBlank()) return


    // Convert comma-separated string to list of trimmed names
    val actorList = actors.split(",").map { it.trim() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize() // Smooth height change if dynamically expanded/collapsed
    ) {
        // --- Section title ---
        Text(
            text = "Actors",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        // --- List of actor names ---
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            actorList.forEach { actor ->
                Text(
                    text = actor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}




/**
 * Composable block that aggregates the main informational sections for content:
 * ratings, description (plot), and list of actors. Handles expansion state for
 * both ratings and description independently.
 *
 * @param content The content object containing metadata to display.
 * @param isDescriptionExpanded Whether the description is fully expanded.
 * @param isRatingsExpanded Whether the ratings section is fully expanded.
 * @param onExpandDesc Callback triggered when the description expand/collapse icon is tapped.
 * @param onExpandRatings Callback triggered when the ratings expand/collapse icon is tapped.
 */
@Composable
private fun ContentInformationsBlock(
    content: ContentDetail,
    isDescriptionExpanded: Boolean,
    isRatingsExpanded: Boolean,
    onExpandDesc: () -> Unit,
    onExpandRatings: () -> Unit
) {
    // --- Optional section: Ratings ---
    if (!content.ratings.isNullOrEmpty()) {
        ContentRatings(
            content = content,
            expanded = isRatingsExpanded,
            onExpandToggle = onExpandRatings
        )
    }

    // --- Always visible: Description block ---
    ContentDescription(
        plot = content.plot ?: "No description available",
        expanded = isDescriptionExpanded,
        onExpandToggle = onExpandDesc
    )

    // --- Optional section: Actor list ---
    ContentActors(
        actors = content.actors
    )
}



/**
 * Composable rendering a tab row with selectable tabs for content details.
 * Used to switch between "Informations" and "Episodes" (if the content is a series).
 *
 * @param isSeries Whether the content is a series; if true, shows the "Episodes" tab.
 * @param selectedTab Currently selected tab key (e.g., "info" or "episodes").
 * @param onTabSelected Callback triggered when a tab is clicked, passing its key.
 * @param modifier Modifier for layout customization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTabs(
    isSeries: Boolean,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Define available tabs as a list of key-label pairs
    val tabs = buildList {
        add("info" to "Informations")
        if (isSeries) add("episodes" to "Episodes")
    }

    // Find index of currently selected tab
    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)


    // Material3 tab row with primary styling
    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
    ) {
        tabs.forEachIndexed { index, (key, label) ->
            // Determine icon for each tab
            val icon = when (key) {
                "info" -> Icons.Default.Info
                "episodes" -> Icons.AutoMirrored.Filled.List
                else -> null
            }


            // Render individual tab
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(key) },
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
                        Text(label)
                    }
                }
            )
        }
    }
}




/**
 * Composable used to render the detail view for a locally stored movie (from Room database).
 * Converts a [MoviesEntity] into a [ContentDetail] and delegates rendering to [ContentDetail].
 *
 * @param movie The locally stored movie entity.
 * @param isAlreadyAdded Whether the movie is already in the watchlist (defaults to true).
 * @param onMarkToWatch Optional callback for adding to the watchlist (rarely needed for local content).
 * @param viewModel ViewModel instance used to manage watch status.
 */
@Composable
fun ContentDetailFromMovieEntity(
    movie: MoviesEntity,
    isAlreadyAdded: Boolean = true,
    onMarkToWatch: () -> Unit = {},
    viewModel: ContentDetailViewModel = viewModel(factory = ContentDetailModelProvider.Factory)
) {
    // Observe current watched status from the ViewModel
    val isWatched by viewModel.isWatched.collectAsState()

    // Check watch status when movie is loaded
    LaunchedEffect(movie.imdbId) {
        viewModel.checkIfMovieWatched(movie.imdbId)
    }

    // Convert MoviesEntity to ContentDetail to reuse unified ContentDetail UI
    val converted = remember(movie) {
        ContentDetail(
            title = movie.title,
            released = movie.released,
            runtime = movie.runtime,
            genre = movie.genre,
            actors = null, // Actor data not stored locally
            plot = movie.plot,
            poster = movie.poster,
            ratings = emptyList(), // No ratings available in local DB
            imdbId = movie.imdbId,
            type = "movie",
            response = "True",
            totalSeasons = null,
        )
    }


    // Delegate rendering to main ContentDetail component
    ContentDetail(
        content = converted,
        episodesUiState = EpisodesUiState.Loading, // Not relevant for movies
        onSeasonSelected = {}, // Not applicable
        isAlreadyAdded = isAlreadyAdded,
        watchedEpisodes = emptySet(), // Not applicable
        onEpisodeWatchedToggle = { _, _ -> }, // Not applicable
        onMarkToWatch = onMarkToWatch,
        onMarkWatched = { viewModel.toggleMovieWatched(movie.imdbId) },
        isWatched = isWatched,
        showMarkAsWatched = true
    )
}

