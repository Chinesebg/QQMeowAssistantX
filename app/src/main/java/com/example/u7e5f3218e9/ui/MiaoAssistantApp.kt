package com.example.u7e5f3218e9.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.u7e5f3218e9.R
import com.example.u7e5f3218e9.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun MiaoAssistantApp(
    config: AppConfig,
    serviceEnabled: Boolean,
    onConfigChange: (AppConfig) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onInvalidRules: (Int) -> Unit,
) {
    var subPage by rememberSaveable { mutableStateOf<AppSubPage?>(null) }
    val pagerState = rememberPagerState(pageCount = { MainDestination.entries.size })
    val scope = rememberCoroutineScope()
    val mainPagerState = remember(pagerState, scope) { MiaoMainPagerState(pagerState, scope) }

    LaunchedEffect(pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    fun navigateBack() {
        subPage = when (subPage) {
            AppSubPage.LICENSES -> AppSubPage.ABOUT
            AppSubPage.MAINTAINERS -> AppSubPage.ABOUT
            else -> null
        }
    }

    BackHandler(enabled = subPage != null) { navigateBack() }
    BackHandler(enabled = subPage == null && mainPagerState.selectedPage != MainDestination.HOME.ordinal) {
        mainPagerState.animateToPage(MainDestination.HOME.ordinal)
    }

    AnimatedContent(
        targetState = subPage,
        modifier = Modifier
            .fillMaxSize()
            .background(if (config.backgroundUri.isNotEmpty()) Color.Transparent else MiuixTheme.colorScheme.surface),
        transitionSpec = {
            if (targetState.depth < initialState.depth) {
                (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                    (slideOutHorizontally { it } + fadeOut())
            } else {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 4 } + fadeOut())
            }
        },
        label = "settingsSubPage",
    ) { page ->
        when (page) {
            AppSubPage.APPEARANCE -> AppearancePage(
                config = config,
                glassSupported = isRuntimeShaderSupported(),
                onConfigChange = onConfigChange,
                onBack = ::navigateBack,
            )

            AppSubPage.ABOUT -> AboutPage(
                config = config,
                onBack = ::navigateBack,
                onOpenLicenses = { subPage = AppSubPage.LICENSES },
                onOpenMaintainers = { subPage = AppSubPage.MAINTAINERS },
            )

            AppSubPage.LICENSES -> ThirdPartyLicensesPage(
                config = config,
                onBack = ::navigateBack,
            )

            AppSubPage.MAINTAINERS -> MaintainersPage(
                config = config,
                onBack = ::navigateBack,
            )

            null -> MainPager(
                config = config,
                serviceEnabled = serviceEnabled,
                mainPagerState = mainPagerState,
                onConfigChange = onConfigChange,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onInvalidRules = onInvalidRules,
                onOpenAppearance = { subPage = AppSubPage.APPEARANCE },
                onOpenAbout = { subPage = AppSubPage.ABOUT },
            )
        }
    }
}

@Composable
private fun MainPager(
    config: AppConfig,
    serviceEnabled: Boolean,
    mainPagerState: MiaoMainPagerState,
    onConfigChange: (AppConfig) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onInvalidRules: (Int) -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val pagerState = mainPagerState.pagerState
    var bottomBarVisible by remember { mutableStateOf(true) }
    val destinations = MainDestination.entries
    val bottomBarItems = destinations.map { destination ->
        BottomBarItem(
            label = stringResource(destination.label),
            icon = destination.icon,
        )
    }
    val floating = true
    val glassActive = floating &&
        config.liquidGlassEnabled &&
        isRuntimeShaderSupported()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = if (glassActive) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else {
        null
    }

    Scaffold(
        containerColor = if (config.backgroundUri.isNotEmpty()) Color.Transparent else MiuixTheme.colorScheme.surface,
        bottomBar = {
            AnimatedVisibility(
                visible = config.fixedBottomBar || bottomBarVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                MiaoBottomBar(
                    items = bottomBarItems,
                    selectedIndex = mainPagerState.selectedPage,
                    floating = true,
                    liquidGlass = glassActive,
                    backdrop = backdrop,
                    onSelected = mainPagerState::animateToPage,
                )
            }
        },
    ) { innerPadding ->
        val bottomInnerPadding = innerPadding.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = destinations.lastIndex,
                overscrollEffect = null,
                key = { page -> destinations[page].name },
            ) { page ->
                when (destinations[page]) {
                    MainDestination.HOME -> HomePage(
                        config = config,
                        serviceEnabled = serviceEnabled,
                        bottomInnerPadding = bottomInnerPadding,
                        onConfigChange = onConfigChange,
                        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                        onInvalidRules = onInvalidRules,
                        onScrollDown = { down -> bottomBarVisible = !down },
                    )

                    MainDestination.SETTINGS -> SettingsPage(
                        config = config,
                        bottomInnerPadding = bottomInnerPadding,
                        onOpenAppearance = onOpenAppearance,
                        onOpenAbout = onOpenAbout,
                        onConfigChange = onConfigChange,
                        onScrollDown = { down -> bottomBarVisible = !down },
                    )
                }
            }
        }
    }
}

private class MiaoMainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    private var navigating = false
    private var navigationJob: Job? = null

    fun animateToPage(targetPage: Int) {
        if (targetPage == selectedPage || targetPage !in 0 until pagerState.pageCount) return

        navigationJob?.cancel()
        selectedPage = targetPage
        navigating = true
        navigationJob = coroutineScope.launch {
            val runningJob = coroutineContext.job
            try {
                pagerState.springAnimateToPage(targetPage)
            } finally {
                if (navigationJob == runningJob) {
                    navigating = false
                    if (pagerState.currentPage != targetPage) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!navigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

private suspend fun PagerState.springAnimateToPage(targetPage: Int) {
    if (targetPage !in 0 until pageCount) return
    var shouldSnapToTarget = false
    scroll(MutatePriority.UserInput) {
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val distance = targetPage - currentPage - currentPageOffsetFraction
        val scrollPixels = distance * pageSize
        if (abs(scrollPixels) <= 0.5f) return@scroll

        var consumedScroll = 0f
        var skipScroll = false
        Animatable(0f).animateTo(
            targetValue = scrollPixels,
            animationSpec = PagerNavigationSpring,
        ) {
            if (skipScroll) return@animateTo

            val delta = value - consumedScroll
            if (abs(delta) > 0.5f) {
                val consumed = scrollBy(delta)
                consumedScroll += consumed
                if (abs(delta - consumed) > 0.1f) {
                    shouldSnapToTarget = true
                    skipScroll = true
                }
            } else {
                consumedScroll = value
            }

            if (abs(velocity) < 0.1f && abs(scrollPixels - consumedScroll) < 1f) {
                skipScroll = true
            }
        }

        val remaining = scrollPixels - consumedScroll
        if (abs(remaining) > 0.5f) scrollBy(remaining)
    }

    if (shouldSnapToTarget || currentPage != targetPage) {
        scrollToPage(targetPage)
    }
}

private enum class AppSubPage {
    APPEARANCE,
    ABOUT,
    LICENSES,
    MAINTAINERS,
}

private val AppSubPage?.depth: Int
    get() = when (this) {
        null -> 0
        AppSubPage.LICENSES -> 2
        else -> 1
    }

private enum class MainDestination(
    @get:StringRes val label: Int,
    val icon: ImageVector,
) {
    HOME(R.string.nav_home, Icons.Rounded.Cottage),
    SETTINGS(R.string.nav_settings, Icons.Rounded.Settings),
}

private val PagerNavigationSpring = spring<Float>(
    dampingRatio = 32.31f / (2f * sqrt(322.2f)),
    stiffness = 322.2f,
    visibilityThreshold = 0.5f,
)
