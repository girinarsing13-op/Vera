package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seen_movies")
data class SeenMovieEntity(
    @PrimaryKey val id: String,
    val tmdbId: Int,
    val title: String,
    val type: String,
    val year: Int,
    val duration: String,
    val rating: Double,
    val genresCsv: String,
    val poster: String,
    val synopsis: String,
    val timestamp: Long = System.currentTimeMillis()
)
