package com.example.data.repository

import com.example.data.api.TmdbApiService
import com.example.data.model.DiscoveryConstants
import com.example.data.model.MediaItem
import com.example.data.model.SeasonInfo
import com.example.data.model.WatchProvider
import com.example.data.model.TmdbProviderInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class CatalogRepository {

    private val api: TmdbApiService

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        api = retrofit.create(TmdbApiService::class.java)
    }

    private val cache = ConcurrentHashMap<String, List<MediaItem>>()
    private val detailsCache = ConcurrentHashMap<String, MediaItem>()

    /**
     * Fetches a balanced multi-era cinema catalog across the entire history of cinema (1940s to 2020s),
     * strictly respecting the typeFilter ("all", "Movie", "Series") and selectedEra.
     */
    suspend fun fetchCatalog(
        typeFilter: String,
        region: String,
        language: String,
        selectedPlatforms: Set<String>,
        selectedEra: String = "Any Era"
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val candidateMap = mutableMapOf<String, MediaItem>()

        val fetchMovies = typeFilter.equals("all", ignoreCase = true) || typeFilter.equals("Movie", ignoreCase = true)
        val fetchSeries = typeFilter.equals("all", ignoreCase = true) || typeFilter.equals("Series", ignoreCase = true)

        val providerIds = if (selectedPlatforms.isNotEmpty()) {
            DiscoveryConstants.getPlatformProviderIds(selectedPlatforms)
        } else {
            emptyList()
        }
        val withProviders = if (providerIds.isNotEmpty()) providerIds.joinToString("|") else null
        val watchRegionParam = if (withProviders != null) region else null
        val activePlatformsList = selectedPlatforms.toList()
        val langParam = if (language != "ANY") language else null

        // 1. FETCH BALANCED MOVIES (Multiple Eras)
        if (fetchMovies) {
            when (selectedEra) {
                "2020s" -> fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                    releaseDateGte = "2020-01-01", pages = listOf(1, 2, 3))

                "2010s" -> fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                    releaseDateGte = "2010-01-01", releaseDateLte = "2019-12-31", pages = listOf(1, 2, 3))

                "2000s" -> fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                    releaseDateGte = "2000-01-01", releaseDateLte = "2009-12-31", pages = listOf(1, 2, 3))

                "1990s" -> fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                    releaseDateGte = "1990-01-01", releaseDateLte = "1999-12-31", pages = listOf(1, 2, 3))

                "1980s" -> fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                    releaseDateGte = "1980-01-01", releaseDateLte = "1989-12-31", pages = listOf(1, 2, 3))

                "1970s" -> fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                    releaseDateGte = "1970-01-01", releaseDateLte = "1979-12-31", pages = listOf(1, 2))

                "Before 1970" -> fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                    releaseDateLte = "1969-12-31", voteCountGte = 80, sortBy = "vote_average.desc", pages = listOf(1, 2))

                else -> {
                    // "Any Era" -> True balanced multi-era cinema catalog!
                    // Slice A: Top Rated of all time (covers legendary golden cinema 1940-2020s)
                    try {
                        val topResp = api.getTopRatedMovies(page = 1, region = region)
                        val pageItems = mapMovieResults(topResp.results, activePlatformsList)
                        pageItems.forEach { candidateMap[it.id] = it }
                    } catch (_: Exception) {}

                    // Slice B: 20th Century Classics (pre-2000: 1950s, 60s, 70s, 80s, 90s)
                    fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                        releaseDateLte = "1999-12-31", voteCountGte = 250, sortBy = "vote_average.desc", pages = listOf(1))

                    // Slice C: 1980s & 1990s acclaimed & popular
                    fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                        releaseDateGte = "1980-01-01", releaseDateLte = "1999-12-31", sortBy = "popularity.desc", pages = listOf(1))

                    // Slice D: 2000s & 2010s modern classics
                    fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                        releaseDateGte = "2000-01-01", releaseDateLte = "2019-12-31", voteCountGte = 600, sortBy = "vote_average.desc", pages = listOf(1))

                    // Slice E: Contemporary & Recent (2020-2026)
                    fetchMovieSlice(candidateMap, region, langParam, withProviders, watchRegionParam, activePlatformsList,
                        releaseDateGte = "2020-01-01", sortBy = "popularity.desc", pages = listOf(1, 2))
                }
            }
        }

        // 2. FETCH TV SERIES (Only when requested, without letting them dominate)
        if (fetchSeries) {
            val seriesPages = if (typeFilter.equals("Series", ignoreCase = true)) listOf(1, 2, 3) else listOf(1)
            for (page in seriesPages) {
                val cacheKey = "tv_${region}_${language}_${page}_${withProviders ?: ""}"
                val cached = cache[cacheKey]
                if (cached != null) {
                    cached.forEach { candidateMap[it.id] = it }
                    continue
                }

                try {
                    val resp = api.discoverTv(
                        page = page,
                        withProviders = withProviders,
                        watchRegion = watchRegionParam,
                        withLanguage = langParam
                    )
                    val pageItems = resp.results.mapNotNull { tv ->
                        val posterPath = tv.posterPath ?: return@mapNotNull null
                        val year = tv.firstAirDate?.split("-")?.firstOrNull()?.toIntOrNull() ?: 2022
                        val genres = tv.genreIds.mapNotNull { DiscoveryConstants.TMDB_GENRES[it] }
                        MediaItem(
                            id = "t_${tv.id}",
                            tmdbId = tv.id,
                            title = tv.name ?: "Untitled",
                            type = "Series",
                            year = year,
                            duration = "1 Season",
                            rating = tv.voteAverage ?: 7.0,
                            popularity = tv.popularity ?: 50.0,
                            trending = (tv.popularity ?: 0.0) > 75.0,
                            originalLanguage = tv.originalLanguage ?: "en",
                            country = null,
                            genres = genres,
                            platforms = activePlatformsList,
                            synopsis = tv.overview?.takeIf { it.isNotBlank() } ?: "No synopsis available.",
                            poster = "https://image.tmdb.org/t/p/w500$posterPath"
                        )
                    }
                    if (pageItems.isNotEmpty()) {
                        cache[cacheKey] = pageItems
                        pageItems.forEach { candidateMap[it.id] = it }
                    }
                } catch (_: Exception) {}
            }
        }

        // Inject curated fallback catalog (which spans 1940s, 50s, 60s, 70s, 80s, 90s, 2000s, 2010s, 2020s)
        val curated = CuratedCatalogData.getAllCuratedMedia()
        for (item in curated) {
            if (fetchMovies && item.type == "Movie") {
                candidateMap.putIfAbsent(item.id, item)
            } else if (fetchSeries && item.type == "Series") {
                candidateMap.putIfAbsent(item.id, item)
            }
        }

        candidateMap.values.toList()
    }

    private suspend fun fetchMovieSlice(
        destination: MutableMap<String, MediaItem>,
        region: String,
        language: String?,
        withProviders: String?,
        watchRegion: String?,
        activePlatformsList: List<String>,
        releaseDateGte: String? = null,
        releaseDateLte: String? = null,
        voteCountGte: Int? = null,
        sortBy: String? = null,
        pages: List<Int>
    ) {
        for (page in pages) {
            val cacheKey = "m_${region}_${language}_${page}_${releaseDateGte}_${releaseDateLte}_${sortBy}"
            val cached = cache[cacheKey]
            if (cached != null) {
                cached.forEach { destination[it.id] = it }
                continue
            }

            try {
                val resp = api.discoverMovies(
                    page = page,
                    sortBy = sortBy,
                    voteCountGte = voteCountGte,
                    releaseDateGte = releaseDateGte,
                    releaseDateLte = releaseDateLte,
                    region = region,
                    withProviders = withProviders,
                    watchRegion = watchRegion,
                    withLanguage = language
                )
                val pageItems = mapMovieResults(resp.results, activePlatformsList)
                if (pageItems.isNotEmpty()) {
                    cache[cacheKey] = pageItems
                    pageItems.forEach { destination[it.id] = it }
                }
            } catch (_: Exception) {}
        }
    }

    private fun mapMovieResults(
        results: List<com.example.data.model.TmdbMovieResult>,
        activePlatformsList: List<String>
    ): List<MediaItem> {
        return results.mapNotNull { m ->
            val posterPath = m.posterPath ?: return@mapNotNull null
            val year = m.releaseDate?.split("-")?.firstOrNull()?.toIntOrNull() ?: 2023
            val genres = m.genreIds.mapNotNull { DiscoveryConstants.TMDB_GENRES[it] }
            MediaItem(
                id = "m_${m.id}",
                tmdbId = m.id,
                title = m.title ?: "Untitled",
                type = "Movie",
                year = year,
                duration = "2h",
                rating = m.voteAverage ?: 7.0,
                popularity = m.popularity ?: 50.0,
                trending = (m.popularity ?: 0.0) > 75.0,
                originalLanguage = m.originalLanguage ?: "en",
                country = null,
                genres = genres,
                platforms = activePlatformsList,
                synopsis = m.overview?.takeIf { it.isNotBlank() } ?: "No synopsis available.",
                poster = "https://image.tmdb.org/t/p/w500$posterPath"
            )
        }
    }

    /**
     * Home Screen Showcase Posters: A diverse, high-appeal, multi-era cinematic showcase.
     */
    fun getShowcaseMedia(): List<MediaItem> = CuratedCatalogData.getShowcaseMedia()

    suspend fun fetchShowcasePosters(): List<MediaItem> = withContext(Dispatchers.IO) {
        val staticShowcase = CuratedCatalogData.getShowcaseMedia()
        try {
            val topResp = api.getTopRatedMovies(page = 1)
            val popResp = api.getPopularMovies(page = 1)
            val dynamicItems = (mapMovieResults(topResp.results, emptyList()) + mapMovieResults(popResp.results, emptyList()))
                .filter { it.poster.isNotBlank() && it.rating >= 7.8 }
                .distinctBy { it.id }

            if (dynamicItems.isNotEmpty()) {
                // Mix static all-time classics with dynamic top rated
                (staticShowcase.take(6) + dynamicItems.take(8)).distinctBy { it.id }
            } else {
                staticShowcase
            }
        } catch (_: Exception) {
            staticShowcase
        }
    }

    suspend fun fetchDetailedMetadata(item: MediaItem, region: String = "IN"): MediaItem = withContext(Dispatchers.IO) {
        val cacheKey = "${item.id}_$region"
        val cached = detailsCache[cacheKey]
        if (cached != null) return@withContext cached

        val providersList = mutableListOf<WatchProvider>()
        var watchLink: String? = null

        try {
            val wpResp = if (item.type == "Movie") {
                api.getMovieWatchProviders(item.tmdbId)
            } else {
                api.getTvWatchProviders(item.tmdbId)
            }

            val regInfo: com.example.data.model.TmdbCountryWatchProviders? = wpResp.results[region]
                ?: wpResp.results["US"]
                ?: wpResp.results.values.firstOrNull()

            if (regInfo != null) {
                watchLink = regInfo.link
                regInfo.flatrate?.forEach { p ->
                    providersList.add(
                        WatchProvider(
                            id = p.providerId,
                            name = cleanProviderName(p.providerName),
                            logoUrl = "https://image.tmdb.org/t/p/w154${p.logoPath}",
                            type = "Stream",
                            link = regInfo.link
                        )
                    )
                }
                regInfo.free?.forEach { p ->
                    providersList.add(
                        WatchProvider(
                            id = p.providerId,
                            name = cleanProviderName(p.providerName),
                            logoUrl = "https://image.tmdb.org/t/p/w154${p.logoPath}",
                            type = "Free",
                            link = regInfo.link
                        )
                    )
                }
                regInfo.rent?.forEach { p ->
                    providersList.add(
                        WatchProvider(
                            id = p.providerId,
                            name = cleanProviderName(p.providerName),
                            logoUrl = "https://image.tmdb.org/t/p/w154${p.logoPath}",
                            type = "Rent",
                            link = regInfo.link
                        )
                    )
                }
                regInfo.buy?.forEach { p ->
                    providersList.add(
                        WatchProvider(
                            id = p.providerId,
                            name = cleanProviderName(p.providerName),
                            logoUrl = "https://image.tmdb.org/t/p/w154${p.logoPath}",
                            type = "Buy",
                            link = regInfo.link
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        var directorName: String? = item.director
        var topCast: List<String> = item.cast
        var durationStr = item.duration
        var seasonCount = item.numberOfSeasons
        var episodeCount = item.numberOfEpisodes
        var seasonsList: List<SeasonInfo> = item.seasons

        try {
            if (item.type == "Movie") {
                val details = api.getMovieDetails(item.tmdbId)
                if (details.runtime != null && details.runtime > 0) {
                    val hrs = details.runtime / 60
                    val mins = details.runtime % 60
                    durationStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                }
                directorName = details.credits?.crew?.firstOrNull { it.job == "Director" }?.name ?: directorName
                topCast = details.credits?.cast?.take(5)?.mapNotNull { it.name } ?: topCast
            } else {
                val tvDetails = api.getTvDetails(item.tmdbId)
                seasonCount = tvDetails.numberOfSeasons ?: seasonCount
                episodeCount = tvDetails.numberOfEpisodes ?: episodeCount
                durationStr = "$seasonCount Season${if (seasonCount > 1) "s" else ""}"
                topCast = tvDetails.credits?.cast?.take(5)?.mapNotNull { it.name } ?: topCast
                seasonsList = tvDetails.seasons?.map { s ->
                    SeasonInfo(
                        seasonNumber = s.seasonNumber,
                        name = s.name ?: "Season ${s.seasonNumber}",
                        episodeCount = s.episodeCount ?: 0,
                        overview = s.overview
                    )
                } ?: seasonsList
            }
        } catch (_: Exception) {}

        val distinctProviders = providersList.distinctBy { "${it.name}_${it.type}" }

        val enriched = item.copy(
            duration = durationStr,
            director = directorName,
            cast = topCast,
            watchProviders = distinctProviders,
            watchLink = watchLink,
            numberOfSeasons = seasonCount,
            numberOfEpisodes = episodeCount,
            seasons = seasonsList
        )
        detailsCache[cacheKey] = enriched
        enriched
    }

    private fun cleanProviderName(raw: String): String {
        return when {
            raw.contains("Hotstar", ignoreCase = true) -> "JioHotstar"
            raw.contains("Prime Video", ignoreCase = true) || raw.contains("Amazon Video", ignoreCase = true) -> "Prime Video"
            raw.contains("Apple TV", ignoreCase = true) -> "Apple TV"
            raw.contains("Netflix", ignoreCase = true) -> "Netflix"
            raw.contains("YouTube", ignoreCase = true) -> "YouTube"
            raw.contains("Google Play", ignoreCase = true) -> "Google Play"
            raw.contains("Zee5", ignoreCase = true) -> "Zee5"
            raw.contains("Sony", ignoreCase = true) -> "Sony LIV"
            else -> raw
        }
    }

    fun getCuratedFallbacks(): List<MediaItem> = CuratedCatalogData.getAllCuratedMedia()

    fun getAllLocalMedia(): List<MediaItem> {
        val pool = mutableMapOf<String, MediaItem>()
        getCuratedFallbacks().forEach { pool[it.id] = it }
        cache.values.forEach { list -> list.forEach { pool[it.id] = it } }
        return pool.values.toList()
    }

    fun searchLocal(query: String): List<MediaItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val allPool = getAllLocalMedia()
        val prefixMatches = mutableListOf<MediaItem>()
        val containsMatches = mutableListOf<MediaItem>()
        val secondaryMatches = mutableListOf<MediaItem>()
        val queryLower = trimmed.lowercase()

        for (item in allPool) {
            val titleLower = item.title.lowercase()
            if (titleLower.startsWith(queryLower)) {
                prefixMatches.add(item)
            } else if (titleLower.contains(queryLower)) {
                containsMatches.add(item)
            } else if (item.director?.contains(queryLower, ignoreCase = true) == true ||
                       item.cast.any { it.contains(queryLower, ignoreCase = true) } ||
                       item.genres.any { it.contains(queryLower, ignoreCase = true) }) {
                secondaryMatches.add(item)
            }
        }

        prefixMatches.sortByDescending { it.popularity }
        containsMatches.sortByDescending { it.popularity }
        secondaryMatches.sortByDescending { it.popularity }

        return (prefixMatches + containsMatches + secondaryMatches).distinctBy { it.id }
    }

    suspend fun searchCatalog(query: String, region: String): List<MediaItem> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val localResults = searchLocal(trimmed)

        try {
            val resp = api.searchMulti(query = trimmed)
            val remoteResults = mutableListOf<MediaItem>()

            for (item in resp.results) {
                when (item.mediaType) {
                    "movie" -> {
                        val posterPath = item.posterPath ?: continue
                        val year = item.releaseDate?.split("-")?.firstOrNull()?.toIntOrNull() ?: 2023
                        val genres = item.genreIds.mapNotNull { DiscoveryConstants.TMDB_GENRES[it] }
                        remoteResults.add(
                            MediaItem(
                                id = "m_${item.id}",
                                tmdbId = item.id,
                                title = item.title ?: "Untitled",
                                type = "Movie",
                                year = year,
                                duration = "2h",
                                rating = item.voteAverage ?: 7.0,
                                popularity = item.popularity ?: 50.0,
                                trending = (item.popularity ?: 0.0) > 75.0,
                                originalLanguage = item.originalLanguage ?: "en",
                                country = null,
                                genres = genres,
                                platforms = emptyList(),
                                synopsis = item.overview?.takeIf { it.isNotBlank() } ?: "No synopsis available.",
                                poster = "https://image.tmdb.org/t/p/w500$posterPath"
                            )
                        )
                    }
                    "tv" -> {
                        val posterPath = item.posterPath ?: continue
                        val year = item.firstAirDate?.split("-")?.firstOrNull()?.toIntOrNull() ?: 2023
                        val genres = item.genreIds.mapNotNull { DiscoveryConstants.TMDB_GENRES[it] }
                        remoteResults.add(
                            MediaItem(
                                id = "t_${item.id}",
                                tmdbId = item.id,
                                title = item.name ?: "Untitled",
                                type = "Series",
                                year = year,
                                duration = "1 Season",
                                rating = item.voteAverage ?: 7.0,
                                popularity = item.popularity ?: 50.0,
                                trending = (item.popularity ?: 0.0) > 75.0,
                                originalLanguage = item.originalLanguage ?: "en",
                                country = null,
                                genres = genres,
                                platforms = emptyList(),
                                synopsis = item.overview?.takeIf { it.isNotBlank() } ?: "No synopsis available.",
                                poster = "https://image.tmdb.org/t/p/w500$posterPath"
                            )
                        )
                    }
                    "person" -> {
                        val knownFor = item.knownFor ?: emptyList()
                        for (km in knownFor) {
                            val posterPath = km.posterPath ?: continue
                            val year = km.releaseDate?.split("-")?.firstOrNull()?.toIntOrNull() ?: 2023
                            val genres = km.genreIds.mapNotNull { DiscoveryConstants.TMDB_GENRES[it] }
                            remoteResults.add(
                                MediaItem(
                                    id = "m_${km.id}",
                                    tmdbId = km.id,
                                    title = km.title ?: "Untitled",
                                    type = "Movie",
                                    year = year,
                                    duration = "2h",
                                    rating = km.voteAverage ?: 7.0,
                                    popularity = km.popularity ?: 50.0,
                                    trending = false,
                                    originalLanguage = km.originalLanguage ?: "en",
                                    country = null,
                                    genres = genres,
                                    platforms = emptyList(),
                                    synopsis = km.overview?.takeIf { it.isNotBlank() } ?: "Featuring ${item.name}",
                                    poster = "https://image.tmdb.org/t/p/w500$posterPath"
                                )
                            )
                        }
                    }
                }
            }

            val combined = (remoteResults + localResults).distinctBy { it.id }
            val queryLower = trimmed.lowercase()

            val prefix = mutableListOf<MediaItem>()
            val contains = mutableListOf<MediaItem>()
            val other = mutableListOf<MediaItem>()

            for (item in combined) {
                val t = item.title.lowercase()
                if (t.startsWith(queryLower)) {
                    prefix.add(item)
                } else if (t.contains(queryLower)) {
                    contains.add(item)
                } else {
                    other.add(item)
                }
            }

            prefix.sortByDescending { it.popularity }
            contains.sortByDescending { it.popularity }
            other.sortByDescending { it.popularity }

            (prefix + contains + other).distinctBy { it.id }
        } catch (_: Exception) {
            localResults
        }
    }
}
