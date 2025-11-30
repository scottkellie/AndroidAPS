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
 * Icon for NSClient background service.
 * Represents the Nightscout client connection status.
 */
val Ns: ImageVector by lazy {
    ImageVector.Builder(
        name = "Ns",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        // Main pin/location marker shape
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
            moveTo(42.9f, 22.5f)
            curveToRelative(0f, 1.7f, -0.2f, 3.9f, -0.3f, 4.6f)
            curveToRelative(-0.3f, 2f, -0.8f, 3.9f, -1.4f, 5.8f)
            curveToRelative(-0.8f, 2.5f, -1.8f, 4.8f, -3.1f, 7.1f)
            curveToRelative(-0.8f, 1.4f, -1.6f, 2.8f, -2.4f, 4.1f)
            curveToRelative(-0.9f, 1.4f, -1.9f, 2.8f, -3f, 4.2f)
            curveToRelative(-1f, 1.3f, -2f, 2.5f, -3.1f, 3.8f)
            curveToRelative(-0.4f, 0.4f, -0.7f, 0.8f, -1.1f, 1.2f)
            curveToRelative(-0.7f, 0.8f, -1.3f, 1.4f, -2.1f, 2.1f)
            curveToRelative(-1.7f, 1.6f, -4.3f, 1.5f, -5.8f, -0.2f)
            curveToRelative(-1.1f, -1.1f, -2.2f, -2.3f, -3.3f, -3.5f)
            curveToRelative(-1.3f, -1.5f, -2.5f, -3f, -3.7f, -4.6f)
            curveToRelative(-1.7f, -2.3f, -3.3f, -4.8f, -4.7f, -7.4f)
            curveToRelative(-1f, -1.8f, -1.8f, -3.8f, -2.5f, -5.7f)
            curveToRelative(-0.9f, -2.7f, -1.7f, -5.4f, -1.9f, -8.2f)
            curveToRelative(0f, -0.3f, -0.1f, -1.7f, -0.1f, -3.2f)
            curveToRelative(0f, -1.6f, 0.2f, -2.8f, 0.2f, -3.3f)
            curveToRelative(0.3f, -1.8f, 0.9f, -3.5f, 1.7f, -5.2f)
            curveToRelative(0.6f, -1.4f, 1.5f, -2.6f, 2.4f, -3.8f)
            curveToRelative(0.9f, -1.1f, 2f, -2.1f, 3.1f, -3f)
            curveToRelative(1.3f, -1f, 2.6f, -1.8f, 4.1f, -2.4f)
            curveToRelative(1.6f, -0.7f, 3.3f, -1.2f, 5f, -1.4f)
            curveToRelative(0.3f, 0f, 1.4f, -0.2f, 2.8f, -0.2f)
            curveToRelative(1.8f, 0f, 3.5f, 0.3f, 4.3f, 0.4f)
            curveToRelative(1.8f, 0.4f, 3.5f, 1.1f, 5.1f, 2f)
            curveToRelative(2.3f, 1.3f, 4.2f, 2.9f, 5.8f, 5f)
            curveToRelative(1.1f, 1.5f, 2.1f, 3.1f, 2.7f, 4.8f)
            curveToRelative(0.5f, 1.2f, 0.8f, 2.5f, 1.1f, 3.8f)
            curveTo(42.7f, 19.6f, 42.9f, 21f, 42.9f, 22.5f)
            close()
            moveTo(23.8f, 6.2f)
            curveToRelative(-0.8f, 0f, -1.5f, 0.1f, -2.2f, 0.2f)
            curveToRelative(-1.6f, 0.2f, -3f, 0.6f, -4.5f, 1.3f)
            curveToRelative(-2.4f, 1.1f, -4.5f, 2.7f, -6.1f, 4.8f)
            curveToRelative(-1.2f, 1.5f, -2f, 3.1f, -2.6f, 4.8f)
            curveToRelative(-0.3f, 1f, -0.6f, 1.9f, -0.7f, 2.9f)
            curveToRelative(-0.1f, 0.8f, -0.2f, 1.7f, -0.2f, 2.5f)
            curveToRelative(0f, 1f, 0.1f, 1.9f, 0.2f, 2.9f)
            curveToRelative(0.1f, 0.9f, 0.2f, 1.9f, 0.5f, 2.8f)
            curveToRelative(0.6f, 2.6f, 1.5f, 5.2f, 2.6f, 7.6f)
            curveToRelative(1f, 2.1f, 2.1f, 4.2f, 3.4f, 6.1f)
            curveToRelative(1.3f, 2f, 2.7f, 3.9f, 4.2f, 5.7f)
            curveToRelative(1.4f, 1.7f, 2.8f, 3.3f, 4.3f, 4.8f)
            curveToRelative(0.6f, 0.6f, 1.3f, 0.6f, 1.9f, 0f)
            curveToRelative(1.5f, -1.5f, 3.7f, -4f, 4.7f, -5.3f)
            curveToRelative(1f, -1.2f, 1.9f, -2.4f, 2.7f, -3.6f)
            curveToRelative(1.1f, -1.6f, 2.1f, -3.2f, 3.1f, -4.9f)
            curveToRelative(1f, -1.9f, 2f, -3.9f, 2.7f, -5.9f)
            curveToRelative(0.7f, -1.8f, 1.2f, -3.7f, 1.5f, -5.6f)
            curveToRelative(0.3f, -1.4f, 0.4f, -2.8f, 0.5f, -4.3f)
            curveToRelative(0f, -0.8f, -0.1f, -1.7f, -0.2f, -2.5f)
            curveToRelative(-0.2f, -1.6f, -0.6f, -3.1f, -1.3f, -4.5f)
            curveToRelative(-0.9f, -2f, -2.2f, -3.8f, -3.9f, -5.4f)
            curveToRelative(-1.2f, -1.1f, -2.6f, -2f, -4.1f, -2.8f)
            curveTo(28.3f, 6.8f, 26.1f, 6.3f, 23.8f, 6.2f)
            close()
        }
        // Third path - glasses/eyes shape
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
            moveTo(22.2f, 26f)
            curveToRelative(0f, 1.2f, 0f, 2.4f, 0f, 3.6f)
            curveToRelative(0f, 0.6f, 0.2f, 0.9f, 0.8f, 0.8f)
            curveToRelative(0.4f, 0f, 0.9f, 0f, 1.3f, 0f)
            curveToRelative(0.7f, 0f, 0.8f, -0.1f, 0.8f, -0.8f)
            curveToRelative(0f, -2.3f, -0.1f, -4.7f, 0f, -7f)
            curveToRelative(0.1f, -2.1f, 1f, -3.7f, 2.8f, -4.8f)
            curveToRelative(1.9f, -1.1f, 3.9f, -1.1f, 5.8f, -0.2f)
            curveToRelative(1.6f, 0.8f, 2.6f, 2.1f, 3f, 3.9f)
            curveToRelative(0.4f, 1.5f, 0f, 3f, -0.3f, 4.5f)
            curveToRelative(-0.4f, 1.8f, -1.3f, 3.4f, -2.5f, 4.9f)
            curveToRelative(-1.8f, 2.2f, -4f, 3.6f, -6.7f, 4.4f)
            curveToRelative(-1.6f, 0.4f, -3.1f, 0.6f, -4.7f, 0.5f)
            curveToRelative(-2.5f, -0.2f, -4.7f, -1.1f, -6.7f, -2.5f)
            curveToRelative(-1.6f, -1.1f, -2.8f, -2.5f, -3.7f, -4.2f)
            curveToRelative(-0.7f, -1.2f, -1.2f, -2.6f, -1.4f, -4f)
            curveToRelative(-0.1f, -0.8f, -0.3f, -1.5f, -0.2f, -2.3f)
            curveToRelative(0.1f, -2.7f, 1.9f, -5.2f, 4.7f, -5.6f)
            curveToRelative(1.6f, -0.3f, 3.1f, -0.1f, 4.5f, 0.8f)
            curveToRelative(1.3f, 0.8f, 2.1f, 2f, 2.5f, 3.5f)
            curveToRelative(0.1f, 0.5f, 0.2f, 1f, 0.2f, 1.5f)
            curveTo(22.2f, 24f, 22.2f, 25f, 22.2f, 26f)
            close()
            moveTo(31f, 17.7f)
            curveToRelative(-2.8f, 0f, -5.1f, 2.2f, -5.1f, 5f)
            curveToRelative(0f, 2.9f, 2.3f, 5.2f, 5.1f, 5.2f)
            curveToRelative(2.9f, 0f, 5.1f, -2.2f, 5.1f, -5f)
            curveTo(36.1f, 19.9f, 33.8f, 17.7f, 31f, 17.7f)
            close()
            moveTo(21.4f, 22.7f)
            curveToRelative(0f, -2.8f, -2.3f, -5f, -4.9f, -5f)
            curveToRelative(-3.1f, -0.1f, -5.3f, 2.3f, -5.3f, 5f)
            curveToRelative(0f, 2.9f, 2.2f, 5.1f, 5.1f, 5.1f)
            curveTo(19f, 27.8f, 21.3f, 25.7f, 21.4f, 22.7f)
            close()
        }
        // Fourth path - eyebrows
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
            moveTo(34.5f, 14.8f)
            curveToRelative(-0.5f, -0.1f, -0.9f, -0.2f, -1.3f, -0.3f)
            curveToRelative(-2.8f, -0.6f, -5.4f, -0.1f, -7.7f, 1.6f)
            curveToRelative(-0.6f, 0.5f, -1.2f, 1f, -1.6f, 1.7f)
            curveToRelative(-0.2f, 0.4f, -0.4f, 0.2f, -0.5f, 0f)
            curveToRelative(-0.3f, -0.4f, -0.6f, -0.8f, -1f, -1.1f)
            curveToRelative(-1.4f, -1.3f, -2.9f, -2f, -4.8f, -2.3f)
            curveToRelative(-1.4f, -0.2f, -2.8f, -0.1f, -4.2f, 0.3f)
            curveToRelative(-0.2f, 0.1f, -0.3f, 0.1f, -0.5f, 0.2f)
            curveToRelative(0f, 0f, -0.1f, 0f, -0.2f, 0f)
            curveTo(15.6f, 11f, 19.5f, 9f, 24.2f, 9.2f)
            curveTo(28.5f, 9.3f, 32f, 11.3f, 34.5f, 14.8f)
            close()
        }
        // Fifth path - right lower detail
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
            moveTo(23.6f, 49.6f)
            curveToRelative(-0.9f, -1f, -1.8f, -2f, -2.7f, -3f)
            curveToRelative(-0.4f, -0.4f, -0.7f, -0.9f, -1.1f, -1.3f)
            curveToRelative(-0.1f, -0.2f, -0.1f, -0.2f, 0f, -0.4f)
            curveToRelative(1.1f, -1.6f, 2.2f, -3.2f, 3.2f, -4.9f)
            curveToRelative(0.2f, -0.4f, 0.4f, -0.7f, 0.6f, -1.1f)
            curveToRelative(0.1f, -0.2f, 0.2f, -0.2f, 0.4f, -0.3f)
            curveToRelative(2.6f, -0.1f, 5f, -0.7f, 7.2f, -2f)
            curveToRelative(0.8f, -0.4f, 1.6f, -0.9f, 2.3f, -1.5f)
            curveTo(31.2f, 40.6f, 27.7f, 45.2f, 23.6f, 49.6f)
            close()
        }
        // Sixth path - left lower detail
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
            moveTo(13.6f, 35.2f)
            curveToRelative(1f, 0.7f, 2f, 1.4f, 3.2f, 1.9f)
            curveToRelative(1.2f, 0.6f, 2.4f, 1f, 3.7f, 1.2f)
            curveToRelative(0.3f, 0.1f, 0.3f, 0.2f, 0.2f, 0.4f)
            curveToRelative(-0.7f, 1.1f, -1.3f, 2.2f, -2.1f, 3.2f)
            curveToRelative(-0.2f, 0.3f, -0.4f, 0.5f, -0.6f, 0.8f)
            curveTo(16.3f, 40.3f, 14.8f, 37.8f, 13.6f, 35.2f)
            close()
        }
        // Seventh path - right eye pupil
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
            moveTo(31f, 20.6f)
            curveToRelative(1.2f, -0.2f, 2.3f, 1f, 2.2f, 2.2f)
            curveToRelative(0f, 1f, -1f, 2.2f, -2.2f, 2.2f)
            curveToRelative(-1.1f, 0f, -2.2f, -1.1f, -2.2f, -2.3f)
            curveTo(28.8f, 21.6f, 29.9f, 20.4f, 31f, 20.6f)
            close()
        }
        // Eighth path - left eye pupil
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
            moveTo(18.5f, 22.7f)
            curveToRelative(0.1f, 1.2f, -1.1f, 2.3f, -2.2f, 2.3f)
            curveToRelative(-1.2f, 0f, -2.2f, -1f, -2.2f, -2.3f)
            curveToRelative(0f, -1f, 0.9f, -2.2f, 2.4f, -2.1f)
            curveTo(17.4f, 20.6f, 18.6f, 21.6f, 18.5f, 22.7f)
            close()
        }
    }.build()
}
