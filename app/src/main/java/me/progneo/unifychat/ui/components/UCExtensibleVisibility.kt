package me.progneo.unifychat.ui.components

import androidx.compose.animation.*
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
