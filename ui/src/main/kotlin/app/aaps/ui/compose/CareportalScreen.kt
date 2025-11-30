package app.aaps.ui.compose

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import app.aaps.core.data.model.TE
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventTherapyEventChange
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.Translator
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Composable screen displaying therapy events (careportal entries) with delete and show hidden functionality.
 *
 * @param persistenceLayer Database layer for therapy event data
 * @param profileUtil Profile utility for unit conversion
 * @param rh Resource helper for string resources
 * @param translator Translator for therapy event types
 * @param dateUtil Date utility for formatting dates and times
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param rxBus RxBus for observing therapy event changes
 * @param aapsSchedulers Schedulers for RxJava operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareportalScreen(
    persistenceLayer: PersistenceLayer,
    profileUtil: ProfileUtil,
    rh: ResourceHelper,
    translator: Translator,
    dateUtil: DateUtil,
    uiInteraction: UiInteraction,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers
) {
    val context = LocalContext.current

    var therapyEvents by remember { mutableStateOf<List<TE>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInvalidated by remember { mutableStateOf(false) }
    var isRemovingMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<TE>() }
    var refreshKey by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    val millsToThePast = T.days(30).msecs()

    // Load data
    LaunchedEffect(showInvalidated, refreshKey) {
        isLoading = true
        therapyEvents = withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (showInvalidated) {
                persistenceLayer.getTherapyEventDataIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
            } else {
                persistenceLayer.getTherapyEventDataFromTime(now - millsToThePast, false).blockingGet()
            }
        }
        isLoading = false
    }

    // Subscribe to therapy event changes
    DisposableEffect(Unit) {
        val disposable = CompositeDisposable()
        disposable += rxBus
            .toObservable(EventTherapyEventChange::class.java)
            .observeOn(aapsSchedulers.io)
            .debounce(1L, TimeUnit.SECONDS)
            .subscribe {
                refreshKey++
            }

        onDispose {
            disposable.clear()
        }
    }

    AapsTheme {
        val elementColors = AapsTheme.elementColors

        Column(modifier = Modifier.fillMaxSize()) {
            // Action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show/Hide invalidated button
                IconButton(onClick = { showInvalidated = !showInvalidated }) {
                    Icon(
                        imageVector = if (showInvalidated) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showInvalidated) "Hide invalidated" else "Show invalidated",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete button
                IconButton(
                    onClick = {
                        if (isRemovingMode) {
                            // Confirm and remove
                            if (selectedItems.isNotEmpty()) {
                                val confirmationText = if (selectedItems.size == 1) {
                                    val te = selectedItems[0]
                                    "${rh.gs(app.aaps.core.ui.R.string.event_type)}: ${translator.translate(te.type)}\n" +
                                        "${rh.gs(app.aaps.core.ui.R.string.notes_label)}: ${te.note ?: ""}\n" +
                                        "${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(te.timestamp)}"
                                } else {
                                    rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                                }

                                uiInteraction.showOkCancelDialog(
                                    context = context,
                                    title = rh.gs(app.aaps.core.ui.R.string.removerecord),
                                    message = confirmationText,
                                    ok = {
                                        selectedItems.forEach { te ->
                                            persistenceLayer.invalidateTherapyEvent(
                                                id = te.id,
                                                action = Action.CAREPORTAL_REMOVED,
                                                source = Sources.Treatments,
                                                note = te.note,
                                                listValues = listOf(
                                                    ValueWithUnit.Timestamp(te.timestamp),
                                                    ValueWithUnit.TEType(te.type)
                                                )
                                            ).subscribe()
                                        }
                                        selectedItems.clear()
                                        isRemovingMode = false
                                        refreshKey++
                                    }
                                )
                            }
                        } else {
                            // Enter removing mode
                            isRemovingMode = true
                            selectedItems.clear()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove items",
                        tint = if (isRemovingMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                // Menu button (for remove started events)
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.careportal_remove_started_events)) },
                            onClick = {
                                showMenu = false
                                uiInteraction.showOkCancelDialog(
                                    context = context,
                                    title = rh.gs(app.aaps.core.ui.R.string.careportal),
                                    message = rh.gs(R.string.careportal_remove_started_events),
                                    ok = {
                                        persistenceLayer.invalidateTherapyEventsWithNote(
                                            rh.gs(app.aaps.core.ui.R.string.androidaps_start),
                                            Action.RESTART_EVENTS_REMOVED,
                                            Sources.Treatments
                                        ).subscribe()
                                        refreshKey++
                                    }
                                )
                            }
                        )
                    }
                }

                // Cancel button when in removing mode
                if (isRemovingMode) {
                    IconButton(onClick = {
                        isRemovingMode = false
                        selectedItems.clear()
                    }) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    therapyEvents.isEmpty() -> {
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
                                items = therapyEvents,
                                key = { _, item -> item.id }
                            ) { index, te ->
                                TherapyEventItem(
                                    therapyEvent = te,
                                    showDate = index == 0 || !dateUtil.isSameDayGroup(te.timestamp, therapyEvents[index - 1].timestamp),
                                    isRemovingMode = isRemovingMode,
                                    isSelected = selectedItems.contains(te),
                                    onSelectionChange = { selected ->
                                        if (selected) {
                                            selectedItems.add(te)
                                        } else {
                                            selectedItems.remove(te)
                                        }
                                    },
                                    profileUtil = profileUtil,
                                    rh = rh,
                                    translator = translator,
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
}

@Composable
private fun TherapyEventItem(
    therapyEvent: TE,
    showDate: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    profileUtil: ProfileUtil,
    rh: ResourceHelper,
    translator: Translator,
    dateUtil: DateUtil,
    elementColors: app.aaps.core.ui.compose.ElementColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .then(
                if (isRemovingMode) {
                    Modifier.clickable { onSelectionChange(!isSelected) }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(2.dp)
        ) {
            // Date header
            if (showDate) {
                Text(
                    text = dateUtil.dateStringRelative(therapyEvent.timestamp, rh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Time and Type row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = dateUtil.timeString(therapyEvent.timestamp),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Event type
                Text(
                    text = translator.translate(therapyEvent.type),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // NS indicator
                if (therapyEvent.ids.nightscoutId != null) {
                    Text(
                        text = "NS",
                        modifier = Modifier.padding(end = 10.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.tempTarget.value)
                    )
                }

                // Invalid indicator
                if (!therapyEvent.isValid) {
                    Text(
                        text = stringResource(app.aaps.core.ui.R.string.invalid),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }
            }

            // Duration, BG, and Note row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duration
                if (therapyEvent.duration != 0L) {
                    Text(
                        text = dateUtil.niceTimeScalar(therapyEvent.duration, rh),
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 14.sp
                    )
                }

                // BG value (for FINGER_STICK_BG_VALUE)
                if (therapyEvent.type == TE.Type.FINGER_STICK_BG_VALUE && therapyEvent.glucose != null) {
                    Text(
                        text = profileUtil.stringInCurrentUnitsDetect(therapyEvent.glucose!!),
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 14.sp
                    )
                }

                // Note
                if (!therapyEvent.note.isNullOrEmpty()) {
                    Text(
                        text = therapyEvent.note!!,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .weight(1f),
                        fontSize = 14.sp
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }

                // Checkbox for removal
                if (isRemovingMode && therapyEvent.isValid) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onSelectionChange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
