package com.example

import android.os.Bundle
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.VeyraViewModel
import com.example.ui.VeyraViewModelFactory
import com.example.ui.components.FloatingBottomBar
import com.example.ui.components.topFadingEdge
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.example.ui.components.HeaderBar
import com.example.ui.components.LanguageModal
import com.example.ui.components.NoResultsModal
import com.example.ui.components.RegionModal
import com.example.ui.components.SettingsModal
import com.example.ui.components.WhyThisModal
import com.example.ui.screens.BrandIntroOverlay
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.OnboardingOverlay
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.WarpScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VeyraBlack

class MainActivity : ComponentActivity() {

    private val viewModel: VeyraViewModel by viewModels {
        VeyraViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable hardware acceleration explicitly on window
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // High refresh rate (90Hz / 120Hz when device supports it)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val maxRate = display?.supportedModes?.maxOfOrNull { it.refreshRate } ?: 0f
                if (maxRate > 60f) {
                    window.attributes = window.attributes.apply {
                        preferredRefreshRate = maxRate
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val maxMode = windowManager.defaultDisplay.supportedModes.maxByOrNull { it.refreshRate }
                if (maxMode != null && maxMode.refreshRate > 60f) {
                    window.attributes = window.attributes.apply {
                        preferredDisplayModeId = maxMode.modeId
                    }
                }
            }
        } catch (_: Exception) {}

        // Optimized Coil ImageLoader: in-memory bitmap cache + disk cache
        try {
            val imageLoader = ImageLoader.Builder(applicationContext)
                .memoryCache {
                    MemoryCache.Builder(applicationContext)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizePercent(0.08)
                        .build()
                }
                .crossfade(true)
                .respectCacheHeaders(false)
                .build()
            Coil.setImageLoader(imageLoader)
        } catch (_: Exception) {}

        setContent {
            MyApplicationTheme {
                VeyraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VeyraApp(viewModel: VeyraViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isOnboardingActive by viewModel.isOnboardingActive.collectAsStateWithLifecycle()
    val onboardingStep by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val isBrandIntroActive by viewModel.isBrandIntroActive.collectAsStateWithLifecycle()
    val brandIntroPhrase by viewModel.brandIntroPhrase.collectAsStateWithLifecycle()

    // Filters
    val selectedGenres by viewModel.selectedGenres.collectAsStateWithLifecycle()
    val selectedEra by viewModel.selectedEra.collectAsStateWithLifecycle()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsStateWithLifecycle()
    val selectedPickMode by viewModel.selectedPickMode.collectAsStateWithLifecycle()
    val selectedMood by viewModel.selectedMood.collectAsStateWithLifecycle()
    val currentTypeFilter by viewModel.currentTypeFilter.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegion.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    // Search
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val motionPosters by viewModel.motionPosters.collectAsStateWithLifecycle()

    // Modals & Settings
    val showSettingsModal by viewModel.showSettingsModal.collectAsStateWithLifecycle()
    val showLanguageModal by viewModel.showLanguageModal.collectAsStateWithLifecycle()
    val showRegionModal by viewModel.showRegionModal.collectAsStateWithLifecycle()
    val showWhyThisModal by viewModel.showWhyThisModal.collectAsStateWithLifecycle()
    val showNoResultsModal by viewModel.showNoResultsModal.collectAsStateWithLifecycle()
    val watchlistCategory by viewModel.watchlistCategory.collectAsStateWithLifecycle()

    // Sound FX & Haptic
    val sfxEnabled by viewModel.sfxEnabled.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()

    // Results & Warp
    val currentItem by viewModel.currentItem.collectAsStateWithLifecycle()
    val isItemSaved by viewModel.isItemSaved.collectAsStateWithLifecycle()
    val isItemSeen by viewModel.isItemSeen.collectAsStateWithLifecycle()
    val isItemNotInterested by viewModel.isItemNotInterested.collectAsStateWithLifecycle()
    val seenList by viewModel.seenList.collectAsStateWithLifecycle()
    val libraryView by viewModel.libraryView.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val warpCandidatePosters by viewModel.warpCandidatePosters.collectAsStateWithLifecycle()
    val warpStatusText by viewModel.warpStatusText.collectAsStateWithLifecycle()
    val warpWinnerItem by viewModel.warpWinnerItem.collectAsStateWithLifecycle()
    val isWarpTargetLocked by viewModel.isWarpTargetLocked.collectAsStateWithLifecycle()

    // Room
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    // Hardware Back Handling
    BackHandler(enabled = showSettingsModal || showLanguageModal || showRegionModal ||
            showWhyThisModal || showNoResultsModal || currentScreen != AppScreen.WELCOME) {
        when {
            showSettingsModal -> viewModel.closeSettingsModal()
            showLanguageModal -> viewModel.closeLanguageModal()
            showRegionModal -> viewModel.closeRegionModal()
            showWhyThisModal -> viewModel.closeWhyThisModal()
            showNoResultsModal -> viewModel.dismissNoResultsModal()
            currentScreen != AppScreen.WELCOME -> viewModel.switchScreen(AppScreen.WELCOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VeyraBlack)
    ) {
        val isFullscreenDetailOrWarp = currentScreen == AppScreen.WARP || currentScreen == AppScreen.RESULT
        val hazeState = remember { HazeState() }

        // Main Screen Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                label = "screen_crossfade",
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .topFadingEdge(fadeHeight = 52.dp)
            ) { screen ->
            when (screen) {
                AppScreen.WELCOME -> {
                    WelcomeScreen(
                        selectedGenres = selectedGenres,
                        selectedEra = selectedEra,
                        selectedPlatforms = selectedPlatforms,
                        selectedPickMode = selectedPickMode,
                        selectedMood = selectedMood,
                        currentTypeFilter = currentTypeFilter,
                        selectedRegion = selectedRegion,
                        selectedLanguage = selectedLanguage,
                        onToggleGenre = { viewModel.toggleGenre(it) },
                        onToggleAllGenres = { viewModel.toggleAllGenres(it) },
                        onSetEra = { viewModel.setEraFilter(it) },
                        onTogglePlatform = { viewModel.togglePlatform(it) },
                        onToggleAllPlatforms = { viewModel.toggleAllPlatforms(it) },
                        onSetPickMode = { viewModel.setPickMode(it) },
                        onSetMood = { viewModel.setMood(it) },
                        onClearMood = { viewModel.clearMood() },
                        onSetTypeFilter = { viewModel.setTypeFilter(it) },
                        onOpenRegionModal = { viewModel.openRegionModal() },
                        onOpenLanguageModal = { viewModel.openLanguageModal() },
                        onPickWatchlist = { viewModel.pickFromWatchlistRandom() },
                        onRandomizePick = { viewModel.startCinematicWarpSearch() },
                        onOpenSettings = { viewModel.openSettingsModal() },
                        motionPosters = motionPosters,
                        onSelectMedia = { viewModel.loadItemDetails(it) },
                        onPlayTick = { viewModel.audioManager.playTactileClick(sfxEnabled) }
                    )
                }

                AppScreen.SEARCH -> {
                    SearchScreen(
                        query = searchQuery,
                        results = searchResults,
                        isSearching = isSearching,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onClearQuery = { viewModel.clearSearch() },
                        onSelectMedia = { viewModel.loadItemDetails(it) },
                        onNavigateHome = { viewModel.switchScreen(AppScreen.WELCOME) },
                        onOpenSettings = { viewModel.openSettingsModal() }
                    )
                }

                AppScreen.WATCHLIST -> {
                    WatchlistScreen(
                        watchlist = watchlist,
                        seenList = seenList,
                        currentCategory = watchlistCategory,
                        currentLibraryView = libraryView,
                        onCategoryChanged = { viewModel.setWatchlistCategory(it) },
                        onLibraryViewChanged = { viewModel.setLibraryView(it) },
                        onViewItem = { viewModel.loadFromWatchlist(it) },
                        onRemoveItem = { viewModel.removeFromWatchlist(it) },
                        onRemoveFromSeen = { viewModel.removeFromSeen(it) },
                        onPickRandom = { viewModel.pickFromWatchlistRandom() },
                        onExplore = { viewModel.switchScreen(AppScreen.WELCOME) },
                        onNavigateHome = { viewModel.switchScreen(AppScreen.WELCOME) },
                        onOpenSettings = { viewModel.openSettingsModal() }
                    )
                }

                AppScreen.HISTORY -> {
                    HistoryScreen(
                        historyList = history,
                        currentFilter = historyFilter,
                        onFilterChanged = { viewModel.setHistoryFilter(it) },
                        onViewItem = { viewModel.loadFromHistory(it) },
                        onClearHistory = { viewModel.clearHistory() },
                        onNavigateHome = { viewModel.switchScreen(AppScreen.WELCOME) },
                        onOpenSettings = { viewModel.openSettingsModal() }
                    )
                }

                AppScreen.WARP -> {
                    WarpScreen(
                        statusText = warpStatusText,
                        candidatePosters = warpCandidatePosters,
                        winnerItem = warpWinnerItem,
                        isTargetLocked = isWarpTargetLocked
                    )
                }

                AppScreen.RESULT -> {
                    ResultScreen(
                        item = currentItem,
                        isSavedToWatchlist = isItemSaved,
                        isAlreadySeen = isItemSeen,
                        isNotInterested = isItemNotInterested,
                        onBackToFilters = { viewModel.switchScreen(AppScreen.WELCOME) },
                        onToggleWatchlist = { viewModel.toggleWatchlistCurrent() },
                        onDismissItem = { viewModel.dismissCurrentItem() },
                        onSpinAgain = { viewModel.startCinematicWarpSearch() },
                        onOpenWhyThis = { viewModel.openWhyThisModal() },
                        onMarkAsSeen = { viewModel.markCurrentAsSeen() },
                        onNotInterested = { viewModel.markCurrentAsNotInterested() },
                        onOpenSettings = { viewModel.openSettingsModal() }
                    )
                }
            }
        }
        }



        // BOTTOM: Minimal Floating Frosted Glass Bottom Navigation Bar (4 Destinations)
        if (!isFullscreenDetailOrWarp) {
            FloatingBottomBar(
                hazeState = hazeState,
                currentScreen = currentScreen,
                watchlistCount = watchlist.size,
                onNavigate = { screen -> viewModel.switchScreen(screen) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }

        // Settings Modal (•••)
        if (showSettingsModal) {
            SettingsModal(
                sfxEnabled = sfxEnabled,
                hapticEnabled = hapticEnabled,
                seenCount = seenList.size,
                onToggleSfx = { viewModel.toggleAudioSFX() },
                onToggleHaptic = { viewModel.toggleHapticFeedback() },
                onOpenAlreadySeen = { viewModel.openAlreadySeenLibrary() },
                onDismiss = { viewModel.closeSettingsModal() }
            )
        }

        // Modals
        if (showLanguageModal) {
            LanguageModal(
                selectedLanguage = selectedLanguage,
                onSelectLanguage = { viewModel.selectLanguage(it) },
                onDismiss = { viewModel.closeLanguageModal() }
            )
        }

        if (showRegionModal) {
            RegionModal(
                selectedRegion = selectedRegion,
                onSelectRegion = { viewModel.selectRegion(it) },
                onDismiss = { viewModel.closeRegionModal() }
            )
        }

        if (showWhyThisModal && currentItem != null) {
            WhyThisModal(
                item = currentItem!!,
                activeEra = selectedEra,
                activePickMode = selectedPickMode,
                onDismiss = { viewModel.closeWhyThisModal() }
            )
        }

        if (showNoResultsModal) {
            NoResultsModal(
                onAdjustFilters = { viewModel.relaxFiltersAndSpin() },
                onSurpriseMe = { viewModel.triggerSurpriseMe() },
                onDismiss = { viewModel.dismissNoResultsModal() }
            )
        }

        // First Launch Onboarding Overlay (3 Cinematic Scenes)
        if (isOnboardingActive) {
            OnboardingOverlay(
                step = onboardingStep,
                onNext = { viewModel.nextOnboardingScene() },
                onFinish = { viewModel.completeOnboarding() },
                onPlayTactile = { viewModel.audioManager.playTactileClick(sfxEnabled) }
            )
        }

        // Returning User Brand Intro (1.5s)
        if (!isOnboardingActive && isBrandIntroActive) {
            BrandIntroOverlay(phrase = brandIntroPhrase)
        }
    }
}
