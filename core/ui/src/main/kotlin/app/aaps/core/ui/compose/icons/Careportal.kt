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
            moveTo(167f, 840f)
            quadToRelative(-21f, 5f, -36.5f, -10.5f)
            reflectiveQuadTo(120f, 793f)
            lineToRelative(40f, -191f)
            lineToRelative(198f, 198f)
            lineToRelative(-191f, 40f)
            close()
            moveTo(358f, 800f)
            lineTo(160f, 602f)
            lineToRelative(473f, -473f)
            quadToRelative(17f, -17f, 42f, -17f)
            reflectiveQuadToRelative(42f, 17f)
            lineToRelative(114f, 114f)
            quadToRelative(17f, 17f, 17f, 42f)
            reflectiveQuadToRelative(-17f, 42f)
            lineTo(358f, 800f)
            close()
            moveTo(675f, 172f)
            lineTo(233f, 614f)
            lineToRelative(113f, 113f)
            lineToRelative(442f, -442f)
            lineToRelative(-113f, -113f)
            close()
        }
    }.build()
}
