package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_movies")
data class SavedMovieEntity(
    @PrimaryKey val id: String,
    val tmdbId: Int,
    val title: String,
    val type: String,
    val year: Int,
    val duration: String,
    val rating: Double,
    val popularity: Double,
    val trending: Boolean,
    val originalLanguage: String,
    val country: String?,
    val genresCsv: String,
    val platformsCsv: String,
    val synopsis: String,
    val poster: String,
    val director: String?,
    val castCsv: String,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    val isWatchlist: Boolean,
    val isHistory: Boolean,
    val unwatched: Boolean,
    val status: String = "discovered", // "seen", "not_interested", "saved", "discovered"
    val timestamp: Long = System.currentTimeMillis()
)
