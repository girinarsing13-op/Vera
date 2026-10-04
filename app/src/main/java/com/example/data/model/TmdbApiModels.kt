package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbDiscoverResponse<T>(
    @Json(name = "page") val page: Int = 1,
    @Json(name = "results") val results: List<T> = emptyList(),
    @Json(name = "total_pages") val totalPages: Int = 1,
    @Json(name = "total_results") val totalResults: Int = 0
)

@JsonClass(generateAdapter = true)
data class TmdbMovieResult(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "popularity") val popularity: Double? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbTvResult(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "popularity") val popularity: Double? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbMovieDetails(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "popularity") val popularity: Double? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "production_countries") val productionCountries: List<ProductionCountry> = emptyList(),
    @Json(name = "credits") val credits: TmdbCredits? = null
)

@JsonClass(generateAdapter = true)
data class TmdbTvDetails(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int? = null,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "popularity") val popularity: Double? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "production_countries") val productionCountries: List<ProductionCountry> = emptyList(),
    @Json(name = "seasons") val seasons: List<TmdbSeason>? = null,
    @Json(name = "credits") val credits: TmdbCredits? = null
)

@JsonClass(generateAdapter = true)
data class TmdbSeason(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "season_number") val seasonNumber: Int = 1,
    @Json(name = "name") val name: String? = null,
    @Json(name = "episode_count") val episodeCount: Int? = null,
    @Json(name = "overview") val overview: String? = null
)

@JsonClass(generateAdapter = true)
data class ProductionCountry(
    @Json(name = "iso_3166_1") val iso: String? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbCredits(
    @Json(name = "cast") val cast: List<TmdbCastMember> = emptyList(),
    @Json(name = "crew") val crew: List<TmdbCrewMember> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbCastMember(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "character") val character: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbCrewMember(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "job") val job: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbWatchProvidersResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "results") val results: Map<String, TmdbCountryWatchProviders> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class TmdbCountryWatchProviders(
    @Json(name = "link") val link: String? = null,
    @Json(name = "flatrate") val flatrate: List<TmdbProviderInfo>? = null,
    @Json(name = "rent") val rent: List<TmdbProviderInfo>? = null,
    @Json(name = "buy") val buy: List<TmdbProviderInfo>? = null,
    @Json(name = "free") val free: List<TmdbProviderInfo>? = null,
    @Json(name = "ads") val ads: List<TmdbProviderInfo>? = null
)

@JsonClass(generateAdapter = true)
data class TmdbProviderInfo(
    @Json(name = "provider_id") val providerId: Int,
    @Json(name = "provider_name") val providerName: String,
    @Json(name = "logo_path") val logoPath: String? = null,
    @Json(name = "display_priority") val displayPriority: Int? = null
)

@JsonClass(generateAdapter = true)
data class TmdbMultiSearchResult(
    @Json(name = "id") val id: Int,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "popularity") val popularity: Double? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList(),
    @Json(name = "known_for_department") val knownForDepartment: String? = null,
    @Json(name = "known_for") val knownFor: List<TmdbMovieResult>? = null
)
