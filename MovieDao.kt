package com.movie.movieapp.data

import androidx.room.*

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie)

    @Query("SELECT * FROM movies WHERE actors LIKE '%' || :actor || '%' COLLATE NOCASE")
    suspend fun searchMoviesByActor(actor: String): List<Movie>

    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<Movie>

    @Query("DELETE FROM movies WHERE imdbID = :imdbID")
    suspend fun deleteByImdbID(imdbID: String)
}
