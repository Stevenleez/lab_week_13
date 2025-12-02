package com.example.lab_week_13

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab_week_13.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar

class MovieViewModel(private val movieRepository: MovieRepository)
    : ViewModel() {
    private val _popularMovies = MutableStateFlow(emptyList<Movie>())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    init {
        fetchPopularMovies()
    }

    // fetch movies from the API
    private fun fetchPopularMovies() {
        // launch a coroutine in viewModelScope
        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.fetchMovies()
                .catch { throwable ->
                    // catch is a terminal operator that catches exceptions from the Flow
                    _error.value = "An exception occurred: ${throwable.message}"
                }
                .collect { moviesFromApi ->

                    val currentYear =
                        Calendar.getInstance().get(Calendar.YEAR).toString()

                    val filteredAndSorted = moviesFromApi
                        .filter { movie ->
                            // aman dari null
                            movie.releaseDate?.startsWith(currentYear) == true
                        }
                        .sortedByDescending { movie ->
                            movie.popularity
                        }

                    _popularMovies.value = filteredAndSorted
                }
        }
    }
}
