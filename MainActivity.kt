package com.movie.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movie.movieapp.ui.MovieAppTheme
import com.movie.movieapp.viewmodel.MovieViewModel
import com.movie.movieapp.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            
            MovieAppTheme(isDarkMode = isDarkMode) {
                val viewModel: MovieViewModel = viewModel()
                MainScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onDarkModeChanged = { isDarkMode = it }
                )
            }
        }
    }
}
