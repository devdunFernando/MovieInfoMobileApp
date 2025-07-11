package com.movie.movieapp.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object OmdbApiService {
    private const val API_KEY = "88d5d36"
    private const val RESULTS_PER_PAGE = 10

    data class OmdbMovie(
        val imdbID: String,
        val title: String,
        val year: String,
        val rated: String,
        val released: String,
        val runtime: String,
        val genre: String,
        val director: String,
        val writer: String,
        val actors: String,
        val plot: String
    )

    suspend fun getMovie(title: String): OmdbMovie? {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "https://www.omdbapi.com/?t=$title&apikey=$API_KEY"
                val json = URL(urlString).readText()
                val jsonObj = JSONObject(json)

                if (jsonObj.getString("Response") == "True") {
                    OmdbMovie(
                        imdbID = jsonObj.getString("imdbID"),
                        title = jsonObj.getString("Title"),
                        year = jsonObj.getString("Year"),
                        rated = jsonObj.getString("Rated"),
                        released = jsonObj.getString("Released"),
                        runtime = jsonObj.getString("Runtime"),
                        genre = jsonObj.getString("Genre"),
                        director = jsonObj.getString("Director"),
                        writer = jsonObj.getString("Writer"),
                        actors = jsonObj.getString("Actors"),
                        plot = jsonObj.getString("Plot")
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun searchMoviesByActor(actor: String): List<OmdbMovie> {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "https://www.omdbapi.com/?s=$actor&apikey=$API_KEY"
                val json = URL(urlString).readText()
                val jsonObj = JSONObject(json)

                if (jsonObj.getString("Response") == "True") {
                    val searchArray = jsonObj.getJSONArray("Search")
                    val movies = mutableListOf<OmdbMovie>()
                    
                    for (i in 0 until searchArray.length()) {
                        val movieObj = searchArray.getJSONObject(i)
                        val imdbID = movieObj.getString("imdbID")
                        
                        // Get full movie details
                        val movieUrlString = "https://www.omdbapi.com/?i=$imdbID&apikey=$API_KEY"
                        val movieJson = URL(movieUrlString).readText()
                        val movieJsonObj = JSONObject(movieJson)
                        
                        if (movieJsonObj.getString("Response") == "True") {
                            val actors = movieJsonObj.getString("Actors")
                            // Only include movies where the actor is in the cast
                            if (actors.contains(actor, ignoreCase = true)) {
                                movies.add(
                                    OmdbMovie(
                                        imdbID = movieJsonObj.getString("imdbID"),
                                        title = movieJsonObj.getString("Title"),
                                        year = movieJsonObj.getString("Year"),
                                        rated = movieJsonObj.getString("Rated"),
                                        released = movieJsonObj.getString("Released"),
                                        runtime = movieJsonObj.getString("Runtime"),
                                        genre = movieJsonObj.getString("Genre"),
                                        director = movieJsonObj.getString("Director"),
                                        writer = movieJsonObj.getString("Writer"),
                                        actors = actors,
                                        plot = movieJsonObj.getString("Plot")
                                    )
                                )
                            }
                        }
                    }
                    movies
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun searchMoviesByTitle(title: String): List<OmdbMovie> {
        return withContext(Dispatchers.IO) {
            try {
                val movies = mutableListOf<OmdbMovie>()
                var page = 1
                var hasMoreResults = true

                while (hasMoreResults && movies.size < RESULTS_PER_PAGE) {
                    val urlString = "https://www.omdbapi.com/?s=$title&page=$page&apikey=$API_KEY"
                    val json = URL(urlString).readText()
                    val jsonObj = JSONObject(json)

                    if (jsonObj.getString("Response") == "True") {
                        val searchArray = jsonObj.getJSONArray("Search")
                        
                        for (i in 0 until searchArray.length()) {
                            if (movies.size >= RESULTS_PER_PAGE) break
                            
                            val movieObj = searchArray.getJSONObject(i)
                            val movieTitle = movieObj.getString("Title")
                            
                            // Only include movies where the title contains the search query (case-insensitive)
                            if (movieTitle.contains(title, ignoreCase = true)) {
                                val imdbID = movieObj.getString("imdbID")
                                
                                // Get full movie details
                                val movieUrlString = "https://www.omdbapi.com/?i=$imdbID&apikey=$API_KEY"
                                val movieJson = URL(movieUrlString).readText()
                                val movieJsonObj = JSONObject(movieJson)
                                
                                if (movieJsonObj.getString("Response") == "True") {
                                    movies.add(
                                        OmdbMovie(
                                            imdbID = movieJsonObj.getString("imdbID"),
                                            title = movieJsonObj.getString("Title"),
                                            year = movieJsonObj.getString("Year"),
                                            rated = movieJsonObj.getString("Rated"),
                                            released = movieJsonObj.getString("Released"),
                                            runtime = movieJsonObj.getString("Runtime"),
                                            genre = movieJsonObj.getString("Genre"),
                                            director = movieJsonObj.getString("Director"),
                                            writer = movieJsonObj.getString("Writer"),
                                            actors = movieJsonObj.getString("Actors"),
                                            plot = movieJsonObj.getString("Plot")
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Check if there are more results
                        val totalResults = jsonObj.getString("totalResults").toInt()
                        hasMoreResults = page * 10 < totalResults
                        page++
                    } else {
                        hasMoreResults = false
                    }
                }
                
                movies
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
