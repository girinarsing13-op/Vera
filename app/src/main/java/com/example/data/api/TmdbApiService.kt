package com.example.data.api

import com.example.data.model.TmdbDiscoverResponse
import com.example.data.model.TmdbMovieDetails
import com.example.data.model.TmdbMovieResult
import com.example.data.model.TmdbTvDetails
import com.example.data.model.TmdbTvResult
import com.example.data.model.TmdbWatchProvidersResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("3/discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c",
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String? = null,
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("primary_release_date.gte") releaseDateGte: String? = null,
        @Query("primary_release_date.lte") releaseDateLte: String? = null,
        @Query("region") region: String? = null,
        @Query("with_watch_providers") withProviders: String? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("with_original_language") withLanguage: String? = null
    ): TmdbDiscoverResponse<TmdbMovieResult>

    @GET("3/movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c",
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): TmdbDiscoverResponse<TmdbMovieResult>

    @GET("3/movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c",
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): TmdbDiscoverResponse<TmdbMovieResult>

    @GET("3/discover/tv")
    suspend fun discoverTv(
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c",
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String? = null,
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("with_watch_providers") withProviders: String? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("with_original_language") withLanguage: String? = null
    ): TmdbDiscoverResponse<TmdbTvResult>

    @GET("3/movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c",
        @Query("append_to_response") append: String = "credits"
    ): TmdbMovieDetails

    @GET("3/tv/{id}")
    suspend fun getTvDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c",
        @Query("append_to_response") append: String = "credits"
    ): TmdbTvDetails

    @GET("3/movie/{id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c"
    ): TmdbWatchProvidersResponse

    @GET("3/tv/{id}/watch/providers")
    suspend fun getTvWatchProviders(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c"
    ): TmdbWatchProvidersResponse

    @GET("3/search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = "3fd2be6f0c70a2a598f084ddfb75487c",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): com.example.data.model.TmdbDiscoverResponse<com.example.data.model.TmdbMultiSearchResult>
}
