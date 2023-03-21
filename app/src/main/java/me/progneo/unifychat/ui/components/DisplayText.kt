package me.progneo.unifychat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisplayText(
    modifier: Modifier = Modifier,
    text: String,
    desc: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                top = 48.dp,
                end = 24.dp,
                bottom = 24.dp,
            )
    ) {
        Text(
            modifier = Modifier
                .height(46.dp),
            text = text,
            style = MaterialTheme.typography.displaySmall.copy(
                baselineShift = BaselineShift.Superscript
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        UCExtensibleVisibility(visible = desc.isNotEmpty()) {
            Text(
                modifier = Modifier.height(19.dp),
                text = desc,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    baselineShift = BaselineShift.Superscript
                ),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}