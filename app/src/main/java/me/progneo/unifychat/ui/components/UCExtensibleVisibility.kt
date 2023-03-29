package me.progneo.unifychat.ui.components

import androidx.compose.animation.* // ktlint-disable no-wildcard-imports
import androidx.compose.runtime.Composable

@Composable
fun UCExtensibleVisibility(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        content = content,
    )
}
