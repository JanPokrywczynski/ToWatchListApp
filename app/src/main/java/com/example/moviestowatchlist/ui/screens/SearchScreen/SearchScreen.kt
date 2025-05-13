package com.example.moviestowatchlist.ui.screens.SearchScreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.moviestowatchlist.R
import com.example.moviestowatchlist.data.remote.SearchResult
import java.util.Locale

/**
 * Composable screen that provides search functionality for movies or series.
 * Allows users to enter a title, select type (movie/series), and displays matching results.
 *
 * @param viewModel ViewModel responsible for handling the search logic and UI state.
 * @param onItemClick Callback invoked when a result item is clicked. Provides IMDb ID and title.
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onItemClick: (imdbId: String, title: String) -> Unit
) {
    // Observe UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Mutable state for query input and selected type (movie/series)
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var selectedType by remember { mutableStateOf("movie") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Search input and action button
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.search_title)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.search(query.text, selectedType)
            }) {
                Text(stringResource(R.string.search_button))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        // Type filter chips: Movie / Series
        Row {
            FilterChip(
                selected = selectedType == "movie",
                onClick = { selectedType = "movie" },
                label = { Text(stringResource(R.string.movies)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = selectedType == "series",
                onClick = { selectedType = "series" },
                label = { Text(stringResource(R.string.series)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))



        // Display different UI based on current state
        when (uiState) {
            is SearchUiState.Idle -> Text(stringResource(R.string.enter_query_to_search))
            is SearchUiState.Loading -> CircularProgressIndicator()
            is SearchUiState.Error -> {
                val message = (uiState as SearchUiState.Error).message
                Log.e("SearchScreen", "Error during search: $message")
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            is SearchUiState.Success -> {
                val results = (uiState as SearchUiState.Success).results
                if (results.isEmpty()) {
                    Text(stringResource(R.string.no_results_found))
                    Log.d("SearchScreen", "Search returned 0 results.")

                } else {
                    Log.d("SearchScreen", "Search returned ${results.size} results.")
                    LazyColumn {
                        items(results) { item ->
                            SearchResultItem(item, onClick = { onItemClick(item.imdbId, item.title) })
                        }
                    }
                }
            }
        }
    }
}





/**
 * Composable representing a single search result item.
 * Displays poster, title, year, and type of the result.
 *
 * @param item The SearchResult data to display.
 * @param onClick Action triggered when the item is clicked.
 */
@Composable
fun SearchResultItem(
    item: SearchResult,
    onClick: () -> Unit
) {

    // Display a clickable card with poster and basic info
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Poster image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.poster)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(2f / 3f)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Title, year, and type
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(item.year, style = MaterialTheme.typography.bodyMedium)
                Text(item.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
