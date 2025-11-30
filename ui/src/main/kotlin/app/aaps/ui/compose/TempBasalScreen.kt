package app.aaps.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import app.aaps.core.ui.compose.icons.Ns
import app.aaps.core.ui.compose.icons.Pump
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.aaps.core.data.model.EB
import app.aaps.core.data.model.TB
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.aps.IobTotal
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventTempBasalChange
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.objects.extensions.iobCalc
import app.aaps.core.objects.extensions.toStringFull
import app.aaps.core.objects.extensions.toTemporaryBasal
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Composable screen displaying temporary basals and extended boluses with delete and show hidden functionality.
 *
 * @param persistenceLayer Database layer for temp basal and extended bolus data
 * @param profileFunction Profile function for getting profiles
 * @param activePlugin Active plugin for checking pump capabilities
 * @param rh Resource helper for string resources
 * @param dateUtil Date utility for formatting dates and times
 * @param decimalFormatter Formatter for decimal values
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param rxBus RxBus for observing temp basal changes
 * @param aapsSchedulers Schedulers for RxJava operations
 * @param setToolbarConfig Callback to set the toolbar configuration
 * @param onNavigateBack Callback to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TempBasalScreen(
    persistenceLayer: PersistenceLayer,
    profileFunction: ProfileFunction,
    activePlugin: ActivePlugin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    uiInteraction: UiInteraction,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers,
    setToolbarConfig: (ToolbarConfig) -> Unit,
    onNavigateBack: () -> Unit = { }
) {
    val context = LocalContext.current

    var tempBasals by remember { mutableStateOf<List<TB>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInvalidated by remember { mutableStateOf(false) }
    var isRemovingMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<TB>() }
    var refreshKey by remember { mutableStateOf(0) }

    val millsToThePast = T.days(30).msecs()

    // Load data
    LaunchedEffect(showInvalidated, refreshKey) {
        isLoading = true
        tempBasals = withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val isFakingTempsByExtendedBoluses = activePlugin.activePump.isFakingTempsByExtendedBoluses

            val tempBasalsList = if (showInvalidated) {
                persistenceLayer.getTemporaryBasalsStartingFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
            } else {
                persistenceLayer.getTemporaryBasalsStartingFromTime(now - millsToThePast, false).blockingGet()
            }

            if (isFakingTempsByExtendedBoluses) {
                val extendedBolusList = if (showInvalidated) {
                    persistenceLayer.getExtendedBolusStartingFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                } else {
                    persistenceLayer.getExtendedBolusesStartingFromTime(now - millsToThePast, false).blockingGet()
                }

                val convertedExtendedBoluses = extendedBolusList.mapNotNull { eb ->
                    profileFunction.getProfile(eb.timestamp)?.let { profile ->
                        eb.toTemporaryBasal(profile)
                    }
                }

                (tempBasalsList + convertedExtendedBoluses).sortedByDescending { it.timestamp }
            } else {
                tempBasalsList.sortedByDescending { it.timestamp }
            }
        }
        isLoading = false
    }

    // Subscribe to temp basal changes
    DisposableEffect(Unit) {
        val disposable = CompositeDisposable()
        disposable += rxBus
            .toObservable(EventTempBasalChange::class.java)
            .observeOn(aapsSchedulers.io)
            .debounce(1L, TimeUnit.SECONDS)
            .subscribe {
                refreshKey++
            }

        onDispose {
            disposable.clear()
        }
    }

    // Update toolbar configuration whenever state changes
    LaunchedEffect(isRemovingMode, selectedItems.size) {
        setToolbarConfig(
            if (isRemovingMode) {
                // Selection mode: show count, close icon, and delete action
                ToolbarConfig(
                    title = rh.gs(app.aaps.core.ui.R.string.count_selected, selectedItems.size),
                    navigationIcon = {
                        IconButton(onClick = {
                            isRemovingMode = false
                            selectedItems.clear()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(app.aaps.core.ui.R.string.close)
                            )
                        }
                    },
                    actions = {
                        // Delete button
                        IconButton(
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    val confirmationText = if (selectedItems.size == 1) {
                                        val tempBasal = selectedItems[0]
                                        val isFakeExtended = tempBasal.type == TB.Type.FAKE_EXTENDED
                                        val profile = profileFunction.getProfile(dateUtil.now())
                                        if (profile != null) {
                                            "${if (isFakeExtended) rh.gs(app.aaps.core.ui.R.string.extended_bolus) else rh.gs(app.aaps.core.ui.R.string.tempbasal_label)}: ${
                                                tempBasal.toStringFull(
                                                    profile,
                                                    dateUtil,
                                                    rh
                                                )
                                            }\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(tempBasal.timestamp)}"
                                        } else {
                                            rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                                        }
                                    } else {
                                        rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                                    }

                                    uiInteraction.showOkCancelDialog(
                                        context = context,
                                        title = rh.gs(app.aaps.core.ui.R.string.removerecord),
                                        message = confirmationText,
                                        ok = {
                                            selectedItems.forEach { tempBasal ->
                                                val isFakeExtended = tempBasal.type == TB.Type.FAKE_EXTENDED
                                                if (isFakeExtended) {
                                                    val extendedBolus = persistenceLayer.getExtendedBolusActiveAt(tempBasal.timestamp)
                                                    if (extendedBolus != null) {
                                                        persistenceLayer.invalidateExtendedBolus(
                                                            id = extendedBolus.id,
                                                            action = Action.EXTENDED_BOLUS_REMOVED,
                                                            source = Sources.Treatments,
                                                            listValues = listOf(
                                                                ValueWithUnit.Timestamp(extendedBolus.timestamp),
                                                                ValueWithUnit.Insulin(extendedBolus.amount),
                                                                ValueWithUnit.UnitPerHour(extendedBolus.rate),
                                                                ValueWithUnit.Minute(TimeUnit.MILLISECONDS.toMinutes(extendedBolus.duration).toInt())
                                                            )
                                                        ).subscribe()
                                                    }
                                                } else {
                                                    persistenceLayer.invalidateTemporaryBasal(
                                                        id = tempBasal.id,
                                                        action = Action.TEMP_BASAL_REMOVED,
                                                        source = Sources.Treatments,
                                                        listValues = listOf(
                                                            ValueWithUnit.Timestamp(tempBasal.timestamp),
                                                            if (tempBasal.isAbsolute) ValueWithUnit.UnitPerHour(tempBasal.rate) else ValueWithUnit.Percent(tempBasal.rate.toInt()),
                                                            ValueWithUnit.Minute(T.msecs(tempBasal.duration).mins().toInt())
                                                        )
                                                    ).subscribe()
                                                }
                                            }
                                            selectedItems.clear()
                                            isRemovingMode = false
                                            refreshKey++
                                        }
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(app.aaps.core.ui.R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            } else {
                // Normal mode: show title, back icon, and show/hide action
                ToolbarConfig(
                    title = rh.gs(app.aaps.core.ui.R.string.treatments),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                            )
                        }
                    },
                    actions = {
                        // Show/Hide invalidated button
                        IconButton(onClick = { showInvalidated = !showInvalidated }) {
                            Icon(
                                imageVector = if (showInvalidated) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showInvalidated)
                                    stringResource(app.aaps.core.ui.R.string.hide_invalidated)
                                else
                                    stringResource(app.aaps.core.ui.R.string.show_invalidated)
                            )
                        }
                    }
                )
            }
        )
    }

    AapsTheme {
        val elementColors = AapsTheme.elementColors

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    tempBasals.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.no_records_available),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(50.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    else -> {
                        // Group items by day for sticky headers (optimized with derivedStateOf)
                        val groupedByDay by remember {
                            derivedStateOf {
                                tempBasals.groupBy { tb ->
                                    val timestamp = tb.timestamp
                                    dateUtil.dateString(timestamp)
                                }
                            }
                        }

                        val haptic = LocalHapticFeedback.current

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            groupedByDay.forEach { (dateString, itemsForDay) ->
                                stickyHeader(key = dateString) {
                                    Text(
                                        text = dateUtil.dateStringRelative(itemsForDay.first().timestamp, rh),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                items(
                                    items = itemsForDay,
                                    key = { item -> item.id }
                                ) { tb ->
                                    TempBasalItem(
                                        tempBasal = tb,
                                        isActive = tb.isInProgress,
                                        isFuture = tb.timestamp > dateUtil.now(),
                                        isRemovingMode = isRemovingMode,
                                        isSelected = selectedItems.contains(tb),
                                        onClick = {
                                            if (isRemovingMode && tb.isValid) {
                                                // Haptic feedback for selection toggle
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                // Toggle selection
                                                if (selectedItems.contains(tb)) {
                                                    selectedItems.remove(tb)
                                                } else {
                                                    selectedItems.add(tb)
                                                }
                                            }
                                        },
                                        onLongPress = {
                                            if (tb.isValid && !isRemovingMode) {
                                                // Haptic feedback for selection mode entry
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                // Enter selection mode and select this item
                                                isRemovingMode = true
                                                selectedItems.clear()
                                                selectedItems.add(tb)
                                            }
                                        },
                                        profileFunction = profileFunction,
                                        activePlugin = activePlugin,
                                        rh = rh,
                                        dateUtil = dateUtil,
                                        decimalFormatter = decimalFormatter,
                                        elementColors = elementColors
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TempBasalItem(
    tempBasal: TB,
    isActive: Boolean,
    isFuture: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    profileFunction: ProfileFunction,
    activePlugin: ActivePlugin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    elementColors: app.aaps.core.ui.compose.ElementColors
) {
    val now = dateUtil.now()
    val profile = profileFunction.getProfile(now)
    val iob = if (profile != null) {
        tempBasal.iobCalc(now, profile, activePlugin.activeInsulin)
    } else {
        IobTotal(now)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(1.dp)
        ) {
            // Main content row - time, rate, duration, flags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = if (isActive) {
                        dateUtil.timeString(tempBasal.timestamp)
                    } else {
                        dateUtil.timeRangeString(tempBasal.timestamp, tempBasal.end)
                    },
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 14.sp,
                    color = when {
                        isActive -> Color(elementColors.tempTarget.value)
                        isFuture -> Color(0xFFFFAA00) // scheduled color
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                // Rate
                Text(
                    text = if (tempBasal.isAbsolute) {
                        rh.gs(app.aaps.core.ui.R.string.pump_base_basal_rate, tempBasal.rate)
                    } else {
                        rh.gs(app.aaps.core.ui.R.string.format_percent, tempBasal.rate.toInt())
                    },
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Duration
                Text(
                    text = rh.gs(app.aaps.core.ui.R.string.format_mins, T.msecs(tempBasal.duration).mins()),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // Type flags
                if (tempBasal.type == TB.Type.FAKE_EXTENDED) {
                    Text(
                        text = "E",
                        modifier = Modifier.padding(start = 5.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.extendedBolus.value)
                    )
                }

                if (tempBasal.type == TB.Type.PUMP_SUSPEND) {
                    Text(
                        text = "S",
                        modifier = Modifier.padding(start = 5.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.extendedBolus.value)
                    )
                }

                if (tempBasal.type == TB.Type.EMULATED_PUMP_SUSPEND) {
                    Text(
                        text = "ES",
                        modifier = Modifier.padding(start = 5.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.extendedBolus.value)
                    )
                }

                if (tempBasal.type == TB.Type.SUPERBOLUS) {
                    Text(
                        text = "SB",
                        modifier = Modifier.padding(start = 5.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.extendedBolus.value)
                    )
                }

                // PH indicator (Pump History)
                if (tempBasal.ids.pumpId != null) {
                    Icon(
                        imageVector = Pump,
                        contentDescription = "Pump History",
                        modifier = Modifier
                            .size(21.dp)
                            .padding(start = 5.dp)
                    )
                }

                // NS indicator
                if (tempBasal.ids.nightscoutId != null) {
                    Icon(
                        imageVector = Ns,
                        contentDescription = "Nightscout",
                        modifier = Modifier
                            .size(21.dp)
                            .padding(start = 5.dp, end = 10.dp)
                    )
                }
            }

            // IOB row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rh.gs(R.string.tempbasals_iob_label_string),
                    modifier = Modifier.padding(start = 4.dp, end = 10.dp),
                    fontSize = 14.sp
                )

                Text(
                    text = rh.gs(app.aaps.core.ui.R.string.format_insulin_units, iob.basaliob),
                    modifier = Modifier.padding(end = 30.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (abs(iob.basaliob) > 0.01) {
                        Color(elementColors.tempTarget.value)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // Invalid indicator
                if (!tempBasal.isValid) {
                    Text(
                        text = stringResource(app.aaps.core.ui.R.string.invalid),
                        modifier = Modifier.padding(horizontal = 5.dp),
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }

                // Checkbox for removal
                if (isRemovingMode && tempBasal.isValid) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
