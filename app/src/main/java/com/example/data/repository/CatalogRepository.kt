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

    suspend fun fetchCatalog(
        typeFilter: String,
        region: String,
        language: String,
        selectedPlatforms: Set<String>
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val candidateMap = mutableMapOf<String, MediaItem>()
        val pagesToFetch = listOf(1, 2, 3, 4, 5, 6)

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

        // Fetch movies
        if (fetchMovies) {
            for (page in pagesToFetch) {
                val cacheKey = "m_${region}_${language}_${page}_${withProviders ?: ""}"
                val cached = cache[cacheKey]
                if (cached != null) {
                    cached.forEach { candidateMap[it.id] = it }
                    continue
                }

                try {
                    val resp = api.discoverMovies(
                        page = page,
                        region = region,
                        withProviders = withProviders,
                        watchRegion = watchRegionParam,
                        withLanguage = langParam
                    )
                    val pageItems = resp.results.mapNotNull { m ->
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
                    if (pageItems.isNotEmpty()) {
                        cache[cacheKey] = pageItems
                        pageItems.forEach { candidateMap[it.id] = it }
                    }
                } catch (_: Exception) {}
            }
        }

        // Fetch TV series
        if (fetchSeries) {
            for (page in pagesToFetch) {
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

        // If network failed or catalog is empty, inject curated fallback catalog
        if (candidateMap.isEmpty()) {
            getCuratedFallbacks().forEach { candidateMap[it.id] = it }
        }

        candidateMap.values.toList()
    }

    suspend fun fetchDetailedMetadata(item: MediaItem, region: String = "IN"): MediaItem = withContext(Dispatchers.IO) {
        val cacheKey = "${item.id}_$region"
        val cached = detailsCache[cacheKey]
        if (cached != null) return@withContext cached

        // 1. Fetch real watch provider information for the specified region
        val providersList = mutableListOf<WatchProvider>()
        var watchLink: String? = null

        try {
            val wpResp = if (item.type == "Movie") {
                api.getMovieWatchProviders(item.tmdbId)
            } else {
                api.getTvWatchProviders(item.tmdbId)
            }
            val countryData = wpResp.results[region] ?: wpResp.results["IN"]
            if (countryData != null) {
                watchLink = countryData.link
                val seenNames = mutableSetOf<String>()

                fun addProviders(list: List<TmdbProviderInfo>?, type: String) {
                    list?.forEach { p ->
                        val cleanName = cleanProviderName(p.providerName)
                        if (seenNames.add(cleanName.lowercase())) {
                            val logo = p.logoPath?.let { "https://image.tmdb.org/t/p/w200$it" }
                            providersList.add(
                                WatchProvider(
                                    id = p.providerId,
                                    name = cleanName,
                                    logoUrl = logo,
                                    type = type,
                                    link = countryData.link
                                )
                            )
                        }
                    }
                }

                // Subscription streaming (flatrate) first
                addProviders(countryData.flatrate, "Stream")
                // Free streaming / ads
                addProviders(countryData.free, "Free")
                addProviders(countryData.ads, "Free with ads")
                // Rent
                addProviders(countryData.rent, "Rent")
                // Buy
                addProviders(countryData.buy, "Buy")
            }
        } catch (e: Exception) {
            android.util.Log.e("CatalogRepository", "Watch providers fetch error for ${item.id}", e)
        }

        val platformsList = if (providersList.isNotEmpty()) {
            providersList.map { it.name }
        } else {
            emptyList()
        }

        try {
            if (item.type == "Movie") {
                val details = api.getMovieDetails(item.tmdbId)
                val hrs = (details.runtime ?: 120) / 60
                val mins = (details.runtime ?: 120) % 60
                val durationStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                val country = details.productionCountries.firstOrNull()?.name
                val director = details.credits?.crew?.firstOrNull { it.job == "Director" }?.name
                val castList = details.credits?.cast?.take(4)?.mapNotNull { it.name } ?: emptyList()

                val enriched = item.copy(
                    duration = durationStr,
                    country = country,
                    director = director,
                    cast = castList,
                    platforms = platformsList,
                    watchProviders = providersList,
                    watchLink = watchLink
                )
                detailsCache[cacheKey] = enriched
                enriched
            } else {
                val details = api.getTvDetails(item.tmdbId)
                val seasonsCount = details.numberOfSeasons ?: (details.seasons?.size ?: 1)
                val episodesCount = details.numberOfEpisodes ?: (seasonsCount * 8)
                val country = details.productionCountries.firstOrNull()?.name
                val castList = details.credits?.cast?.take(4)?.mapNotNull { it.name } ?: emptyList()
                val seasons = details.seasons?.filter { it.seasonNumber > 0 }?.map {
                    SeasonInfo(
                        seasonNumber = it.seasonNumber,
                        name = it.name ?: "Season ${it.seasonNumber}",
                        episodeCount = it.episodeCount ?: 8,
                        overview = it.overview
                    )
                } ?: emptyList()

                val enriched = item.copy(
                    numberOfSeasons = seasonsCount,
                    numberOfEpisodes = episodesCount,
                    duration = "$seasonsCount Seasons • $episodesCount Episodes",
                    country = country,
                    cast = castList,
                    seasons = seasons,
                    platforms = platformsList,
                    watchProviders = providersList,
                    watchLink = watchLink
                )
                detailsCache[cacheKey] = enriched
                enriched
            }
        } catch (_: Exception) {
            val enriched = item.copy(
                platforms = platformsList,
                watchProviders = providersList,
                watchLink = watchLink
            )
            detailsCache[cacheKey] = enriched
            enriched
        }
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

    fun getCuratedFallbacks(): List<MediaItem> = listOf(
        MediaItem(
            id = "m_157336",
            tmdbId = 157336,
            title = "Interstellar",
            type = "Movie",
            year = 2014,
            duration = "2h 49m",
            rating = 8.7,
            popularity = 95.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Sci-Fi", "Drama", "Adventure"),
            platforms = listOf("Prime Video", "Apple TV"),
            synopsis = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
            poster = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            director = "Christopher Nolan",
            cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine")
        ),
        MediaItem(
            id = "m_27205",
            tmdbId = 27205,
            title = "Inception",
            type = "Movie",
            year = 2010,
            duration = "2h 28m",
            rating = 8.8,
            popularity = 92.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Action", "Sci-Fi", "Thriller"),
            platforms = listOf("Netflix", "Prime Video"),
            synopsis = "Cobb, a skilled thief who commits corporate espionage by infiltrating the subconscious of his targets, is offered a chance to regain his old life as payment for a task considered to be impossible: \"inception\".",
            poster = "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            director = "Christopher Nolan",
            cast = listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page", "Tom Hardy")
        ),
        MediaItem(
            id = "m_1726",
            tmdbId = 1726,
            title = "Iron Man",
            type = "Movie",
            year = 2008,
            duration = "2h 6m",
            rating = 7.6,
            popularity = 89.0,
            trending = false,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Action", "Sci-Fi", "Adventure"),
            platforms = listOf("Disney+ Hotstar"),
            synopsis = "After being held captive in an Afghan cave, billionaire engineer Tony Stark creates a unique weaponized suit of armor to fight evil.",
            poster = "https://image.tmdb.org/t/p/w500/78lPtwv72eTNqFW9COBYI0dWDJa.jpg",
            director = "Jon Favreau",
            cast = listOf("Robert Downey Jr.", "Gwyneth Paltrow", "Terrence Howard", "Jeff Bridges")
        ),
        MediaItem(
            id = "m_150540",
            tmdbId = 150540,
            title = "Inside Out",
            type = "Movie",
            year = 2015,
            duration = "1h 35m",
            rating = 7.9,
            popularity = 85.0,
            trending = false,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Animation", "Family", "Adventure", "Comedy"),
            platforms = listOf("Disney+ Hotstar"),
            synopsis = "Growing up can be a bumpy road, and it is no exception for Riley, who is uprooted from her Midwest life when her father starts a new job in San Francisco.",
            poster = "https://image.tmdb.org/t/p/w500/2H1TmgdfNbfMqqiqzeuvneG04qu.jpg",
            director = "Pete Docter",
            cast = listOf("Amy Poehler", "Phyllis Smith", "Richard Kind", "Bill Hader")
        ),
        MediaItem(
            id = "m_16869",
            tmdbId = 16869,
            title = "Inglourious Basterds",
            type = "Movie",
            year = 2009,
            duration = "2h 33m",
            rating = 8.2,
            popularity = 87.0,
            trending = false,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Drama", "Thriller", "War"),
            platforms = listOf("Netflix", "Prime Video"),
            synopsis = "In Nazi-occupied France during World War II, a group of Jewish-American soldiers known as The Basterds are chosen specifically to spread fear throughout the Third Reich.",
            poster = "https://image.tmdb.org/t/p/w500/7sfbEnaARXDD5Km007qsHQM0v01.jpg",
            director = "Quentin Tarantino",
            cast = listOf("Brad Pitt", "Christoph Waltz", "Melanie Laurent", "Michael Fassbender")
        ),
        MediaItem(
            id = "m_414906",
            tmdbId = 414906,
            title = "The Batman",
            type = "Movie",
            year = 2022,
            duration = "2h 56m",
            rating = 7.7,
            popularity = 93.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Crime", "Mystery", "Thriller", "Action"),
            platforms = listOf("Netflix", "Prime Video", "Apple TV"),
            synopsis = "In his second year of fighting crime, Batman uncovers corruption in Gotham City that connects to his own family while facing a serial killer known as the Riddler.",
            poster = "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg",
            director = "Matt Reeves",
            cast = listOf("Robert Pattinson", "Zoe Kravitz", "Paul Dano", "Colin Farrell")
        ),
        MediaItem(
            id = "m_272",
            tmdbId = 272,
            title = "Batman Begins",
            type = "Movie",
            year = 2005,
            duration = "2h 20m",
            rating = 7.7,
            popularity = 88.0,
            trending = false,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Action", "Crime", "Drama"),
            platforms = listOf("Prime Video", "Apple TV"),
            synopsis = "Driven by tragedy, billionaire Bruce Wayne dedicates his life to uncovering and defeating the corruption that plagues his home of Gotham City.",
            poster = "https://image.tmdb.org/t/p/w500/4MpN4Cwhv4agFOEkzgkV22709iz.jpg",
            director = "Christopher Nolan",
            cast = listOf("Christian Bale", "Michael Caine", "Liam Neeson", "Katie Holmes")
        ),
        MediaItem(
            id = "m_155",
            tmdbId = 155,
            title = "The Dark Knight",
            type = "Movie",
            year = 2008,
            duration = "2h 32m",
            rating = 8.5,
            popularity = 97.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Drama", "Action", "Crime", "Thriller"),
            platforms = listOf("Netflix", "Prime Video", "Apple TV"),
            synopsis = "Batman raises the stakes in his war on crime. With the help of Lt. Jim Gordon and District Attorney Harvey Dent, Batman sets out to dismantle the remaining criminal organizations that plague the streets.",
            poster = "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            director = "Christopher Nolan",
            cast = listOf("Christian Bale", "Heath Ledger", "Aaron Eckhart", "Michael Caine")
        ),
        MediaItem(
            id = "m_872585",
            tmdbId = 872585,
            title = "Oppenheimer",
            type = "Movie",
            year = 2023,
            duration = "3h 0m",
            rating = 8.1,
            popularity = 96.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Drama", "History"),
            platforms = listOf("Prime Video", "Apple TV"),
            synopsis = "The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II.",
            poster = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
            director = "Christopher Nolan",
            cast = listOf("Cillian Murphy", "Emily Blunt", "Matt Damon", "Robert Downey Jr.")
        ),
        MediaItem(
            id = "m_693134",
            tmdbId = 693134,
            title = "Dune: Part Two",
            type = "Movie",
            year = 2024,
            duration = "2h 46m",
            rating = 8.2,
            popularity = 95.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Sci-Fi", "Adventure"),
            platforms = listOf("Prime Video", "Apple TV"),
            synopsis = "Follow the mythic journey of Paul Atreides as he unites with Chani and the Fremen while on a path of revenge against the conspirators who destroyed his family.",
            poster = "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            director = "Denis Villeneuve",
            cast = listOf("Timothee Chalamet", "Zendaya", "Rebecca Ferguson", "Javier Bardem")
        ),
        MediaItem(
            id = "t_1396",
            tmdbId = 1396,
            title = "Breaking Bad",
            type = "Series",
            year = 2008,
            duration = "5 Seasons • 62 Episodes",
            rating = 8.9,
            popularity = 96.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Drama", "Crime", "Thriller"),
            platforms = listOf("Netflix"),
            synopsis = "Walter White, a New Mexico chemistry teacher, is diagnosed with Stage III cancer and given a prognosis of two years left to live. He chooses to enter a dangerous world of drugs and crime.",
            poster = "https://image.tmdb.org/t/p/w500/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
            numberOfSeasons = 5,
            numberOfEpisodes = 62,
            cast = listOf("Bryan Cranston", "Aaron Paul", "Anna Gunn", "Giancarlo Esposito")
        ),
        MediaItem(
            id = "t_1399",
            tmdbId = 1399,
            title = "Game of Thrones",
            type = "Series",
            year = 2011,
            duration = "8 Seasons • 73 Episodes",
            rating = 8.4,
            popularity = 98.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Drama", "Fantasy", "Action"),
            platforms = listOf("Disney+ Hotstar", "Apple TV"),
            synopsis = "Seven noble families fight for control of the mythical land of Westeros. Friction between the houses leads to full-scale war. All while a very ancient evil awakens in the farthest north.",
            poster = "https://image.tmdb.org/t/p/w500/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
            numberOfSeasons = 8,
            numberOfEpisodes = 73,
            cast = listOf("Peter Dinklage", "Lena Headey", "Emilia Clarke", "Kit Harington")
        ),
        MediaItem(
            id = "t_66732",
            tmdbId = 66732,
            title = "Stranger Things",
            type = "Series",
            year = 2016,
            duration = "4 Seasons • 34 Episodes",
            rating = 8.6,
            popularity = 94.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Sci-Fi", "Drama", "Mystery"),
            platforms = listOf("Netflix"),
            synopsis = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
            poster = "https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
            numberOfSeasons = 4,
            numberOfEpisodes = 34,
            cast = listOf("Millie Bobby Brown", "Finn Wolfhard", "Winona Ryder", "David Harbour")
        ),
        MediaItem(
            id = "m_496243",
            tmdbId = 496243,
            title = "Parasite",
            type = "Movie",
            year = 2019,
            duration = "2h 12m",
            rating = 8.5,
            popularity = 88.0,
            trending = true,
            originalLanguage = "ko",
            country = "South Korea",
            genres = listOf("Comedy", "Thriller", "Drama"),
            platforms = listOf("Prime Video", "Apple TV"),
            synopsis = "All unemployed, Ki-taek's family takes peculiar interest in the wealthy and glamorous Parks for their livelihood until they get entangled in an unexpected incident.",
            poster = "https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
            director = "Bong Joon-ho",
            cast = listOf("Song Kang-ho", "Lee Sun-kyun", "Cho Yeo-jeong", "Choi Woo-shik")
        ),
        MediaItem(
            id = "m_569094",
            tmdbId = 569094,
            title = "Spider-Man: Across the Spider-Verse",
            type = "Movie",
            year = 2023,
            duration = "2h 20m",
            rating = 8.4,
            popularity = 91.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Animation", "Action", "Adventure", "Sci-Fi"),
            platforms = listOf("Netflix", "Prime Video"),
            synopsis = "After reuniting with Gwen Stacy, Brooklyn's full-time, friendly neighborhood Spider-Man is catapulted across the Multiverse.",
            poster = "https://image.tmdb.org/t/p/w500/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
            director = "Joaquim Dos Santos",
            cast = listOf("Shameik Moore", "Hailee Steinfeld", "Oscar Isaac", "Daniel Kaluuya")
        ),
        MediaItem(
            id = "m_550",
            tmdbId = 550,
            title = "Fight Club",
            type = "Movie",
            year = 1999,
            duration = "2h 19m",
            rating = 8.4,
            popularity = 86.0,
            trending = false,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Drama", "Thriller"),
            platforms = listOf("Prime Video", "Apple TV"),
            synopsis = "A ticking-time-bomb insomniac and a slippery soap salesman channel primal male aggression into a shocking new form of therapy.",
            poster = "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            director = "David Fincher",
            cast = listOf("Brad Pitt", "Edward Norton", "Helena Bonham Carter")
        ),
        MediaItem(
            id = "m_680",
            tmdbId = 680,
            title = "Pulp Fiction",
            type = "Movie",
            year = 1994,
            duration = "2h 34m",
            rating = 8.5,
            popularity = 88.0,
            trending = false,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Thriller", "Crime"),
            platforms = listOf("Prime Video", "Apple TV"),
            synopsis = "A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in four tales of violence and redemption.",
            poster = "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
            director = "Quentin Tarantino",
            cast = listOf("John Travolta", "Samuel L. Jackson", "Uma Thurman", "Bruce Willis")
        ),
        MediaItem(
            id = "t_76331",
            tmdbId = 76331,
            title = "Succession",
            type = "Series",
            year = 2018,
            duration = "4 Seasons • 39 Episodes",
            rating = 8.5,
            popularity = 92.0,
            trending = true,
            originalLanguage = "en",
            country = "United States",
            genres = listOf("Drama"),
            platforms = listOf("Disney+ Hotstar"),
            synopsis = "The Roy family is known for controlling the biggest media and entertainment company in the world. However, their world changes when their aging father steps down from the company.",
            poster = "https://image.tmdb.org/t/p/w500/7udW4F6egq9zN1r1GgWzZ4eX987.jpg",
            numberOfSeasons = 4,
            numberOfEpisodes = 39,
            cast = listOf("Brian Cox", "Jeremy Strong", "Sarah Snook", "Kieran Culkin")
        )
    )

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

            // Combine remote with local, prioritizing prefix matches on the query
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
