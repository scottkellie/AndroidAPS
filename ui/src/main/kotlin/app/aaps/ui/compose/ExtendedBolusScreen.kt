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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.aaps.core.data.model.EB
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.insulin.Insulin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventExtendedBolusChange
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.objects.extensions.iobCalc
import app.aaps.core.objects.extensions.isInProgress
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Composable screen displaying extended boluses with delete and show hidden functionality.
 *
 * @param persistenceLayer Database layer for extended bolus data
 * @param profileFunction Profile function for getting profiles
 * @param activeInsulin Active insulin plugin for IOB calculation
 * @param rh Resource helper for string resources
 * @param dateUtil Date utility for formatting dates and times
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param rxBus RxBus for observing extended bolus changes
 * @param aapsSchedulers Schedulers for RxJava operations
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExtendedBolusScreen(
    persistenceLayer: PersistenceLayer,
    profileFunction: ProfileFunction,
    activeInsulin: Insulin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    uiInteraction: UiInteraction,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers,
    setToolbarConfig: (ToolbarConfig) -> Unit,
    onNavigateBack: () -> Unit = { }
) {
    val context = LocalContext.current

    var extendedBoluses by remember { mutableStateOf<List<EB>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInvalidated by remember { mutableStateOf(false) }
    var isRemovingMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<EB>() }
    var refreshKey by remember { mutableStateOf(0) }

    val millsToThePast = T.days(30).msecs()

    // Load data
    LaunchedEffect(showInvalidated, refreshKey) {
        isLoading = true
        extendedBoluses = withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (showInvalidated) {
                persistenceLayer.getExtendedBolusStartingFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
            } else {
                persistenceLayer.getExtendedBolusesStartingFromTime(now - millsToThePast, false).blockingGet()
            }
        }
        isLoading = false
    }

    // Subscribe to extended bolus changes
    DisposableEffect(Unit) {
        val disposable = CompositeDisposable()
        disposable += rxBus
            .toObservable(EventExtendedBolusChange::class.java)
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
                                        val eb = selectedItems[0]
                                        "${rh.gs(app.aaps.core.ui.R.string.extended_bolus)}\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(eb.timestamp)}"
                                    } else {
                                        rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                                    }

                                    uiInteraction.showOkCancelDialog(
                                        context = context,
                                        title = rh.gs(app.aaps.core.ui.R.string.removerecord),
                                        message = confirmationText,
                                        ok = {
                                            selectedItems.forEach { eb ->
                                                persistenceLayer.invalidateExtendedBolus(
                                                    id = eb.id,
                                                    action = Action.EXTENDED_BOLUS_REMOVED,
                                                    source = Sources.Treatments,
                                                    listValues = listOf(
                                                        ValueWithUnit.Timestamp(eb.timestamp),
                                                        ValueWithUnit.Insulin(eb.amount),
                                                        ValueWithUnit.UnitPerHour(eb.rate),
                                                        ValueWithUnit.Minute(TimeUnit.MILLISECONDS.toMinutes(eb.duration).toInt())
                                                    )
                                                ).subscribe()
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
        val generalColors = AapsTheme.generalColors

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                extendedBoluses.isEmpty() -> {
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
                            extendedBoluses.groupBy { eb ->
                                val timestamp = eb.timestamp
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
                            ) { eb ->
                                ExtendedBolusItem(
                                    extendedBolus = eb,
                                    isRemovingMode = isRemovingMode,
                                    isSelected = selectedItems.contains(eb),
                                    onClick = {
                                        if (isRemovingMode && eb.isValid) {
                                            // Haptic feedback for selection toggle
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // Toggle selection
                                            if (selectedItems.contains(eb)) {
                                                selectedItems.remove(eb)
                                            } else {
                                                selectedItems.add(eb)
                                            }
                                        }
                                    },
                                    onLongPress = {
                                        if (eb.isValid && !isRemovingMode) {
                                            // Haptic feedback for selection mode entry
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // Enter selection mode and select this item
                                            isRemovingMode = true
                                            selectedItems.clear()
                                            selectedItems.add(eb)
                                        }
                                    },
                                    profileFunction = profileFunction,
                                    activeInsulin = activeInsulin,
                                    rh = rh,
                                    dateUtil = dateUtil,
                                    elementColors = elementColors,
                                    generalColors = generalColors
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
private fun ExtendedBolusItem(
    extendedBolus: EB,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    profileFunction: ProfileFunction,
    activeInsulin: Insulin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    elementColors: app.aaps.core.ui.compose.ElementColors,
    generalColors: app.aaps.core.ui.compose.GeneralColors
) {
    val profile = profileFunction.getProfile(extendedBolus.timestamp)
    val iob = if (profile != null) {
        extendedBolus.iobCalc(System.currentTimeMillis(), profile, activeInsulin)
    } else {
        null
    }
    val isActive = extendedBolus.isInProgress(dateUtil)

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
        // Single row with all info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pump indicator (at start for EPS-style)
            if (extendedBolus.ids.pumpId != null) {
                Icon(
                    imageVector = Pump,
                    contentDescription = "Pump History",
                    modifier = Modifier
                        .size(21.dp)
                        .padding(end = 5.dp)
                )
            }

            // Time range, rate, IOB, duration - all in one compact format
            Text(
                text = buildAnnotatedString {
                    // Time range
                    append(
                        if (isActive) {
                            dateUtil.timeString(extendedBolus.timestamp)
                        } else {
                            dateUtil.timeRangeString(extendedBolus.timestamp, extendedBolus.end)
                        }
                    )
                    append(" ")
                    // Rate
                    val formattedRate = String.format("%.2f", extendedBolus.rate)
                    append(formattedRate)
                    append("U/h")
                    // IOB in blue color when != 0.0
                    if (iob != null && iob.iob != 0.0) {
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color(generalColors.activeInsulinText.value)
                            )
                        ) {
                            append("(")
                            val formattedIob = String.format("%.2f", iob.iob)
                            append(formattedIob)
                            append("U)")
                        }
                    }
                    append(" ")
                    // Duration
                    append(T.msecs(extendedBolus.duration).mins().toInt().toString())
                    append("min")
                },
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 14.sp,
                color = when {
                    isActive -> Color(elementColors.tempBasal.value)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Spacer
            Box(modifier = Modifier.weight(1f))

            // Invalid indicator
            if (!extendedBolus.isValid) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Invalid",
                    modifier = Modifier
                        .size(21.dp)
                        .padding(start = 5.dp),
                    tint = Color.Red
                )
            }

            // NS indicator
            if (extendedBolus.ids.nightscoutId != null) {
                Icon(
                    imageVector = Ns,
                    contentDescription = "Nightscout",
                    modifier = Modifier
                        .size(21.dp)
                        .padding(start = 5.dp)
                )
            }

            // Checkbox for removal
            if (isRemovingMode && extendedBolus.isValid) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
