package com.example.data.repository

import com.example.data.db.MovieDao
import com.example.data.db.SavedMovieEntity
import com.example.data.db.SeenMovieEntity
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MovieRepository(private val movieDao: MovieDao) {

    val watchlist: Flow<List<MediaItem>> = movieDao.getWatchlist().map { entities ->
        entities.map { it.toMediaItem() }
    }

    val history: Flow<List<MediaItem>> = movieDao.getHistory().map { entities ->
        entities.map { it.toMediaItem() }
    }

    val seenList: Flow<List<MediaItem>> = movieDao.getAllSeen().map { entities ->
        entities.map { it.toMediaItem() }
    }

    suspend fun getExcludedIds(): Set<String> {
        val excludedFromSaved = movieDao.getExcludedIds().toSet()
        val seenFromTable = movieDao.getSeenIds().toSet()
        return excludedFromSaved + seenFromTable
    }

    suspend fun getSeenIds(): Set<String> {
        val seenFromHistory = movieDao.getExcludedIds().toSet()
        val seenFromTable = movieDao.getSeenIds().toSet()
        return seenFromHistory + seenFromTable
    }

    suspend fun isSeen(id: String): Boolean {
        val entity = movieDao.getById(id)
        if (entity?.status == "seen") return true
        return movieDao.isSeen(id) > 0
    }

    suspend fun isNotInterested(id: String): Boolean {
        val entity = movieDao.getById(id)
        return entity?.status == "not_interested"
    }

    suspend fun isSaved(id: String): Boolean {
        val entity = movieDao.getById(id)
        return entity?.isWatchlist == true || entity?.status == "saved"
    }

    suspend fun markAsSeen(item: MediaItem) {
        val existing = movieDao.getById(item.id)
        val entity = item.toEntity(
            isWatchlist = false,
            isHistory = true,
            status = "seen",
            unwatched = false
        )
        movieDao.upsert(entity)
        movieDao.insertSeen(
            SeenMovieEntity(
                id = item.id,
                tmdbId = item.tmdbId,
                title = item.title,
                type = item.type,
                year = item.year,
                duration = item.duration,
                rating = item.rating,
                genresCsv = item.genres.joinToString(","),
                poster = item.poster,
                synopsis = item.synopsis,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun markAsNotInterested(item: MediaItem) {
        val existing = movieDao.getById(item.id)
        val entity = item.toEntity(
            isWatchlist = false,
            isHistory = true,
            status = "not_interested",
            unwatched = false
        )
        movieDao.upsert(entity)
        movieDao.removeFromSeen(item.id)
    }

    suspend fun addToWatchlist(item: MediaItem) {
        val existing = movieDao.getById(item.id)
        val entity = item.toEntity(
            isWatchlist = true,
            isHistory = true,
            status = "saved",
            unwatched = true
        )
        movieDao.upsert(entity)
        movieDao.removeFromSeen(item.id)
    }

    suspend fun removeFromWatchlist(id: String) {
        val existing = movieDao.getById(id)
        if (existing != null) {
            val newStatus = if (existing.status == "saved") "discovered" else existing.status
            movieDao.upsert(existing.copy(isWatchlist = false, status = newStatus))
        } else {
            movieDao.removeFromWatchlist(id)
        }
        movieDao.cleanOrphaned()
    }

    suspend fun removeFromSeen(id: String) {
        val existing = movieDao.getById(id)
        if (existing != null && existing.status == "seen") {
            movieDao.upsert(existing.copy(status = "discovered"))
        }
        movieDao.removeFromSeen(id)
    }

    suspend fun removeFromNotInterested(id: String) {
        val existing = movieDao.getById(id)
        if (existing != null && existing.status == "not_interested") {
            movieDao.upsert(existing.copy(status = "discovered"))
        }
    }

    suspend fun recordDiscovery(item: MediaItem) {
        val existing = movieDao.getById(item.id)
        val entity = item.toEntity(
            isWatchlist = existing?.isWatchlist ?: false,
            isHistory = true,
            status = existing?.status ?: "discovered",
            unwatched = existing?.unwatched ?: false
        )
        movieDao.upsert(entity)
    }

    suspend fun clearHistory() {
        movieDao.clearHistory()
        movieDao.cleanOrphaned()
    }

    private fun SeenMovieEntity.toMediaItem(): MediaItem {
        return MediaItem(
            id = id,
            tmdbId = tmdbId,
            title = title,
            type = type,
            year = year,
            duration = duration,
            rating = rating,
            popularity = 50.0,
            trending = false,
            originalLanguage = "en",
            country = null,
            genres = genresCsv.split(",").filter { it.isNotBlank() },
            platforms = emptyList(),
            synopsis = synopsis,
            poster = poster,
            director = null,
            cast = emptyList(),
            numberOfSeasons = 1,
            numberOfEpisodes = 1,
            isSavedToWatchlist = false,
            status = "seen",
            timestamp = timestamp
        )
    }

    private fun SavedMovieEntity.toMediaItem(): MediaItem {
        return MediaItem(
            id = id,
            tmdbId = tmdbId,
            title = title,
            type = type,
            year = year,
            duration = duration,
            rating = rating,
            popularity = popularity,
            trending = trending,
            originalLanguage = originalLanguage,
            country = country,
            genres = genresCsv.split(",").filter { it.isNotBlank() },
            platforms = platformsCsv.split(",").filter { it.isNotBlank() },
            synopsis = synopsis,
            poster = poster,
            director = director,
            cast = castCsv.split(",").filter { it.isNotBlank() },
            numberOfSeasons = numberOfSeasons,
            numberOfEpisodes = numberOfEpisodes,
            isSavedToWatchlist = isWatchlist || status == "saved",
            status = status,
            timestamp = timestamp
        )
    }

    private fun MediaItem.toEntity(
        isWatchlist: Boolean,
        isHistory: Boolean,
        status: String,
        unwatched: Boolean
    ): SavedMovieEntity {
        return SavedMovieEntity(
            id = id,
            tmdbId = tmdbId,
            title = title,
            type = type,
            year = year,
            duration = duration,
            rating = rating,
            popularity = popularity,
            trending = trending,
            originalLanguage = originalLanguage,
            country = country,
            genresCsv = genres.joinToString(","),
            platformsCsv = platforms.joinToString(","),
            synopsis = synopsis,
            poster = poster,
            director = director,
            castCsv = cast.joinToString(","),
            numberOfSeasons = numberOfSeasons,
            numberOfEpisodes = numberOfEpisodes,
            isWatchlist = isWatchlist,
            isHistory = isHistory,
            status = status,
            unwatched = unwatched,
            timestamp = System.currentTimeMillis()
        )
    }
}
