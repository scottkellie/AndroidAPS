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
 * Icon for insulin pump.
 * Represents the insulin pump device with display and buttons.
 */
val Pump: ImageVector by lazy {
    ImageVector.Builder(
        name = "Pump",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        // Main pump body
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(14f, 8f)
            lineTo(34f, 8f)
            arcTo(4f, 4f, 0f, false, true, 38f, 12f)
            lineTo(38f, 36f)
            arcTo(4f, 4f, 0f, false, true, 34f, 40f)
            lineTo(14f, 40f)
            arcTo(4f, 4f, 0f, false, true, 10f, 36f)
            lineTo(10f, 12f)
            arcTo(4f, 4f, 0f, false, true, 14f, 8f)
            close()
        }
        // Display screen
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(15.6f, 12f)
            lineTo(32.4f, 12f)
            arcTo(1.6f, 1.6f, 0f, false, true, 34f, 13.6f)
            lineTo(34f, 18.4f)
            arcTo(1.6f, 1.6f, 0f, false, true, 32.4f, 20f)
            lineTo(15.6f, 20f)
            arcTo(1.6f, 1.6f, 0f, false, true, 14f, 18.4f)
            lineTo(14f, 13.6f)
            arcTo(1.6f, 1.6f, 0f, false, true, 15.6f, 12f)
            close()
        }
        // Top center button
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(22.6f, 24f)
            lineTo(25.4f, 24f)
            arcTo(0.6f, 0.6f, 0f, false, true, 26f, 24.6f)
            lineTo(26f, 27.4f)
            arcTo(0.6f, 0.6f, 0f, false, true, 25.4f, 28f)
            lineTo(22.6f, 28f)
            arcTo(0.6f, 0.6f, 0f, false, true, 22f, 27.4f)
            lineTo(22f, 24.6f)
            arcTo(0.6f, 0.6f, 0f, false, true, 22.6f, 24f)
            close()
        }
        // Bottom center button
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(22.6f, 30f)
            lineTo(25.4f, 30f)
            arcTo(0.6f, 0.6f, 0f, false, true, 26f, 30.6f)
            lineTo(26f, 33.4f)
            arcTo(0.6f, 0.6f, 0f, false, true, 25.4f, 34f)
            lineTo(22.6f, 34f)
            arcTo(0.6f, 0.6f, 0f, false, true, 22f, 33.4f)
            lineTo(22f, 30.6f)
            arcTo(0.6f, 0.6f, 0f, false, true, 22.6f, 30f)
            close()
        }
        // Left button
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(18.6f, 27f)
            lineTo(21.4f, 27f)
            arcTo(0.6f, 0.6f, 0f, false, true, 22f, 27.6f)
            lineTo(22f, 30.4f)
            arcTo(0.6f, 0.6f, 0f, false, true, 21.4f, 31f)
            lineTo(18.6f, 31f)
            arcTo(0.6f, 0.6f, 0f, false, true, 18f, 30.4f)
            lineTo(18f, 27.6f)
            arcTo(0.6f, 0.6f, 0f, false, true, 18.6f, 27f)
            close()
        }
        // Right button
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(26.6f, 27f)
            lineTo(29.4f, 27f)
            arcTo(0.6f, 0.6f, 0f, false, true, 30f, 27.6f)
            lineTo(30f, 30.4f)
            arcTo(0.6f, 0.6f, 0f, false, true, 29.4f, 31f)
            lineTo(26.6f, 31f)
            arcTo(0.6f, 0.6f, 0f, false, true, 26f, 30.4f)
            lineTo(26f, 27.6f)
            arcTo(0.6f, 0.6f, 0f, false, true, 26.6f, 27f)
            close()
        }
    }.build()
}
