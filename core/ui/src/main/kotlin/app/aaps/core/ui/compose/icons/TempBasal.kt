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
 * Icon for Temporary Basal treatment type.
 * Represents temporary basal rate adjustments.
 */
val TempBasal: ImageVector by lazy {
    ImageVector.Builder(
        name = "TempBasal",
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
            moveTo(16.578f, 18.033f)
            verticalLineToRelative(-11.043f)
            horizontalLineToRelative(-3.698f)
            verticalLineToRelative(11.043f)
            horizontalLineToRelative(-9.363f)
            verticalLineToRelative(-1.01f)
            horizontalLineToRelative(8.354f)
            verticalLineToRelative(-11.041f)
            horizontalLineToRelative(5.717f)
            verticalLineToRelative(11.041f)
            horizontalLineToRelative(1.645f)
            verticalLineToRelative(1.01f)
            close()
        }
    }.build()
}
