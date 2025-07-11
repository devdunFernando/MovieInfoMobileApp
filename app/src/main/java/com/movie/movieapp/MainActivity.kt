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
import com.movie.movieapp.ui.MainScreen
import com.movie.movieapp.logiccontroller.MovieController
import com.movie.movieapp.logiccontroller.MovieControllerFactory
import com.movie.movieapp.data.MovieDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            MovieAppTheme(isDarkMode = isDarkMode) {
                val dao = MovieDatabase.getDatabase(applicationContext).movieDao()
                val controller: MovieController = viewModel(
                    factory = MovieControllerFactory(dao)
                )

                MainScreen(
                    viewModel = controller,
                    isDarkMode = isDarkMode,
                    onDarkModeChanged = { isDarkMode = it }
                )
            }
        }
    }
}
