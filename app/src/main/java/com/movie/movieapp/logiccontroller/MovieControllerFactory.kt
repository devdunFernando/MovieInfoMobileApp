package com.movie.movieapp.logiccontroller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movie.movieapp.data.MovieDao

class MovieControllerFactory(private val dao: MovieDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieController::class.java)) {
            return MovieController(dao) as T
        }
        throw IllegalArgumentException("Unknown Controller class")
    }
}
