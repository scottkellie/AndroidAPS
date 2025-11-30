package app.aaps.ui.compose

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

/**
 * Composable screen displaying treatments with tab navigation.
 * Uses AndroidView to embed existing fragment-based treatment screens.
 *
 * @param activity The FragmentActivity hosting this screen
 * @param showExtendedBolusTab Whether to show the Extended Bolus tab
 * @param onNavigateBack Callback when back navigation is requested
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TreatmentsScreen(
    activity: FragmentActivity,
    showExtendedBolusTab: Boolean,
    onNavigateBack: () -> Unit
) {
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

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(tabs[pagerState.currentPage].titleRes)) },
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
            PrimaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
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

            // Fragment container with swipe support
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AndroidView(
                    factory = { context ->
                        FragmentContainerView(context).apply {
                            id = View.generateViewId()
                        }
                    },
                    update = { view ->
                        val fragment = tabs[page].fragmentClass.getDeclaredConstructor().newInstance()
                        activity.supportFragmentManager.beginTransaction()
                            .replace(view.id, fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
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
