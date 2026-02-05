package dev.lackluster.hyperx.compose.base

import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.lackluster.hyperx.compose.R
import dev.lackluster.hyperx.compose.navigation3.MiuixNavHostDefaults
import dev.lackluster.hyperx.compose.navigation3.NavigationState
import dev.lackluster.hyperx.compose.navigation3.Navigator
import dev.lackluster.hyperx.compose.navigation3.rememberNavigationState
import dev.lackluster.hyperx.compose.navigation3.toEntries
import dev.lackluster.hyperx.compose.theme.AppTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils

@Composable
fun HyperXApp(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>,
    emptyRoute: NavKey,
    autoSplitView: MutableState<Boolean> = mutableStateOf(true),
    mainPageContent: @Composable (navigator: Navigator, adjustPadding: PaddingValues, mode: BasePageDefaults.Mode) -> Unit,
    emptyPageContent: @Composable () -> Unit = { DefaultEmptyPage() },
    otherPageBuilder: (@Composable (key: NavKey, navigator: Navigator, adjustPadding: PaddingValues, mode: BasePageDefaults.Mode) -> Unit)? = null
) {
    AppTheme {
        val navigationState = rememberNavigationState(
            startRoute = startRoute,
            topLevelRoutes = topLevelRoutes
        )
        val navigator = remember { Navigator(navigationState) }
        val canGoBack by remember { derivedStateOf { navigator.canGoBack } }
        val navigationEventState = rememberNavigationEventState(NavigationEventInfo.None)

        NavigationBackHandler(
            state = navigationEventState,
            isBackEnabled = canGoBack,
            onBackCompleted = { navigator.goBack() }
        )

        val configuration = LocalConfiguration.current
        val isLandscape by rememberUpdatedState(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        val density = LocalDensity.current
        val windowInfo by rememberUpdatedState(LocalWindowInfo.current)
        val windowWidth by rememberUpdatedState(windowInfo.containerDpSize.width / density.density)
        val windowHeight by rememberUpdatedState(windowInfo.containerDpSize.height / density.density)
        val largeScreen by remember { derivedStateOf { (windowHeight >= 480.dp && windowWidth >= 840.dp) } }
        val appRootLayout: AppRootLayout
        val normalLayoutPadding: PaddingValues
        val splitRightWeight: Float
        if (autoSplitView.value && largeScreen && isLandscape) {
            appRootLayout = AppRootLayout.Split12
            normalLayoutPadding = PaddingValues(0.dp)
            splitRightWeight = 2.0f
        } else if (autoSplitView.value && (largeScreen || isLandscape)) {
            appRootLayout = AppRootLayout.Split11
            normalLayoutPadding = PaddingValues(0.dp)
            splitRightWeight = 1.0f
        } else if (largeScreen) {
            appRootLayout = AppRootLayout.LargeScreen
            normalLayoutPadding = PaddingValues(horizontal = windowWidth * 0.1f)
            splitRightWeight = 1.0f
        } else {
            appRootLayout = AppRootLayout.Normal
            normalLayoutPadding = PaddingValues(0.dp)
            splitRightWeight = 1.0f
        }
        if (appRootLayout == AppRootLayout.Split11 || appRootLayout == AppRootLayout.Split12) {
            SplitLayout(
                navigationState,
                navigator,
                emptyRoute,
                mainPageContent,
                emptyPageContent,
                otherPageBuilder,
                1.0f,
                splitRightWeight
            )
        } else {
            NormalLayout(navigationState, navigator, mainPageContent, otherPageBuilder, normalLayoutPadding)
        }
        MiuixPopupUtils.MiuixPopupHost()
    }
}

@Composable
fun NormalLayout(
    navigationState: NavigationState,
    navigator: Navigator,
    mainPageContent: @Composable (navigator: Navigator, adjustPadding: PaddingValues, mode: BasePageDefaults.Mode) -> Unit,
    otherPageBuilder: (@Composable (key: NavKey, navigator: Navigator, adjustPadding: PaddingValues, mode: BasePageDefaults.Mode) -> Unit)? = null,
    extraPadding: PaddingValues = PaddingValues(0.dp)
) {
    val layoutDirection = LocalLayoutDirection.current
    val systemBarInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal).asPaddingValues()
    val contentPadding = systemBarInsets.let {
        PaddingValues.Absolute(
            left = it.calculateLeftPadding(layoutDirection) + extraPadding.calculateLeftPadding(layoutDirection),
            top = extraPadding.calculateTopPadding(),
            right = it.calculateRightPadding(layoutDirection)+ extraPadding.calculateRightPadding(layoutDirection),
            bottom = extraPadding.calculateBottomPadding()
        )
    }

    val entryProvider = entryProvider {
        entry(navigationState.startRoute) {
            mainPageContent(navigator, contentPadding, BasePageDefaults.Mode.FULL)
        }
        navigationState.backStacks[navigationState.topLevelRoute]?.forEach { key ->
            if (key != navigationState.startRoute) {
                entry(key) {
                    otherPageBuilder?.invoke(key, navigator, contentPadding, BasePageDefaults.Mode.FULL)
                }
            }
        }
    }

    NavDisplay(
        modifier = Modifier.background(Color.Black),
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}

@Composable
fun SplitLayout(
    navigationState: NavigationState,
    navigator: Navigator,
    emptyRoute: NavKey,
    mainPageContent: @Composable (navigator: Navigator, adjustPadding: PaddingValues, mode: BasePageDefaults.Mode) -> Unit,
    emptyPageContent: @Composable () -> Unit,
    otherPageBuilder: (@Composable (key: NavKey, navigator: Navigator, adjustPadding: PaddingValues, mode: BasePageDefaults.Mode) -> Unit)? = null,
    leftWeight: Float = 1.0f,
    rightWeight: Float = 1.0f
) {
    val easing = MiuixNavHostDefaults.NavAnimationEasing
    val duration = 500
    val layoutDirection = LocalLayoutDirection.current
    val systemBarInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal).asPaddingValues()
    val contentPaddingLeft = systemBarInsets.let {
        PaddingValues.Absolute(
            left = it.calculateLeftPadding(layoutDirection) + 12.dp,
            top = it.calculateTopPadding(),
            right = 12.dp,
            bottom = it.calculateBottomPadding()
        )
    }
    val contentPaddingRight = systemBarInsets.let {
        PaddingValues.Absolute(
            left = 12.dp,
            top = it.calculateTopPadding(),
            right = it.calculateRightPadding(layoutDirection) + 12.dp,
            bottom = it.calculateBottomPadding()
        )
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.weight(leftWeight)
        ) {
            mainPageContent(navigator, contentPaddingLeft, BasePageDefaults.Mode.SPLIT_LEFT)
        }
        VerticalDivider(thickness = 0.75.dp, color = colorScheme.dividerLine)

        val entryProvider = entryProvider {
            entry(emptyRoute) {
                emptyPageContent()
            }
            navigationState.backStacks[navigationState.topLevelRoute]?.forEach { key ->
                if (key != navigationState.startRoute) {
                    entry(key) {
                        otherPageBuilder?.invoke(key, navigator, contentPaddingRight, BasePageDefaults.Mode.SPLIT_RIGHT)
                    }
                }
            }
        }

        NavDisplay(
            modifier = Modifier.weight(rightWeight),
            entries = navigationState.toEntries(entryProvider),
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(duration, 0, easing),
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(duration, 0, easing),
                )
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(duration, 0, easing),
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(duration, 0, easing),
                )
            },
            onBack = { navigator.goBack() }
        )
    }
}

@Composable
fun DefaultEmptyPage(
    imageIcon: ImageIcon = ImageIcon(
        iconRes = R.drawable.ic_miuix,
        iconSize = 255.dp
    )
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DrawableResIcon(imageIcon)
    }
}

enum class AppRootLayout {
    Normal,
    LargeScreen,
    Split11,
    Split12
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp,
    color: Color,
) =
    Canvas(modifier.fillMaxHeight().width(thickness)) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(thickness.toPx() / 2, 0f),
            end = Offset(thickness.toPx() / 2, size.height),
        )
    }