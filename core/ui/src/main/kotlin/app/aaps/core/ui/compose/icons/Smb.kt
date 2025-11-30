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
 * Icon for Super Micro Bolus (SMB).
 * Represents a drop/bolus with a downward arrow.
 */
val Smb: ImageVector by lazy {
    ImageVector.Builder(
        name = "Smb",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        // Drop/bolus shape
        // Scaled by 1.6 (2x for viewport change, 0.8 for 80% size) and centered
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(24f, 12.8f)
            curveToRelative(-1.76f, 2.4f, -3.2f, 4.8f, -3.2f, 8f)
            curveToRelative(0f, 3.52f, 2.88f, 6.4f, 6.4f, 6.4f)
            reflectiveCurveToRelative(6.4f, -2.88f, 6.4f, -6.4f)
            curveToRelative(0f, -3.2f, -1.44f, -5.6f, -3.2f, -8f)
            close()
        }
        // Vertical line (arrow shaft)
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(24f, 20.8f)
            verticalLineTo(27.2f)
        }
        // Arrow head (down)
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(20.8f, 24f)
            lineTo(24f, 27.2f)
            lineTo(27.2f, 24f)
        }
    }.build()
}
