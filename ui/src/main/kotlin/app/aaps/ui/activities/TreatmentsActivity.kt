package app.aaps.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.activities.TranslatedDaggerAppCompatActivity
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.ui.compose.TreatmentsScreen
import javax.inject.Inject

/**
 * Activity that displays diabetes treatments with tab navigation.
 *
 * This Compose-based activity provides a centralized view of various treatment types including:
 *
 * 1. **Bolus & Carbs**: Insulin boluses and carbohydrate entries
 * 2. **Extended Boluses**: Extended/dual-wave bolus deliveries (if pump supports)
 * 3. **Temporary Basals**: Temporary basal rate adjustments
 * 4. **Temp Targets**: Temporary blood glucose targets
 * 5. **Profile Switches**: Profile changes and adjustments
 * 6. **Careportal**: General careportal entries and notes
 * 7. **Running Mode**: Running mode changes (closed loop, open loop, etc.)
 * 8. **User Entry**: User action log entries
 *
 * The activity uses Jetpack Compose for the tab navigation and toolbar while preserving
 * the existing Fragment-based implementation for each treatment category. This allows for
 * a gradual migration to Compose while maintaining existing functionality.
 *
 * @see app.aaps.ui.compose.TreatmentsScreen
 */
class TreatmentsActivity : TranslatedDaggerAppCompatActivity() {

    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var rxBus: RxBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Determine if Extended Bolus tab should be shown based on pump capabilities
        val showExtendedBolusTab = !activePlugin.activePump.isFakingTempsByExtendedBoluses &&
            activePlugin.activePump.pumpDescription.isExtendedBolusCapable

        setContent {
            CompositionLocalProvider(
                LocalPreferences provides preferences,
                LocalRxBus provides rxBus
            ) {
                AapsTheme {
                    TreatmentsScreen(
                        activity = this,
                        showExtendedBolusTab = showExtendedBolusTab,
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}