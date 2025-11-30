package app.aaps.core.ui.compose.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icon for Careportal/Note treatment type.
 * Represents careportal entries and notes.
 */
val Careportal: ImageVector by lazy {
    ImageVector.Builder(
        name = "Careportal",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(13.909f, 5.715f)
            lineToRelative(-7.634f, 7.634f)
            curveToRelative(-0.061f, 0.061f, -0.102f, 0.139f, -0.118f, 0.224f)
            lineToRelative(-0.911f, 4.941f)
            curveToRelative(-0.025f, 0.138f, 0.019f, 0.279f, 0.118f, 0.378f)
            curveToRelative(0.099f, 0.099f, 0.24f, 0.143f, 0.378f, 0.118f)
            lineToRelative(4.941f, -0.911f)
            curveToRelative(0.085f, -0.016f, 0.163f, -0.057f, 0.224f, -0.118f)
            lineToRelative(7.634f, -7.634f)
            curveToRelative(0.445f, -0.445f, 0.444f, -1.17f, -0.003f, -1.617f)
            lineToRelative(-3.012f, -3.012f)
            curveTo(15.079f, 5.271f, 14.354f, 5.27f, 13.909f, 5.715f)
            close()
            moveTo(9.891f, 16.927f)
            curveToRelative(0.097f, 0.097f, 0.132f, 0.24f, 0.091f, 0.371f)
            curveToRelative(-0.018f, 0.058f, -0.05f, 0.109f, -0.091f, 0.15f)
            curveToRelative(-0.053f, 0.053f, -0.121f, 0.089f, -0.197f, 0.102f)
            lineToRelative(-2.462f, 0.435f)
            curveToRelative(-0.244f, -0.008f, -0.486f, -0.103f, -0.673f, -0.29f)
            curveToRelative(-0.187f, -0.187f, -0.282f, -0.429f, -0.29f, -0.673f)
            lineToRelative(0.435f, -2.463f)
            curveToRelative(0.024f, -0.135f, 0.121f, -0.246f, 0.252f, -0.287f)
            curveToRelative(0.131f, -0.041f, 0.274f, -0.006f, 0.371f, 0.091f)
            lineTo(9.891f, 16.927f)
            close()
            moveTo(17.903f, 9.269f)
            curveToRelative(0.141f, 0.141f, 0.141f, 0.369f, 0f, 0.509f)
            lineToRelative(-6.746f, 6.746f)
            curveToRelative(-0.141f, 0.141f, -0.369f, 0.141f, -0.509f, 0f)
            lineToRelative(-0.265f, -0.265f)
            curveToRelative(-0.141f, -0.141f, -0.141f, -0.369f, 0f, -0.509f)
            lineToRelative(6.746f, -6.746f)
            curveToRelative(0.141f, -0.141f, 0.369f, -0.141f, 0.509f, 0f)
            lineTo(17.903f, 9.269f)
            close()
            moveTo(16.588f, 7.933f)
            curveToRelative(0.141f, 0.141f, 0.141f, 0.369f, 0f, 0.509f)
            lineToRelative(-6.746f, 6.746f)
            curveToRelative(-0.141f, 0.141f, -0.369f, 0.141f, -0.509f, 0f)
            lineToRelative(-0.265f, -0.265f)
            curveToRelative(-0.141f, -0.141f, -0.141f, -0.369f, 0f, -0.509f)
            lineToRelative(6.746f, -6.746f)
            curveToRelative(0.141f, -0.141f, 0.369f, -0.141f, 0.509f, 0f)
            lineTo(16.588f, 7.933f)
            close()
            moveTo(15.273f, 6.596f)
            curveToRelative(0.141f, 0.141f, 0.141f, 0.369f, 0f, 0.509f)
            lineToRelative(-6.746f, 6.746f)
            curveToRelative(-0.141f, 0.141f, -0.369f, 0.141f, -0.509f, 0f)
            lineToRelative(-0.265f, -0.265f)
            curveToRelative(-0.141f, -0.141f, -0.141f, -0.369f, 0f, -0.509f)
            lineToRelative(6.746f, -6.746f)
            curveToRelative(0.141f, -0.141f, 0.369f, -0.141f, 0.509f, 0f)
            lineTo(15.273f, 6.596f)
            close()
        }
    }.build()
}
