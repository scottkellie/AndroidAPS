package app.aaps.ui.compose

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.icons.Carbs
import app.aaps.core.ui.compose.icons.Careportal
import app.aaps.core.ui.compose.icons.ExtendedBolus
import app.aaps.core.ui.compose.icons.ProfileSwitch
import app.aaps.core.ui.compose.icons.RunningMode
import app.aaps.core.ui.compose.icons.TempBasal
import app.aaps.core.ui.compose.icons.TempTarget
import app.aaps.core.ui.compose.icons.UserEntry
import app.aaps.ui.R

/**
 * Composable screen displaying treatments with tab navigation.
 * Uses AndroidView to embed existing fragment-based treatment screens.
 *
 * @param activity The FragmentActivity hosting this screen
 * @param showExtendedBolusTab Whether to show the Extended Bolus tab
 * @param onNavigateBack Callback when back navigation is requested
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentsScreen(
    activity: FragmentActivity,
    showExtendedBolusTab: Boolean,
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var containerInitialized by remember { mutableStateOf(false) }
    val containerId = remember { View.generateViewId() }

    val iconColors = AapsTheme.elementColors

    // Define tabs with their icons and content descriptions
    val tabs = remember(showExtendedBolusTab) {
        buildList {
            add(
                TreatmentTab(
                    icon = Carbs,
                    titleRes = R.string.carbs_and_bolus,
                    fragmentClass = app.aaps.ui.activities.fragments.TreatmentsBolusCarbsFragment::class.java,
                    colorGetter = { iconColors.bolusCarbs }
                )
            )
            if (showExtendedBolusTab) {
                add(
                    TreatmentTab(
                        icon = ExtendedBolus,
                        titleRes = app.aaps.core.ui.R.string.extended_bolus,
                        fragmentClass = app.aaps.ui.activities.fragments.TreatmentsExtendedBolusesFragment::class.java,
                        colorGetter = { iconColors.extendedBolus }
                    )
                )
            }
            add(
                TreatmentTab(
                    icon = TempBasal,
                    titleRes = app.aaps.core.ui.R.string.tempbasal_label,
                    fragmentClass = app.aaps.ui.activities.fragments.TreatmentsTemporaryBasalsFragment::class.java,
                    colorGetter = { iconColors.tempBasal }
                )
            )
            add(
                TreatmentTab(
                    icon = TempTarget,
                    titleRes = app.aaps.core.ui.R.string.temporary_target,
                    fragmentClass = app.aaps.ui.activities.fragments.TreatmentsTempTargetFragment::class.java,
                    colorGetter = { iconColors.tempTarget }
                )
            )
            add(
                TreatmentTab(
                    icon = ProfileSwitch,
                    titleRes = app.aaps.core.ui.R.string.careportal_profileswitch,
                    fragmentClass = app.aaps.ui.activities.fragments.TreatmentsProfileSwitchFragment::class.java,
                    colorGetter = { iconColors.profileSwitch }
                )
            )
            add(
                TreatmentTab(
                    icon = Careportal,
                    titleRes = app.aaps.core.ui.R.string.careportal,
                    fragmentClass = app.aaps.ui.activities.fragments.TreatmentsCareportalFragment::class.java,
                    colorGetter = { iconColors.careportal }
                )
            )
            add(
                TreatmentTab(
                    icon = RunningMode,
                    titleRes = app.aaps.core.ui.R.string.running_mode,
                    fragmentClass = app.aaps.ui.activities.fragments.TreatmentsRunningModeFragment::class.java,
                    colorGetter = { iconColors.runningMode }
                )
            )
            add(
                TreatmentTab(
                    icon = UserEntry,
                    titleRes = R.string.user_entry,
                    fragmentClass = app.aaps.ui.activities.fragments.TreatmentsUserEntryFragment::class.java,
                    colorGetter = { iconColors.userEntry }
                )
            )
        }
    }

    // Function to set fragment
    fun setFragment(fragment: Fragment) {
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(tabs[selectedTabIndex].titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            PrimaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            if (containerInitialized) {
                                val fragment = tab.fragmentClass.getDeclaredConstructor().newInstance()
                                setFragment(fragment)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.titleRes),
                                tint = tab.colorGetter(),  // Use theme colors for icons
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(stringResource(tab.titleRes))
                        }
                    )
                }
            }

            // Fragment container
            AndroidView(
                factory = { context ->
                    FragmentContainerView(context).apply {
                        id = containerId
                    }
                },
                update = { view ->
                    if (!containerInitialized) {
                        containerInitialized = true
                        // Load initial fragment
                        setFragment(tabs[0].fragmentClass.getDeclaredConstructor().newInstance())
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Data class representing a treatment tab.
 *
 * @param icon The ImageVector icon for the tab
 * @param titleRes The string resource ID for the tab title
 * @param fragmentClass The Fragment class to display when this tab is selected
 * @param colorGetter Lambda function that returns the color for the tab icon from theme
 */
private data class TreatmentTab(
    val icon: ImageVector,
    val titleRes: Int,
    val fragmentClass: Class<out Fragment>,
    val colorGetter: () -> Color
)
