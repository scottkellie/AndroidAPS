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
 * Icon for Profile Switch treatment type.
 * Represents profile changes and adjustments.
 */
val ProfileSwitch: ImageVector by lazy {
    ImageVector.Builder(
        name = "ProfileSwitch",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(19.744f, 9.7f)
            curveToRelative(-0.135f, -0.417f, -0.494f, -0.72f, -0.928f, -0.783f)
            lineToRelative(-3.997f, -0.581f)
            lineTo(13.03f, 4.714f)
            curveToRelative(-0.387f, -0.786f, -1.675f, -0.786f, -2.061f, 0f)
            lineTo(9.181f, 8.336f)
            lineTo(5.183f, 8.918f)
            curveTo(4.751f, 8.98f, 4.39f, 9.284f, 4.255f, 9.7f)
            curveToRelative(-0.135f, 0.416f, -0.023f, 0.873f, 0.291f, 1.178f)
            lineToRelative(2.894f, 2.819f)
            lineTo(6.757f, 17.68f)
            curveToRelative(-0.074f, 0.432f, 0.103f, 0.868f, 0.457f, 1.125f)
            curveToRelative(0.2f, 0.146f, 0.437f, 0.22f, 0.676f, 0.22f)
            curveToRelative(0.183f, 0f, 0.367f, -0.044f, 0.535f, -0.133f)
            lineTo(12f, 17.013f)
            lineToRelative(3.576f, 1.879f)
            curveToRelative(0.39f, 0.203f, 0.855f, 0.173f, 1.212f, -0.087f)
            curveToRelative(0.353f, -0.257f, 0.531f, -0.694f, 0.456f, -1.125f)
            lineToRelative(-0.683f, -3.983f)
            lineToRelative(2.893f, -2.819f)
            curveTo(19.766f, 10.573f, 19.881f, 10.116f, 19.744f, 9.7f)
            close()
            moveTo(18.689f, 10.261f)
            lineToRelative(-3.16f, 3.081f)
            lineToRelative(0.746f, 4.35f)
            curveToRelative(0.014f, 0.087f, -0.021f, 0.174f, -0.092f, 0.225f)
            curveToRelative(-0.04f, 0.03f, -0.087f, 0.044f, -0.135f, 0.044f)
            curveToRelative(-0.036f, 0f, -0.073f, -0.008f, -0.108f, -0.027f)
            lineToRelative(-3.907f, -2.053f)
            lineToRelative(-3.907f, 2.053f)
            curveToRelative(-0.075f, 0.044f, -0.17f, 0.036f, -0.242f, -0.017f)
            curveToRelative(-0.07f, -0.051f, -0.106f, -0.138f, -0.091f, -0.225f)
            lineToRelative(0.746f, -4.35f)
            lineToRelative(-3.162f, -3.081f)
            curveToRelative(-0.063f, -0.061f, -0.085f, -0.153f, -0.058f, -0.236f)
            curveToRelative(0.027f, -0.083f, 0.099f, -0.143f, 0.185f, -0.156f)
            lineToRelative(4.369f, -0.634f)
            lineToRelative(1.954f, -3.959f)
            curveToRelative(0.078f, -0.158f, 0.334f, -0.158f, 0.412f, 0f)
            lineToRelative(1.953f, 3.959f)
            lineToRelative(4.369f, 0.634f)
            curveToRelative(0.087f, 0.013f, 0.158f, 0.073f, 0.185f, 0.156f)
            reflectiveCurveTo(18.753f, 10.2f, 18.689f, 10.261f)
            close()
        }
    }.build()
}
