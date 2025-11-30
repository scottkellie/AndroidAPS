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
 *
 * Bounding box: x: 50-896, y: 42-906 (viewport: 960x960, ~90% height)
 */
val Careportal: ImageVector by lazy {
    ImageVector.Builder(
        name = "Careportal",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
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
            moveTo(107.67f, 906.44f)
            quadToRelative(-24.93f, 5.93f, -43.33f, -12.47f)
            reflectiveQuadTo(50.44f, 850.64f)
            lineToRelative(47.48f, -226.73f)
            lineToRelative(235.13f, 235.13f)
            lineToRelative(-226.73f, 47.48f)
            close()
            moveTo(333.05f, 906.44f)
            lineTo(97.92f, 671.31f)
            lineToRelative(561.28f, -561.28f)
            quadToRelative(20.18f, 20.18f, 49.85f, 20.18f)
            reflectiveQuadToRelative(49.85f, 20.18f)
            lineToRelative(135.28f, 135.28f)
            quadToRelative(20.18f, 20.18f, 20.18f, 49.85f)
            reflectiveQuadToRelative(-20.18f, 49.85f)
            lineTo(333.05f, 906.44f)
            close()
            moveTo(721.05f, 134.24f)
            lineTo(190.79f, 664.5f)
            lineToRelative(134.11f, 134.11f)
            lineToRelative(524.56f, -524.56f)
            lineToRelative(-134.11f, -134.11f)
            close()
        }
    }.build()
}
