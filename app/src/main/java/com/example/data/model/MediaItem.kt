package com.example.data.model

data class MediaItem(
    val id: String,
    val tmdbId: Int,
    val title: String,
    val type: String, // "Movie" or "Series"
    val year: Int,
    val duration: String,
    val rating: Double,
    val popularity: Double,
    val trending: Boolean,
    val originalLanguage: String,
    val country: String? = null,
    val genres: List<String>,
    val platforms: List<String>,
    val watchProviders: List<WatchProvider> = emptyList(),
    val watchLink: String? = null,
    val synopsis: String,
    val poster: String,
    val director: String? = null,
    val cast: List<String> = emptyList(),
    val numberOfSeasons: Int = 1,
    val numberOfEpisodes: Int = 8,
    val seasons: List<SeasonInfo> = emptyList(),
    val isSavedToWatchlist: Boolean = false,
    val status: String = "discovered",
    val timestamp: Long = System.currentTimeMillis()
)

data class WatchProvider(
    val id: Int,
    val name: String,
    val logoUrl: String? = null,
    val type: String = "Stream", // "Stream", "Free", "Rent", "Buy"
    val link: String? = null
)

data class SeasonInfo(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val overview: String?
)

data class MoodOption(
    val id: String,
    val label: String,
    val genres: List<String>,
    val pickMode: String
)

data class LanguageOption(
    val code: String,
    val label: String
)

data class RegionOption(
    val code: String,
    val label: String
)

object DiscoveryConstants {
    val TMDB_GENRES = mapOf(
        28 to "Action", 12 to "Adventure", 16 to "Animation", 35 to "Comedy", 80 to "Crime",
        99 to "Documentary", 18 to "Drama", 10751 to "Family", 14 to "Fantasy", 36 to "History",
        27 to "Horror", 10402 to "Music", 9648 to "Mystery", 10749 to "Romance", 878 to "Sci-Fi",
        53 to "Thriller", 10752 to "War", 37 to "Western", 10759 to "Action", 10762 to "Family", 10765 to "Sci-Fi"
    )

    val ALL_GENRES = listOf(
        "Action", "Adventure", "Sci-Fi", "Horror", "Comedy", "Drama",
        "Thriller", "Mystery", "Fantasy", "Animation", "Crime", "Romance", "Family"
    )

    val ALL_ERAS = listOf(
        "Any Era", "2020s", "2010s", "2000s", "1990s", "1980s", "1970s", "Before 1970"
    )

    val ALL_PLATFORMS = listOf(
        "Netflix", "Prime Video", "JioHotstar", "Apple TV", "YouTube"
    )

    val PLATFORM_KEYWORD_MAP = mapOf(
        "Netflix" to listOf(8),
        "Prime Video" to listOf(119, 9),
        "JioHotstar" to listOf(2336, 122, 337),
        "Disney+ Hotstar" to listOf(2336, 122, 337),
        "Apple TV" to listOf(350, 2),
        "YouTube" to listOf(192)
    )

    fun getPlatformProviderIds(platforms: Set<String>): List<Int> {
        val ids = mutableListOf<Int>()
        platforms.forEach { p ->
            PLATFORM_KEYWORD_MAP[p]?.let { ids.addAll(it) }
        }
        return ids.distinct()
    }

    val PICK_MODES = listOf(
        "🎲 Random", "🔥 Trending", "⭐ Top Rated", "❤️ Popular",
        "💎 Hidden Gems", "✨ Underrated", "🏆 Critically Acclaimed",
        "🎬 Cult Favorites", "⚡ New Releases", "🏛️ Classics", "⏳ Short & Sweet"
    )

    val MOODS = listOf(
        MoodOption("comfort", "Something comforting", listOf("Comedy", "Family", "Animation"), "❤️ Popular"),
        MoodOption("laugh", "Make me laugh", listOf("Comedy"), "❤️ Popular"),
        MoodOption("think", "Make me think", listOf("Sci-Fi", "Mystery", "Drama"), "⭐ Top Rated"),
        MoodOption("emotional", "Something emotional", listOf("Drama", "Romance"), "⭐ Top Rated"),
        MoodOption("intense", "Something intense", listOf("Thriller", "Crime", "Action"), "🔥 Trending"),
        MoodOption("scare", "Scare me", listOf("Horror"), "🎲 Random"),
        MoodOption("mind", "Blow my mind", listOf("Sci-Fi", "Mystery", "Thriller"), "⭐ Top Rated"),
        MoodOption("latenight", "Late-night watch", listOf("Thriller", "Horror", "Crime"), "🔥 Trending"),
        MoodOption("relax", "Something relaxing", listOf("Documentary", "Family", "Music"), "🎲 Random")
    )

    val LANGUAGES = listOf(
        LanguageOption("ANY", "Any Language"),
        LanguageOption("en", "English"),
        LanguageOption("hi", "Hindi"),
        LanguageOption("te", "Telugu"),
        LanguageOption("ta", "Tamil"),
        LanguageOption("ml", "Malayalam"),
        LanguageOption("kn", "Kannada"),
        LanguageOption("ko", "Korean"),
        LanguageOption("ja", "Japanese"),
        LanguageOption("es", "Spanish"),
        LanguageOption("fr", "French")
    )

    val REGIONS = listOf(
        RegionOption("US", "United States (US)"),
        RegionOption("IN", "India (IN)"),
        RegionOption("GB", "United Kingdom (GB)"),
        RegionOption("CA", "Canada (CA)"),
        RegionOption("AU", "Australia (AU)"),
        RegionOption("JP", "Japan (JP)"),
        RegionOption("KR", "South Korea (KR)"),
        RegionOption("FR", "France (FR)"),
        RegionOption("ES", "Spain (ES)")
    )

    val BRAND_PHRASES = listOf(
        "What will you discover today?",
        "Your next story is out there.",
        "Something worth watching is waiting.",
        "Where do we go tonight?",
        "Let the universe choose."
    )

    val FALLBACK_POSTERS = listOf(
        "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
        "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
        "https://image.tmdb.org/t/p/w500/rSPw7MQFij5IH9XQoHO2EtgWn68.jpg",
        "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
        "https://image.tmdb.org/t/p/w500/uxzzxijgPIY7slzFvMpv8wjKC5l.jpg",
        "https://image.tmdb.org/t/p/w500/wKiOkZTN9lUUUNZLmtnwubZYONg.jpg",
        "https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
        "https://image.tmdb.org/t/p/w500/6oom5QYQ2yQTMJIbnvbkBL9cHo6.jpg",
        "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg"
    )

    fun getEraCategory(year: Int): String {
        return when {
            year >= 2020 -> "2020s"
            year >= 2010 -> "2010s"
            year >= 2000 -> "2000s"
            year >= 1990 -> "1990s"
            year >= 1980 -> "1980s"
            year >= 1970 -> "1970s"
            else -> "Before 1970"
        }
    }
}
