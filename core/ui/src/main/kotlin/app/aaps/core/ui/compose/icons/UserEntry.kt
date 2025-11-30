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
 * Icon for User Entry treatment type.
 * Represents user action log entries (document/note icon).
 */
val UserEntry: ImageVector by lazy {
    ImageVector.Builder(
        name = "UserEntry",
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
            moveTo(280f, -280f)
            horizontalLineToRelative(280f)
            verticalLineToRelative(-80f)
            horizontalLineTo(280f)
            verticalLineToRelative(80f)
            close()
            moveTo(280f, -440f)
            horizontalLineToRelative(400f)
            verticalLineToRelative(-80f)
            horizontalLineTo(280f)
            verticalLineToRelative(80f)
            close()
            moveTo(280f, -600f)
            horizontalLineToRelative(400f)
            verticalLineToRelative(-80f)
            horizontalLineTo(280f)
            verticalLineToRelative(80f)
            close()
            moveTo(200f, -120f)
            quadToRelative(-33f, 0f, -56.5f, -23.5f)
            reflectiveQuadTo(120f, -200f)
            verticalLineToRelative(-560f)
            quadToRelative(0f, -33f, 23.5f, -56.5f)
            reflectiveQuadTo(200f, -840f)
            horizontalLineToRelative(560f)
            quadToRelative(33f, 0f, 56.5f, 23.5f)
            reflectiveQuadTo(840f, -760f)
            verticalLineToRelative(560f)
            quadToRelative(0f, 33f, -23.5f, 56.5f)
            reflectiveQuadTo(760f, -120f)
            horizontalLineTo(200f)
            close()
            moveTo(200f, -200f)
            horizontalLineToRelative(560f)
            verticalLineToRelative(-560f)
            horizontalLineTo(200f)
            verticalLineToRelative(560f)
            close()
            moveTo(200f, -760f)
            verticalLineToRelative(560f)
            verticalLineToRelative(-560f)
            close()
        }
    }.build()
}
