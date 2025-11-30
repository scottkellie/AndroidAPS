package app.aaps.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.aaps.core.ui.compose.icons.Ns
import app.aaps.core.ui.compose.icons.Pump
import androidx.compose.ui.text.font.FontWeight
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(
                            items = extendedBoluses,
                            key = { _, item -> item.id }
                        ) { index, eb ->
                            ExtendedBolusItem(
                                extendedBolus = eb,
                                showDate = index == 0 || !dateUtil.isSameDayGroup(eb.timestamp, extendedBoluses[index - 1].timestamp),
                                isRemovingMode = isRemovingMode,
                                isSelected = selectedItems.contains(eb),
                                onClick = {
                                    if (isRemovingMode && eb.isValid) {
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
                                elementColors = elementColors
                            )
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
    showDate: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    profileFunction: ProfileFunction,
    activeInsulin: Insulin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    elementColors: app.aaps.core.ui.compose.ElementColors
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
            .padding(horizontal = 4.dp)
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
            modifier = Modifier.padding(2.dp)
        ) {
            // Date header
            if (showDate) {
                Text(
                    text = dateUtil.dateStringRelative(extendedBolus.timestamp, rh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Main content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = if (isActive) {
                        dateUtil.timeString(extendedBolus.timestamp)
                    } else {
                        dateUtil.timeRangeString(extendedBolus.timestamp, extendedBolus.end)
                    },
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp,
                    color = if (isActive) {
                        Color(elementColors.tempBasal.value)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                // Duration
                Text(
                    text = rh.gs(app.aaps.core.ui.R.string.format_mins, T.msecs(extendedBolus.duration).mins()),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // NS indicator
                if (extendedBolus.ids.nightscoutId != null) {
                    Icon(
                        imageVector = Ns,
                        contentDescription = "Nightscout",
                        modifier = Modifier
                            .size(21.dp)
                            .padding(end = 5.dp)
                    )
                }

                // Pump indicator
                if (extendedBolus.ids.pumpId != null) {
                    Icon(
                        imageVector = Pump,
                        contentDescription = "Pump History",
                        modifier = Modifier
                            .size(21.dp)
                            .padding(end = 10.dp)
                    )
                }
            }

            // Details row 1: Insulin and Ratio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Insulin amount
                Text(
                    text = rh.gs(app.aaps.core.ui.R.string.format_insulin_units, extendedBolus.amount),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Ratio label
                Text(
                    text = stringResource(R.string.tempbasals_netratio_label_string),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Ratio value
                Text(
                    text = rh.gs(app.aaps.core.ui.R.string.pump_base_basal_rate, extendedBolus.rate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Details row 2: IOB
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // IOB label
                Text(
                    text = stringResource(R.string.tempbasals_iob_label_string),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // IOB value
                if (iob != null) {
                    Text(
                        text = rh.gs(app.aaps.core.ui.R.string.format_insulin_units, iob.iob),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (iob.iob != 0.0) {
                            Color(elementColors.tempBasal.value)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                } else {
                    Text(
                        text = "-",
                        fontSize = 14.sp
                    )
                }

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // Invalid indicator
                if (!extendedBolus.isValid) {
                    Text(
                        text = stringResource(app.aaps.core.ui.R.string.invalid),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontSize = 14.sp,
                        color = Color.Red
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
}
