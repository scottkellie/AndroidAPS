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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.aaps.core.data.model.TT
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventTempTargetChange
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.interfaces.utils.Translator
import app.aaps.core.objects.extensions.friendlyDescription
import app.aaps.core.objects.extensions.highValueToUnitsToString
import app.aaps.core.objects.extensions.lowValueToUnitsToString
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Composable screen displaying temporary targets with delete and show hidden functionality.
 *
 * @param persistenceLayer Database layer for temp target data
 * @param profileUtil Profile utility for unit conversion
 * @param rh Resource helper for string resources
 * @param translator Translator for temp target reasons
 * @param dateUtil Date utility for formatting dates and times
 * @param decimalFormatter Formatter for decimal values
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param rxBus RxBus for observing temp target changes
 * @param aapsSchedulers Schedulers for RxJava operations
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TempTargetScreen(
    persistenceLayer: PersistenceLayer,
    profileUtil: ProfileUtil,
    rh: ResourceHelper,
    translator: Translator,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    uiInteraction: UiInteraction,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers,
    setToolbarConfig: (ToolbarConfig) -> Unit,
    onNavigateBack: () -> Unit = { }
) {
    val context = LocalContext.current

    var tempTargets by remember { mutableStateOf<List<TT>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInvalidated by remember { mutableStateOf(false) }
    var isRemovingMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<TT>() }
    var refreshKey by remember { mutableStateOf(0) }

    val millsToThePast = T.days(30).msecs()

    // Load data
    LaunchedEffect(showInvalidated, refreshKey) {
        isLoading = true
        tempTargets = withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (showInvalidated) {
                persistenceLayer.getTemporaryTargetDataIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
            } else {
                persistenceLayer.getTemporaryTargetDataFromTime(now - millsToThePast, false).blockingGet()
            }
        }
        isLoading = false
    }

    // Subscribe to temp target changes
    DisposableEffect(Unit) {
        val disposable = CompositeDisposable()
        disposable += rxBus
            .toObservable(EventTempTargetChange::class.java)
            .observeOn(aapsSchedulers.io)
            .debounce(1L, TimeUnit.SECONDS)
            .subscribe {
                refreshKey++
            }

        onDispose {
            disposable.clear()
        }
    }

    val currentlyActiveTarget = remember(tempTargets) {
        persistenceLayer.getTemporaryTargetActiveAt(dateUtil.now())
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
                                        val tt = selectedItems[0]
                                        "${rh.gs(app.aaps.core.ui.R.string.temporary_target)}: ${tt.friendlyDescription(profileUtil.units, rh, profileUtil)}\n${dateUtil.dateAndTimeString(tt.timestamp)}"
                                    } else {
                                        rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                                    }

                                    uiInteraction.showOkCancelDialog(
                                        context = context,
                                        title = rh.gs(app.aaps.core.ui.R.string.removerecord),
                                        message = confirmationText,
                                        ok = {
                                            selectedItems.forEach { tt ->
                                                persistenceLayer.invalidateTemporaryTarget(
                                                    id = tt.id,
                                                    action = Action.TT_REMOVED,
                                                    source = Sources.Treatments,
                                                    note = null,
                                                    listValues = listOfNotNull(
                                                        ValueWithUnit.Timestamp(tt.timestamp),
                                                        ValueWithUnit.TETTReason(tt.reason),
                                                        ValueWithUnit.Mgdl(tt.lowTarget),
                                                        ValueWithUnit.Mgdl(tt.highTarget).takeIf { tt.lowTarget != tt.highTarget },
                                                        ValueWithUnit.Minute(TimeUnit.MILLISECONDS.toMinutes(tt.duration).toInt())
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

                    tempTargets.isEmpty() -> {
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
                                items = tempTargets,
                                key = { _, item -> item.id }
                            ) { index, tt ->
                                TempTargetItem(
                                    tempTarget = tt,
                                    isActive = tt.id == currentlyActiveTarget?.id,
                                    isFuture = tt.timestamp > dateUtil.now(),
                                    showDate = index == 0 || !dateUtil.isSameDayGroup(tt.timestamp, tempTargets[index - 1].timestamp),
                                    isRemovingMode = isRemovingMode,
                                    isSelected = selectedItems.contains(tt),
                                    onClick = {
                                        if (isRemovingMode && tt.isValid) {
                                            // Toggle selection
                                            if (selectedItems.contains(tt)) {
                                                selectedItems.remove(tt)
                                            } else {
                                                selectedItems.add(tt)
                                            }
                                        }
                                    },
                                    onLongPress = {
                                        if (tt.isValid && !isRemovingMode) {
                                            // Enter selection mode and select this item
                                            isRemovingMode = true
                                            selectedItems.clear()
                                            selectedItems.add(tt)
                                        }
                                    },
                                    profileUtil = profileUtil,
                                    rh = rh,
                                    translator = translator,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TempTargetItem(
    tempTarget: TT,
    isActive: Boolean,
    isFuture: Boolean,
    showDate: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    profileUtil: ProfileUtil,
    rh: ResourceHelper,
    translator: Translator,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    elementColors: app.aaps.core.ui.compose.ElementColors
) {
    val units = profileUtil.units

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
                    text = dateUtil.dateStringRelative(tempTarget.timestamp, rh),
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
                    text = dateUtil.timeRangeString(tempTarget.timestamp, tempTarget.end),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp,
                    color = when {
                        isActive -> Color(elementColors.tempTarget.value)
                        isFuture -> Color(0xFFFFAA00) // scheduled color
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                // Low target
                Text(
                    text = tempTarget.lowValueToUnitsToString(units, decimalFormatter),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Dash
                Text(
                    text = "-",
                    modifier = Modifier.padding(start = 5.dp),
                    fontSize = 14.sp
                )

                // High target
                Text(
                    text = tempTarget.highValueToUnitsToString(units, decimalFormatter),
                    modifier = Modifier.padding(start = 5.dp),
                    fontSize = 14.sp
                )

                // Duration
                Text(
                    text = rh.gs(app.aaps.core.ui.R.string.format_mins, T.msecs(tempTarget.duration).mins()),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // NS indicator
                if (tempTarget.ids.nightscoutId != null) {
                    Text(
                        text = "NS",
                        modifier = Modifier.padding(end = 10.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.tempTarget.value)
                    )
                }
            }

            // Reason row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(app.aaps.core.ui.R.string.reason),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                Text(
                    text = ":",
                    modifier = Modifier.padding(end = 5.dp),
                    fontSize = 14.sp
                )

                Text(
                    text = translator.translate(tempTarget.reason),
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Invalid indicator
                if (!tempTarget.isValid) {
                    Text(
                        text = stringResource(app.aaps.core.ui.R.string.invalid),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }

                // Checkbox for removal
                if (isRemovingMode && tempTarget.isValid) {
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
