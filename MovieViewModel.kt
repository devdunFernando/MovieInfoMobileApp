package com.movie.movieapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.movie.movieapp.data.Movie
import com.movie.movieapp.data.MovieDatabase
import com.movie.movieapp.network.OmdbApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = MovieDatabase.getDatabase(app).movieDao()

    private val _currentMovie = MutableStateFlow<Movie?>(null)
    val currentMovie = _currentMovie.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _savedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val savedMovies = _savedMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadSavedMovies()
    }

    private fun loadSavedMovies() {
        viewModelScope.launch {
            _savedMovies.value = dao.getAllMovies()
        }
    }

    fun addHardcodedMovies() {
        viewModelScope.launch {
            val movies = listOf(
                Movie("tt0111161", "The Shawshank Redemption", "1994", "R", "14 Oct 1994", "142 min", "Drama", "Frank Darabont", "Stephen King, Frank Darabont", "Tim Robbins, Morgan Freeman, Bob Gunton", "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency."),
                Movie("tt2313197", "Batman: The Dark Knight Returns, Part 1", "2012", "PG-13", "25 Sep 2012", "76 min", "Animation, Action, Crime, Drama, Thriller", "Jay Oliva", "Bob Kane (character created by: Batman), Frank Miller (comic book), Klaus Janson (comic book), Bob Goodman", "Peter Weller, Ariel Winter, David Selby, Wade Williams", "Batman has not been seen for ten years. A new breed of criminal ravages Gotham City, forcing 55-year-old Bruce Wayne back into the cape and cowl. But, does he still have what it takes to fight crime in a new era?"),
                Movie("tt0167260", "The Lord of the Rings: The Return of the King", "2003", "PG-13", "17 Dec 2003", "201 min", "Action, Adventure, Drama", "Peter Jackson", "J.R.R. Tolkien, Fran Walsh, Philippa Boyens", "Elijah Wood, Viggo Mortensen, Ian McKellen", "Gandalf and Aragorn lead the World of Men against Sauron's army to draw his gaze from Frodo and Sam as they approach Mount Doom with the One Ring."),
                Movie("tt1375666", "Inception", "2010", "PG-13", "16 Jul 2010", "148 min", "Action, Adventure, Sci-Fi", "Christopher Nolan", "Christopher Nolan", "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page", "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster."),
                Movie("tt0133093", "The Matrix", "1999", "R", "31 Mar 1999", "136 min", "Action, Sci-Fi", "Lana Wachowski, Lilly Wachowski", "Lilly Wachowski, Lana Wachowski", "Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss", "When a beautiful stranger leads computer hacker Neo to a forbidding underworld, he discovers the shocking truth--the life he knows is the elaborate deception of an evil cyber-intelligence.")
            )
            movies.forEach { dao.insert(it) }
            loadSavedMovies()
        }
    }

    fun searchMoviesByActor(actor: String, onResult: (List<Movie>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val localResults = dao.searchMoviesByActor(actor)
                val omdbResults = OmdbApiService.searchMoviesByActor(actor)
                
                // Convert OMDB results to Movie objects and combine with local results
                val allResults = localResults + omdbResults.map { omdbMovie ->
                    Movie(
                        imdbID = omdbMovie.imdbID,
                        title = omdbMovie.title,
                        year = omdbMovie.year,
                        rated = omdbMovie.rated,
                        released = omdbMovie.released,
                        runtime = omdbMovie.runtime,
                        genre = omdbMovie.genre,
                        director = omdbMovie.director,
                        writer = omdbMovie.writer,
                        actors = omdbMovie.actors,
                        plot = omdbMovie.plot
                    )
                }
                
                // Remove duplicates based on imdbID
                val uniqueResults = allResults.distinctBy { it.imdbID }
                onResult(uniqueResults)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMoviesByTitle(title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val movies = OmdbApiService.searchMoviesByTitle(title)
                _searchResults.value = movies.map { omdbMovie ->
                    Movie(
                        imdbID = omdbMovie.imdbID,
                        title = omdbMovie.title,
                        year = omdbMovie.year,
                        rated = omdbMovie.rated,
                        released = omdbMovie.released,
                        runtime = omdbMovie.runtime,
                        genre = omdbMovie.genre,
                        director = omdbMovie.director,
                        writer = omdbMovie.writer,
                        actors = omdbMovie.actors,
                        plot = omdbMovie.plot
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCurrentMovie() {
        viewModelScope.launch {
            currentMovie.value?.let { 
                dao.insert(it)
                loadSavedMovies()
            }
        }
    }

    fun deleteSavedMovie(imdbID: String) {
        viewModelScope.launch {
            dao.deleteByImdbID(imdbID)
            loadSavedMovies()
        }
    }

    fun setCurrentMovie(movie: Movie) {
        _currentMovie.value = movie
    }
}
