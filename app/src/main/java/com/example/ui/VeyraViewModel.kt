package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEffectsManager
import com.example.data.db.VeyraDatabase
import com.example.data.model.DiscoveryConstants
import com.example.data.model.MediaItem
import com.example.data.repository.CatalogRepository
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    WELCOME,
    SEARCH,
    WATCHLIST,
    HISTORY,
    WARP,
    RESULT
}

enum class WatchlistCategory {
    ALL,
    MOVIE,
    SERIES
}

enum class LibraryView {
    WATCHLIST,
    ALREADY_SEEN
}

enum class HistoryFilter {
    ALL,
    SEEN,
    NOT_INTERESTED,
    SAVED
}

class VeyraViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("veyra_prefs", Context.MODE_PRIVATE)
    private val database = VeyraDatabase.getInstance(application)
    private val movieRepository = MovieRepository(database.movieDao())
    private val catalogRepository = CatalogRepository()
    val audioManager = AudioEffectsManager()

    // Navigation & Screen state
    private val _currentScreen = MutableStateFlow(AppScreen.WELCOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Onboarding & Intro states
    private val _isOnboardingActive = MutableStateFlow(!prefs.getBoolean("onboarded", false))
    val isOnboardingActive: StateFlow<Boolean> = _isOnboardingActive.asStateFlow()

    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    private val _isBrandIntroActive = MutableStateFlow(false)
    val isBrandIntroActive: StateFlow<Boolean> = _isBrandIntroActive.asStateFlow()

    private val _brandIntroPhrase = MutableStateFlow(DiscoveryConstants.BRAND_PHRASES.first())
    val brandIntroPhrase: StateFlow<String> = _brandIntroPhrase.asStateFlow()

    // Filter states
    private val _selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    val selectedGenres: StateFlow<Set<String>> = _selectedGenres.asStateFlow()

    private val _selectedEra = MutableStateFlow("Any Era")
    val selectedEra: StateFlow<String> = _selectedEra.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlatforms: StateFlow<Set<String>> = _selectedPlatforms.asStateFlow()

    private val _selectedPickMode = MutableStateFlow("🎲 Random")
    val selectedPickMode: StateFlow<String> = _selectedPickMode.asStateFlow()

    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood: StateFlow<String?> = _selectedMood.asStateFlow()

    private val _currentTypeFilter = MutableStateFlow("all")
    val currentTypeFilter: StateFlow<String> = _currentTypeFilter.asStateFlow()

    private val _selectedRegion = MutableStateFlow("IN")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("ANY")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Modals
    private val _showLanguageModal = MutableStateFlow(false)
    val showLanguageModal: StateFlow<Boolean> = _showLanguageModal.asStateFlow()

    private val _showRegionModal = MutableStateFlow(false)
    val showRegionModal: StateFlow<Boolean> = _showRegionModal.asStateFlow()

    private val _showWatchlistModal = MutableStateFlow(false)
    val showWatchlistModal: StateFlow<Boolean> = _showWatchlistModal.asStateFlow()

    private val _showHistoryModal = MutableStateFlow(false)
    val showHistoryModal: StateFlow<Boolean> = _showHistoryModal.asStateFlow()

    private val _showWhyThisModal = MutableStateFlow(false)
    val showWhyThisModal: StateFlow<Boolean> = _showWhyThisModal.asStateFlow()

    private val _showNoResultsModal = MutableStateFlow(false)
    val showNoResultsModal: StateFlow<Boolean> = _showNoResultsModal.asStateFlow()

    private val _watchlistCategory = MutableStateFlow(WatchlistCategory.ALL)
    val watchlistCategory: StateFlow<WatchlistCategory> = _watchlistCategory.asStateFlow()

    // Sound FX
    private val _sfxEnabled = MutableStateFlow(prefs.getBoolean("sfx_enabled", true))
    val sfxEnabled: StateFlow<Boolean> = _sfxEnabled.asStateFlow()

    // Warp & Result state
    private val _currentItem = MutableStateFlow<MediaItem?>(null)
    val currentItem: StateFlow<MediaItem?> = _currentItem.asStateFlow()

    private val _isItemSaved = MutableStateFlow(false)
    val isItemSaved: StateFlow<Boolean> = _isItemSaved.asStateFlow()

    private val _isItemSeen = MutableStateFlow(false)
    val isItemSeen: StateFlow<Boolean> = _isItemSeen.asStateFlow()

    private val _isItemNotInterested = MutableStateFlow(false)
    val isItemNotInterested: StateFlow<Boolean> = _isItemNotInterested.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    private val _warpCandidatePosters = MutableStateFlow<List<String>>(emptyList())
    val warpCandidatePosters: StateFlow<List<String>> = _warpCandidatePosters.asStateFlow()

    private val _warpStatusText = MutableStateFlow("Searching Live Catalog...")
    val warpStatusText: StateFlow<String> = _warpStatusText.asStateFlow()

    private val _warpWinnerItem = MutableStateFlow<MediaItem?>(null)
    val warpWinnerItem: StateFlow<MediaItem?> = _warpWinnerItem.asStateFlow()

    private val _isWarpTargetLocked = MutableStateFlow(false)
    val isWarpTargetLocked: StateFlow<Boolean> = _isWarpTargetLocked.asStateFlow()

    // 3D Motion Carousel items (curated multi-era showcase + live discovery refresh)
    private val _motionPosters = MutableStateFlow<List<MediaItem>>(
        catalogRepository.getShowcaseMedia()
    )
    val motionPosters: StateFlow<List<MediaItem>> = _motionPosters.asStateFlow()

    // Room flows
    val watchlist: StateFlow<List<MediaItem>> = movieRepository.watchlist.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<MediaItem>> = movieRepository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val seenList: StateFlow<List<MediaItem>> = movieRepository.seenList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _libraryView = MutableStateFlow(LibraryView.WATCHLIST)
    val libraryView: StateFlow<LibraryView> = _libraryView.asStateFlow()

    fun setLibraryView(view: LibraryView) {
        _libraryView.value = view
    }

    fun openAlreadySeenLibrary() {
        _libraryView.value = LibraryView.ALREADY_SEEN
        _currentScreen.value = AppScreen.WATCHLIST
        _showSettingsModal.value = false
    }

    // Session cache & Smart spin
    private val recentlyShownIds = ArrayDeque<String>()
    private val dismissedSessionIds = mutableSetOf<String>()
    private var warpJob: Job? = null

    init {
        if (!_isOnboardingActive.value) {
            playBrandIntro()
        }
        refreshShowcasePosters()
    }

    private fun refreshShowcasePosters() {
        viewModelScope.launch {
            try {
                val dynamicShowcase = catalogRepository.fetchShowcasePosters()
                if (dynamicShowcase.isNotEmpty()) {
                    _motionPosters.value = dynamicShowcase
                }
            } catch (_: Exception) {}
        }
    }

    private fun playBrandIntro() {
        viewModelScope.launch {
            _brandIntroPhrase.value = DiscoveryConstants.BRAND_PHRASES.random()
            _isBrandIntroActive.value = true
            delay(1600)
            _isBrandIntroActive.value = false
        }
    }

    // Onboarding controls
    fun nextOnboardingScene() {
        audioManager.playTactileClick(_sfxEnabled.value)
        if (_onboardingStep.value < 3) {
            _onboardingStep.value += 1
        } else {
            completeOnboarding()
        }
    }

    fun completeOnboarding() {
        audioManager.playTactileClick(_sfxEnabled.value)
        prefs.edit().putBoolean("onboarded", true).apply()
        _isOnboardingActive.value = false
    }

    // SFX toggle
    fun toggleAudioSFX() {
        val newVal = !_sfxEnabled.value
        _sfxEnabled.value = newVal
        prefs.edit().putBoolean("sfx_enabled", newVal).apply()
        if (newVal) {
            audioManager.playTactileClick(true)
        }
    }

    fun playClickSound() {
        audioManager.playTactileClick(_sfxEnabled.value)
    }

    // Filter updates
    fun toggleGenre(genre: String) {
        playClickSound()
        val current = _selectedGenres.value.toMutableSet()
        if (current.contains(genre)) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _selectedGenres.value = current
    }

    fun toggleAllGenres(selectAll: Boolean) {
        playClickSound()
        _selectedGenres.value = if (selectAll) DiscoveryConstants.ALL_GENRES.toSet() else emptySet()
    }

    fun setEraFilter(era: String) {
        playClickSound()
        if (_selectedEra.value == era) {
            _selectedEra.value = "Any Era"
        } else {
            _selectedEra.value = era
        }
    }

    fun togglePlatform(platform: String) {
        playClickSound()
        val current = _selectedPlatforms.value.toMutableSet()
        if (current.contains(platform)) {
            current.remove(platform)
        } else {
            current.add(platform)
        }
        _selectedPlatforms.value = current
    }

    fun toggleAllPlatforms(selectAll: Boolean) {
        playClickSound()
        _selectedPlatforms.value = if (selectAll) DiscoveryConstants.ALL_PLATFORMS.toSet() else emptySet()
    }

    fun setPickMode(mode: String) {
        playClickSound()
        _selectedPickMode.value = mode
    }

    fun setMood(moodId: String) {
        playClickSound()
        val mood = DiscoveryConstants.MOODS.find { it.id == moodId } ?: return
        if (_selectedMood.value == moodId) {
            clearMood()
            return
        }
        _selectedMood.value = moodId
        _selectedGenres.value = mood.genres.toSet()
        _selectedPickMode.value = mood.pickMode
    }

    fun clearMood() {
        playClickSound()
        _selectedMood.value = null
        _selectedGenres.value = emptySet()
    }

    fun setTypeFilter(type: String) {
        playClickSound()
        _currentTypeFilter.value = type
    }

    fun selectLanguage(code: String) {
        playClickSound()
        _selectedLanguage.value = code
        _showLanguageModal.value = false
    }

    fun selectRegion(code: String) {
        playClickSound()
        _selectedRegion.value = code
        _showRegionModal.value = false
    }

    // Modals
    fun openLanguageModal() { playClickSound(); _showLanguageModal.value = true }
    fun closeLanguageModal() { playClickSound(); _showLanguageModal.value = false }

    fun openRegionModal() { playClickSound(); _showRegionModal.value = true }
    fun closeRegionModal() { playClickSound(); _showRegionModal.value = false }

    fun openWatchlistModal() { playClickSound(); _showWatchlistModal.value = true }
    fun closeWatchlistModal() { playClickSound(); _showWatchlistModal.value = false }

    fun openHistoryModal() { playClickSound(); _showHistoryModal.value = true }
    fun closeHistoryModal() { playClickSound(); _showHistoryModal.value = false }

    fun openWhyThisModal() { playClickSound(); _showWhyThisModal.value = true }
    fun closeWhyThisModal() { playClickSound(); _showWhyThisModal.value = false }

    fun dismissNoResultsModal() { playClickSound(); _showNoResultsModal.value = false }

    fun setWatchlistCategory(cat: WatchlistCategory) {
        playClickSound()
        _watchlistCategory.value = cat
    }

    // Navigation
    fun switchScreen(screen: AppScreen) {
        playClickSound()
        _currentScreen.value = screen
    }

    // Surprise Me
    fun triggerSurpriseMe() {
        playClickSound()
        _selectedGenres.value = emptySet()
        _selectedEra.value = "Any Era"
        _selectedPlatforms.value = emptySet()
        _selectedPickMode.value = "🎲 Random"
        _selectedMood.value = null
        _currentTypeFilter.value = "all"
        _showNoResultsModal.value = false
        startCinematicWarpSearch()
    }

    // Relax filters & Spin
    fun relaxFiltersAndSpin() {
        playClickSound()
        _showNoResultsModal.value = false
        _selectedGenres.value = emptySet()
        _selectedEra.value = "Any Era"
        _selectedPlatforms.value = emptySet()
        startCinematicWarpSearch()
    }

    // Main cinematic warp search logic
    fun startCinematicWarpSearch() {
        warpJob?.cancel()
        warpJob = viewModelScope.launch {
            audioManager.playHyperspaceSequence(_sfxEnabled.value)

            // Switch to warp screen with initial parallax film-reel candidate pool
            _currentScreen.value = AppScreen.WARP
            _isWarpTargetLocked.value = false
            _warpStatusText.value = "Searching Live Catalog..."
            val initialSeed = DiscoveryConstants.FALLBACK_POSTERS.shuffled()
            _warpCandidatePosters.value = initialSeed
            _warpWinnerItem.value = null

            // Fetch catalog across all cinema eras
            val pool = catalogRepository.fetchCatalog(
                typeFilter = _currentTypeFilter.value,
                region = _selectedRegion.value,
                language = _selectedLanguage.value,
                selectedPlatforms = _selectedPlatforms.value,
                selectedEra = _selectedEra.value
            )

            val excludedIds = movieRepository.getExcludedIds()

            // Filter catalog: empty set matches all, selected set filters strictly
            val filtered = pool.filter { item ->
                if (dismissedSessionIds.contains(item.id)) return@filter false
                if (excludedIds.contains(item.id)) return@filter false
                val matchesGenre = _selectedGenres.value.isEmpty() || item.genres.any { _selectedGenres.value.contains(it) }
                val itemEra = DiscoveryConstants.getEraCategory(item.year)
                val matchesEra = _selectedEra.value == "Any Era" || itemEra == _selectedEra.value
                val matchesPlatform = _selectedPlatforms.value.isEmpty() || item.platforms.any { _selectedPlatforms.value.contains(it) }
                matchesGenre && matchesEra && matchesPlatform
            }

            if (filtered.isEmpty()) {
                _currentScreen.value = AppScreen.WELCOME
                _showNoResultsModal.value = true
                return@launch
            }

            // Exclude last 15 shown
            var available = filtered.filter { !recentlyShownIds.contains(it.id) }
            if (available.isEmpty()) {
                recentlyShownIds.clear()
                available = filtered
            }

            // Apply pick mode sorting / filtering
            var sorted = available.toMutableList()
            when (_selectedPickMode.value) {
                "🎲 Random" -> sorted.shuffle()
                "🔥 Trending" -> sorted.sortByDescending { it.popularity }
                "⭐ Top Rated" -> sorted.sortByDescending { it.rating }
                "❤️ Popular" -> sorted.sortByDescending { it.popularity }
                "💎 Hidden Gems" -> {
                    val gems = sorted.filter { it.rating >= 7.3 && it.popularity < 65 }
                    if (gems.isNotEmpty()) sorted = gems.toMutableList()
                }
                "✨ Underrated" -> {
                    val under = sorted.filter { it.rating >= 7.0 && it.popularity < 55 }
                    if (under.isNotEmpty()) sorted = under.toMutableList()
                }
                "🏆 Critically Acclaimed" -> {
                    val acclaimed = sorted.filter { it.rating >= 8.0 }
                    if (acclaimed.isNotEmpty()) sorted = acclaimed.toMutableList()
                }
                "🎬 Cult Favorites" -> {
                    val cult = sorted.filter { it.rating >= 7.5 && (it.year < 2015 || it.popularity < 75) }
                    if (cult.isNotEmpty()) sorted = cult.toMutableList()
                }
                "⚡ New Releases" -> {
                    val newRels = sorted.filter { it.year >= 2023 }
                    if (newRels.isNotEmpty()) sorted = newRels.toMutableList()
                }
                "🏛️ Classics" -> {
                    val classics = sorted.filter { it.year < 2000 }
                    if (classics.isNotEmpty()) sorted = classics.toMutableList()
                }
                "⏳ Short & Sweet" -> {
                    val shorts = sorted.filter { it.type == "Movie" }
                    if (shorts.isNotEmpty()) sorted = shorts.toMutableList()
                }
            }

            // Strict format adherence & balanced movie/series distribution
            val candidatePool: List<MediaItem> = when (_currentTypeFilter.value) {
                "Movie" -> sorted.filter { it.type == "Movie" }.ifEmpty { sorted }
                "Series" -> sorted.filter { it.type == "Series" }.ifEmpty { sorted }
                else -> {
                    // "all" -> strong movie presence (82% movies, 18% series), preventing TV series domination
                    val movies = sorted.filter { it.type == "Movie" }
                    val series = sorted.filter { it.type == "Series" }
                    if (movies.isNotEmpty() && series.isNotEmpty()) {
                        if (Random.nextFloat() < 0.82f) movies else series
                    } else if (movies.isNotEmpty()) {
                        movies
                    } else {
                        sorted
                    }
                }
            }

            val topSlice = candidatePool.take(maxOf(6, candidatePool.size / 2))
            val winner = topSlice[Random.nextInt(topSlice.size)]

            // Record smart spin queue
            recentlyShownIds.addLast(winner.id)
            if (recentlyShownIds.size > 15) {
                recentlyShownIds.removeFirst()
            }

            // Phase 1: Filtering candidates
            delay(400)
            _warpStatusText.value = "Filtering Candidates..."
            val candidatePosters = (filtered.map { it.poster } + DiscoveryConstants.FALLBACK_POSTERS).filter { it.isNotBlank() }.distinct().shuffled().take(24)
            _warpCandidatePosters.value = candidatePosters

            // Fetch detailed credits/metadata concurrently during warp
            val enrichedWinner = catalogRepository.fetchDetailedMetadata(winner, region = _selectedRegion.value)

            // Phase 2: Target locked
            delay(1200)
            _warpStatusText.value = "Target Locked!"
            _warpWinnerItem.value = enrichedWinner
            _isWarpTargetLocked.value = true

            // Save to discovery history
            movieRepository.recordDiscovery(enrichedWinner)

            // Phase 3: Transition to Result screen
            delay(700)
            _currentItem.value = enrichedWinner
            _isItemSaved.value = movieRepository.isSaved(enrichedWinner.id)
            _isItemSeen.value = movieRepository.isSeen(enrichedWinner.id)
            _isItemNotInterested.value = movieRepository.isNotInterested(enrichedWinner.id)
            _currentScreen.value = AppScreen.RESULT
        }
    }

    // Dismiss current item ("Not for me")
    fun dismissCurrentItem() {
        playClickSound()
        val current = _currentItem.value ?: return
        dismissedSessionIds.add(current.id)
        startCinematicWarpSearch()
    }

    // Toggle watchlist for current item
    fun toggleWatchlistCurrent() {
        playClickSound()
        val current = _currentItem.value ?: return
        viewModelScope.launch {
            if (_isItemSaved.value) {
                movieRepository.removeFromWatchlist(current.id)
                _isItemSaved.value = false
            } else {
                movieRepository.addToWatchlist(current)
                _isItemSaved.value = true
            }
        }
    }

    fun removeFromWatchlist(id: String) {
        playClickSound()
        viewModelScope.launch {
            movieRepository.removeFromWatchlist(id)
            if (_currentItem.value?.id == id) {
                _isItemSaved.value = false
            }
        }
    }

    fun clearHistory() {
        playClickSound()
        viewModelScope.launch {
            movieRepository.clearHistory()
        }
    }

    fun loadFromWatchlist(item: MediaItem) {
        playClickSound()
        viewModelScope.launch {
            val enriched = catalogRepository.fetchDetailedMetadata(item)
            _currentItem.value = enriched
            _isItemSaved.value = true
            _isItemSeen.value = movieRepository.isSeen(enriched.id)
            _showWatchlistModal.value = false
            _currentScreen.value = AppScreen.RESULT
        }
    }

    fun loadFromHistory(item: MediaItem) {
        playClickSound()
        viewModelScope.launch {
            val enriched = catalogRepository.fetchDetailedMetadata(item)
            _currentItem.value = enriched
            _isItemSaved.value = movieRepository.isSaved(enriched.id)
            _showHistoryModal.value = false
            _currentScreen.value = AppScreen.RESULT
        }
    }

    fun pickFromWatchlistRandom() {
        playClickSound()
        val list = watchlist.value
        if (list.isEmpty()) {
            _showNoResultsModal.value = true
            return
        }
        val randomItem = list[Random.nextInt(list.size)]
        viewModelScope.launch {
            val enriched = catalogRepository.fetchDetailedMetadata(randomItem)
            _currentItem.value = enriched
            _isItemSaved.value = true
            _isItemSeen.value = movieRepository.isSeen(enriched.id)
            _showWatchlistModal.value = false
            _currentScreen.value = AppScreen.RESULT
        }
    }

    // Search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MediaItem>>(emptyList())
    val searchResults: StateFlow<List<MediaItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            searchJob?.cancel()
            return
        }

        // 1. INSTANT RESULTS: Zero-millisecond response on first keystroke from local media pool
        val instantMatches = catalogRepository.searchLocal(trimmed)
        if (instantMatches.isNotEmpty()) {
            _searchResults.value = instantMatches
        }

        // 2. LIVE TMDB API REFINEMENT: Short 120ms debounce to avoid spamming the API while typing fast
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(120)
            _isSearching.value = true
            try {
                val liveResults = catalogRepository.searchCatalog(trimmed, selectedRegion.value)
                if (liveResults.isNotEmpty()) {
                    _searchResults.value = liveResults
                } else if (instantMatches.isNotEmpty()) {
                    _searchResults.value = instantMatches
                }
            } catch (_: Exception) {
                if (instantMatches.isNotEmpty()) {
                    _searchResults.value = instantMatches
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
        searchJob?.cancel()
    }

    // Settings Modal
    private val _showSettingsModal = MutableStateFlow(false)
    val showSettingsModal: StateFlow<Boolean> = _showSettingsModal.asStateFlow()

    private val _hapticFeedbackEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

    fun openSettingsModal() {
        playClickSound()
        _showSettingsModal.value = true
    }

    fun closeSettingsModal() {
        _showSettingsModal.value = false
    }

    fun toggleHapticFeedback() {
        val newVal = !_hapticFeedbackEnabled.value
        _hapticFeedbackEnabled.value = newVal
        prefs.edit().putBoolean("haptic_enabled", newVal).apply()
    }



    fun loadItemDetails(item: MediaItem) {
        playClickSound()
        viewModelScope.launch {
            val enriched = catalogRepository.fetchDetailedMetadata(item)
            _currentItem.value = enriched
            _isItemSaved.value = movieRepository.isSaved(enriched.id)
            _isItemSeen.value = movieRepository.isSeen(enriched.id)
            _currentScreen.value = AppScreen.RESULT
        }
    }
    fun markCurrentAsSeen() {
        val current = _currentItem.value ?: return
        playClickSound()
        viewModelScope.launch {
            movieRepository.markAsSeen(current)
            _isItemSeen.value = true
            _isItemSaved.value = false
            _isItemNotInterested.value = false
            startCinematicWarpSearch()
        }
    }

    fun markCurrentAsNotInterested() {
        val current = _currentItem.value ?: return
        playClickSound()
        viewModelScope.launch {
            movieRepository.markAsNotInterested(current)
            _isItemNotInterested.value = true
            _isItemSaved.value = false
            _isItemSeen.value = false
            startCinematicWarpSearch()
        }
    }

    fun markItemAsSeen(item: MediaItem) {
        playClickSound()
        viewModelScope.launch {
            movieRepository.markAsSeen(item)
            if (_currentItem.value?.id == item.id) {
                _isItemSeen.value = true
            }
        }
    }

    fun removeFromSeen(id: String) {
        playClickSound()
        viewModelScope.launch {
            movieRepository.removeFromSeen(id)
            if (_currentItem.value?.id == id) {
                _isItemSeen.value = false
            }
        }
    }
}

class VeyraViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VeyraViewModel::class.java)) {
            return VeyraViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
