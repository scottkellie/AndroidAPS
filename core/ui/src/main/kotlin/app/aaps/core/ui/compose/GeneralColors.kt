package app.aaps.core.ui.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color scheme for general UI elements across the app.
 * Provides consistent color coding for common elements like IOB, COB, etc.
 *
 * **Usage:**
 * - Treatment screens
 * - Overview screen
 * - General UI elements
 *
 * **Color Assignment:**
 * - activeInsulinText: Blue - Active Insulin On Board (IOB) text color
 * - calculator: Green - Bolus calculator icon and related elements
 *
 * Colors match the existing theme attribute colors for consistency with the rest of the app.
 *
 * @property activeInsulinText Color for active insulin (IOB) text
 * @property calculator Color for calculator icon and elements
 */
data class GeneralColors(
    val activeInsulinText: Color,
    val calculator: Color
)

/**
 * Light mode color scheme for general elements.
 * Colors match the light theme values from colors.xml.
 */
internal val LightGeneralColors = GeneralColors(
    activeInsulinText = Color(0xFF1E88E5),  // iob color
    calculator = Color(0xFF66BB6A)           // colorCalculatorButton
)

/**
 * Dark mode color scheme for general elements.
 * Colors match the dark theme values from colors.xml (night folder).
 */
internal val DarkGeneralColors = GeneralColors(
    activeInsulinText = Color(0xFF1E88E5),  // iob color (same in both modes)
    calculator = Color(0xFF67E86A)           // colorCalculatorButton (night)
)

/**
 * CompositionLocal providing general colors based on current theme (light/dark).
 * Accessed via AapsTheme.generalColors in composables.
 */
internal val LocalGeneralColors = compositionLocalOf { LightGeneralColors }
