package dev.lackluster.hyperx.compose.navigation3

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@Composable
fun MiuixNavHost(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (NavKey) -> Unit
) {
    val currentKey = navigator.current() ?: return

    if (navigator.canPop) {
        val info = NavigationEventInfo.None
        val state = rememberNavigationEventState(info)
        NavigationBackHandler(
            state = state,
            onBackCompleted = { navigator.pop() }
        )
    }

    AnimatedContent(
        targetState = currentKey,
        modifier = Modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(modifier),
        transitionSpec = {
            val isPop = navigator.lastAction == NavAction.Pop
            if (isPop) {
                (miuixPopEnterTransition() togetherWith miuixPopExitTransition()).apply {
                    targetContentZIndex = -1f
                }
            } else {
                miuixEnterTransition() togetherWith miuixExitTransition()
            }
        },
        contentAlignment = contentAlignment,
        label = "MiuixNavHost"
    ) { key ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(key)
        }
    }
}

private fun <T> miuixSpringSpec() = spring<T>(
    dampingRatio = 0.85f,
    stiffness = 440f
)

private fun miuixEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = miuixSpringSpec()
    )

private fun miuixExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = miuixSpringSpec()
    ) + fadeOut(
        targetAlpha = 0.5f,
        animationSpec = miuixSpringSpec()
    )

private fun miuixPopEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = miuixSpringSpec()
    ) + fadeIn(
        initialAlpha = 0.5f,
        animationSpec = miuixSpringSpec()
    )

private fun miuixPopExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = miuixSpringSpec()
    )
