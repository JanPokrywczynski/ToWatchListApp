package com.example.moviestowatchlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.moviestowatchlist.ui.theme.MoviesToWatchListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoviesToWatchListTheme {
                ToWatchApp()
            }
        }
    }
}
//tag:SeriesRepository tag:EpisodesRepository tag:MoviesRepository tag:ToWatchApp tag:ContentDetailScreen tag:SavedContentScreen tag:ContentDetailCache tag:ContentDetailViewModel tag:ContentDetailVM tag:IntentAction tag:SearchScreen tag:ENRICH_EPISODES