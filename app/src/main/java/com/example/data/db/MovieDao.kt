package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM saved_movies WHERE isWatchlist = 1 OR status = 'saved' ORDER BY timestamp DESC")
    fun getWatchlist(): Flow<List<SavedMovieEntity>>

    @Query("SELECT * FROM saved_movies WHERE isHistory = 1 ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<SavedMovieEntity>>

    @Query("SELECT * FROM saved_movies WHERE isHistory = 1 AND status = 'seen' ORDER BY timestamp DESC")
    fun getSeenHistory(): Flow<List<SavedMovieEntity>>

    @Query("SELECT * FROM saved_movies WHERE isHistory = 1 AND status = 'not_interested' ORDER BY timestamp DESC")
    fun getNotInterestedHistory(): Flow<List<SavedMovieEntity>>

    @Query("SELECT * FROM saved_movies WHERE isHistory = 1 AND (isWatchlist = 1 OR status = 'saved') ORDER BY timestamp DESC")
    fun getSavedHistory(): Flow<List<SavedMovieEntity>>

    @Query("SELECT id FROM saved_movies WHERE status = 'seen' OR status = 'not_interested'")
    suspend fun getExcludedIds(): List<String>

    @Query("SELECT * FROM saved_movies WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SavedMovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(movie: SavedMovieEntity)

    @Query("UPDATE saved_movies SET isWatchlist = 0, status = 'discovered' WHERE id = :id")
    suspend fun removeFromWatchlist(id: String)

    @Query("UPDATE saved_movies SET isHistory = 0")
    suspend fun clearHistory()

    @Query("DELETE FROM saved_movies WHERE isWatchlist = 0 AND isHistory = 0 AND status = 'discovered'")
    suspend fun cleanOrphaned()

    // Seen Movies queries
    @Query("SELECT * FROM seen_movies ORDER BY timestamp DESC")
    fun getAllSeen(): Flow<List<SeenMovieEntity>>

    @Query("SELECT id FROM seen_movies")
    suspend fun getSeenIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeen(seen: SeenMovieEntity)

    @Query("DELETE FROM seen_movies WHERE id = :id")
    suspend fun removeFromSeen(id: String)

    @Query("SELECT COUNT(*) FROM seen_movies WHERE id = :id")
    suspend fun isSeen(id: String): Int
}
