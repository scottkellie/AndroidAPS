package app.aaps.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.userEntry.UserEntryPresentationHelper
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.interfaces.utils.Translator
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
 * Uses Jetpack Compose for all content including each treatment type.
 *
 * @param showExtendedBolusTab Whether to show the Extended Bolus tab
 * @param persistenceLayer Database layer for treatment data
 * @param profileUtil Profile utility for unit conversion
 * @param profileFunction Profile function for calculations
 * @param activePlugin Active plugin for pump capabilities
 * @param rh Resource helper for string resources
 * @param translator Translator for treatment types
 * @param dateUtil Date utility for formatting dates and times
 * @param decimalFormatter Formatter for decimal values
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param userEntryPresentationHelper Helper for formatting user entry display
 * @param rxBus RxBus for observing treatment changes
 * @param aapsSchedulers Schedulers for RxJava operations
 * @param onNavigateBack Callback when back navigation is requested
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TreatmentsScreen(
    showExtendedBolusTab: Boolean,
    persistenceLayer: PersistenceLayer,
    profileUtil: ProfileUtil,
    profileFunction: ProfileFunction,
    activePlugin: ActivePlugin,
    rh: ResourceHelper,
    translator: Translator,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    uiInteraction: UiInteraction,
    userEntryPresentationHelper: UserEntryPresentationHelper,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers,
    onNavigateBack: () -> Unit
) {
    val iconColors = AapsTheme.elementColors
    var toolbarActions by remember { mutableStateOf<(@Composable RowScope.() -> Unit)?>(null) }

    // Define tabs with their icons and content
    val tabs = remember(showExtendedBolusTab) {
        buildList {
            add(
                TreatmentTab(
                    icon = Carbs,
                    titleRes = R.string.carbs_and_bolus,
                    colorGetter = { iconColors.bolusCarbs },
                    content = {
                        BolusCarbsScreen(
                            persistenceLayer = persistenceLayer,
                            profileFunction = profileFunction,
                            activePlugin = activePlugin,
                            rh = rh,
                            dateUtil = dateUtil,
                            decimalFormatter = decimalFormatter,
                            uiInteraction = uiInteraction,
                            rxBus = rxBus,
                            aapsSchedulers = aapsSchedulers,
                            setToolbarActions = { actions -> toolbarActions = actions }
                        )
                    }
                )
            )
            if (showExtendedBolusTab) {
                add(
                    TreatmentTab(
                        icon = ExtendedBolus,
                        titleRes = app.aaps.core.ui.R.string.extended_bolus,
                        colorGetter = { iconColors.extendedBolus },
                        content = {
                            ExtendedBolusScreen(
                                persistenceLayer = persistenceLayer,
                                profileFunction = profileFunction,
                                activeInsulin = activePlugin.activeInsulin,
                                rh = rh,
                                dateUtil = dateUtil,
                                uiInteraction = uiInteraction,
                                rxBus = rxBus,
                                aapsSchedulers = aapsSchedulers,
                                setToolbarActions = { actions -> toolbarActions = actions }
                            )
                        }
                    )
                )
            }
            add(
                TreatmentTab(
                    icon = TempBasal,
                    titleRes = app.aaps.core.ui.R.string.tempbasal_label,
                    colorGetter = { iconColors.tempBasal },
                    content = {
                        TempBasalScreen(
                            persistenceLayer = persistenceLayer,
                            profileFunction = profileFunction,
                            activePlugin = activePlugin,
                            rh = rh,
                            dateUtil = dateUtil,
                            decimalFormatter = decimalFormatter,
                            uiInteraction = uiInteraction,
                            rxBus = rxBus,
                            aapsSchedulers = aapsSchedulers,
                            setToolbarActions = { actions -> toolbarActions = actions }
                        )
                    }
                )
            )
            add(
                TreatmentTab(
                    icon = TempTarget,
                    titleRes = app.aaps.core.ui.R.string.temporary_target,
                    colorGetter = { iconColors.tempTarget },
                    content = {
                        TempTargetScreen(
                            persistenceLayer = persistenceLayer,
                            profileUtil = profileUtil,
                            rh = rh,
                            translator = translator,
                            dateUtil = dateUtil,
                            decimalFormatter = decimalFormatter,
                            uiInteraction = uiInteraction,
                            rxBus = rxBus,
                            aapsSchedulers = aapsSchedulers,
                            setToolbarActions = { actions -> toolbarActions = actions }
                        )
                    }
                )
            )
            add(
                TreatmentTab(
                    icon = ProfileSwitch,
                    titleRes = app.aaps.core.ui.R.string.careportal_profileswitch,
                    colorGetter = { iconColors.profileSwitch },
                    content = {
                        ProfileSwitchScreen(
                            persistenceLayer = persistenceLayer,
                            rh = rh,
                            dateUtil = dateUtil,
                            decimalFormatter = decimalFormatter,
                            uiInteraction = uiInteraction,
                            rxBus = rxBus,
                            aapsSchedulers = aapsSchedulers,
                            setToolbarActions = { actions -> toolbarActions = actions }
                        )
                    }
                )
            )
            add(
                TreatmentTab(
                    icon = Careportal,
                    titleRes = app.aaps.core.ui.R.string.careportal,
                    colorGetter = { iconColors.careportal },
                    content = {
                        CareportalScreen(
                            persistenceLayer = persistenceLayer,
                            profileUtil = profileUtil,
                            rh = rh,
                            translator = translator,
                            dateUtil = dateUtil,
                            uiInteraction = uiInteraction,
                            rxBus = rxBus,
                            aapsSchedulers = aapsSchedulers,
                            setToolbarActions = { actions -> toolbarActions = actions }
                        )
                    }
                )
            )
            add(
                TreatmentTab(
                    icon = RunningMode,
                    titleRes = app.aaps.core.ui.R.string.running_mode,
                    colorGetter = { iconColors.runningMode },
                    content = {
                        RunningModeScreen(
                            persistenceLayer = persistenceLayer,
                            rh = rh,
                            translator = translator,
                            dateUtil = dateUtil,
                            uiInteraction = uiInteraction,
                            rxBus = rxBus,
                            aapsSchedulers = aapsSchedulers,
                            setToolbarActions = { actions -> toolbarActions = actions }
                        )
                    }
                )
            )
            add(
                TreatmentTab(
                    icon = UserEntry,
                    titleRes = R.string.user_entry,
                    colorGetter = { iconColors.userEntry },
                    content = {
                        UserEntryScreen(
                            persistenceLayer = persistenceLayer,
                            rh = rh,
                            dateUtil = dateUtil,
                            userEntryPresentationHelper = userEntryPresentationHelper,
                            rxBus = rxBus,
                            aapsSchedulers = aapsSchedulers,
                            setToolbarActions = { actions -> toolbarActions = actions }
                        )
                    }
                )
            )
        }
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(app.aaps.core.ui.R.string.treatments)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                        )
                    }
                },
                actions = {
                    toolbarActions?.invoke(this)
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
                                tint = tab.colorGetter(),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(stringResource(tab.titleRes))
                        }
                    )
                }
            }

            // Pager with treatment screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                tabs[page].content()
            }
        }
    }
}

/**
 * Data class representing a treatment tab.
 *
 * @param icon The ImageVector icon for the tab
 * @param titleRes The string resource ID for the tab title
 * @param colorGetter Lambda function that returns the color for the tab icon from theme
 * @param content Composable content to display when this tab is selected
 */
private data class TreatmentTab(
    val icon: ImageVector,
    val titleRes: Int,
    val colorGetter: () -> Color,
    val content: @Composable () -> Unit
)
