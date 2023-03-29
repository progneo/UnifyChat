package me.progneo.unifychat.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

val Icons.Telegram: ImageVector
    get() {
        if (_telegram != null) {
            return _telegram!!
        }
        _telegram = ImageVector.Builder(
            name = "me.progneo.unifychat.ui.Icons.getTelegram",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 50.0F,
            viewportHeight = 50.0F,
        ).materialPath {
            moveTo(46.137F, 6.552F)
            curveToRelative(-0.75F, -0.636F, -1.928F, -0.727F, -3.146F, -0.238F)
            lineToRelative(-0.002F, 0.0F)
            curveTo(41.708F, 6.828F, 6.728F, 21.832F, 5.304F, 22.445F)
            curveToRelative(-0.259F, 0.09F, -2.521F, 0.934F, -2.288F, 2.814F)
            curveToRelative(0.208F, 1.695F, 2.026F, 2.397F, 2.248F, 2.478F)
            lineToRelative(8.893F, 3.045F)
            curveToRelative(0.59F, 1.964F, 2.765F, 9.21F, 3.246F, 10.758F)
            curveToRelative(0.3F, 0.965F, 0.789F, 2.233F, 1.646F, 2.494F)
            curveToRelative(0.752F, 0.29F, 1.5F, 0.025F, 1.984F, -0.355F)
            lineToRelative(5.437F, -5.043F)
            lineToRelative(8.777F, 6.845F)
            lineToRelative(0.209F, 0.125F)
            curveToRelative(0.596F, 0.264F, 1.167F, 0.396F, 1.712F, 0.396F)
            curveToRelative(0.421F, 0.0F, 0.825F, -0.079F, 1.211F, -0.237F)
            curveToRelative(1.315F, -0.54F, 1.841F, -1.793F, 1.896F, -1.935F)
            lineToRelative(6.556F, -34.077F)
            curveTo(47.231F, 7.933F, 46.675F, 7.007F, 46.137F, 6.552F)

            moveTo(22.0F, 32.0F)
            lineToRelative(-3.0F, 8.0F)
            lineToRelative(-3.0F, -10.0F)
            lineToRelative(23.0F, -17.0F)
            lineTo(22.0F, 32.0F)
            close()
        }.build()
        return _telegram!!
    }
private var _telegram: ImageVector? = null
