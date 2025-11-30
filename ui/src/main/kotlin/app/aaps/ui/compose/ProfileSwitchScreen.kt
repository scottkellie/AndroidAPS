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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventEffectiveProfileSwitchChanged
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.objects.extensions.getCustomizedName
import app.aaps.core.objects.profile.ProfileSealed
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Composable screen displaying profile switches with delete and show hidden functionality.
 *
 * @param persistenceLayer Database layer for profile switch data
 * @param rh Resource helper for string resources
 * @param dateUtil Date utility for formatting dates and times
 * @param decimalFormatter Formatter for decimal values
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param rxBus RxBus for observing profile switch changes
 * @param aapsSchedulers Schedulers for RxJava operations
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileSwitchScreen(
    persistenceLayer: PersistenceLayer,
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

    var profileSwitches by remember { mutableStateOf<List<ProfileSealed>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInvalidated by remember { mutableStateOf(false) }
    var isRemovingMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ProfileSealed>() }
    var refreshKey by remember { mutableStateOf(0) }

    val millsToThePast = T.days(30).msecs()

    // Load data
    LaunchedEffect(showInvalidated, refreshKey) {
        isLoading = true
        profileSwitches = withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val ps = if (showInvalidated) {
                persistenceLayer.getProfileSwitchesIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
            } else {
                persistenceLayer.getProfileSwitchesFromTime(now - millsToThePast, false).blockingGet()
            }
            val eps = if (showInvalidated) {
                persistenceLayer.getEffectiveProfileSwitchesIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
            } else {
                persistenceLayer.getEffectiveProfileSwitchesFromTime(now - millsToThePast, false).blockingGet()
            }
            (ps.map { ProfileSealed.PS(value = it, activePlugin = null) } +
             eps.map { ProfileSealed.EPS(value = it, activePlugin = null) })
                .sortedByDescending { it.timestamp }
        }
        isLoading = false
    }

    // Subscribe to profile switch changes
    DisposableEffect(Unit) {
        val disposable = CompositeDisposable()
        disposable += rxBus
            .toObservable(EventEffectiveProfileSwitchChanged::class.java)
            .observeOn(aapsSchedulers.io)
            .debounce(1L, TimeUnit.SECONDS)
            .subscribe {
                refreshKey++
            }

        onDispose {
            disposable.clear()
        }
    }

    val currentlyActiveProfile = remember(profileSwitches) {
        persistenceLayer.getEffectiveProfileSwitchActiveAt(dateUtil.now())
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
                                        val ps = selectedItems[0]
                                        "${rh.gs(app.aaps.core.ui.R.string.careportal_profileswitch)}: ${ps.profileName}\n${dateUtil.dateAndTimeString(ps.timestamp)}"
                                    } else {
                                        rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                                    }

                                    uiInteraction.showOkCancelDialog(
                                        context = context,
                                        title = rh.gs(app.aaps.core.ui.R.string.removerecord),
                                        message = confirmationText,
                                        ok = {
                                            selectedItems.forEach { profileSwitch ->
                                                if (profileSwitch is ProfileSealed.PS) {
                                                    persistenceLayer.invalidateProfileSwitch(
                                                        id = profileSwitch.id,
                                                        action = Action.PROFILE_SWITCH_REMOVED,
                                                        source = Sources.Treatments,
                                                        note = profileSwitch.profileName,
                                                        listValues = listOf(
                                                            ValueWithUnit.Timestamp(profileSwitch.timestamp)
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

                profileSwitches.isEmpty() -> {
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
                            profileSwitches.groupBy { ps ->
                                val timestamp = ps.timestamp
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
                            ) { profileSwitch ->
                                ProfileSwitchItem(
                                    profileSwitch = profileSwitch,
                                    isActive = profileSwitch.id == currentlyActiveProfile?.id,
                                    isFuture = profileSwitch.timestamp > dateUtil.now(),
                                    isRemovingMode = isRemovingMode,
                                    isSelected = selectedItems.contains(profileSwitch),
                                    onClick = {
                                        if (isRemovingMode && profileSwitch is ProfileSealed.PS && profileSwitch.isValid) {
                                            // Haptic feedback for selection toggle
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // Toggle selection
                                            if (selectedItems.contains(profileSwitch)) {
                                                selectedItems.remove(profileSwitch)
                                            } else {
                                                selectedItems.add(profileSwitch)
                                            }
                                        }
                                    },
                                    onLongPress = {
                                        if (profileSwitch is ProfileSealed.PS && profileSwitch.isValid && !isRemovingMode) {
                                            // Haptic feedback for selection mode entry
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // Enter selection mode and select this item
                                            isRemovingMode = true
                                            selectedItems.clear()
                                            selectedItems.add(profileSwitch)
                                        }
                                    },
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
private fun ProfileSwitchItem(
    profileSwitch: ProfileSealed,
    isActive: Boolean,
    isFuture: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    elementColors: app.aaps.core.ui.compose.ElementColors
) {
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
            // Main content row - Date and Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile helper indicator
                if (profileSwitch is ProfileSealed.EPS) {
                    Icon(
                        imageVector = Pump,
                        contentDescription = "Pump History",
                        modifier = Modifier
                            .size(21.dp)
                            .padding(end = 5.dp)
                    )
                }

                // Time
                Text(
                    text = dateUtil.timeString(profileSwitch.timestamp),
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 14.sp,
                    color = when {
                        isActive -> Color(elementColors.profileSwitch.value)
                        isFuture -> Color(0xFFFFAA00) // scheduled color
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                // Duration
                if (profileSwitch.duration != null && profileSwitch.duration != 0L) {
                    Text(
                        text = rh.gs(app.aaps.core.ui.R.string.format_mins, T.msecs(profileSwitch.duration ?: 0L).mins()),
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 14.sp
                    )
                }

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // NS indicator
                if (profileSwitch.ids?.nightscoutId != null) {
                    Icon(
                        imageVector = Ns,
                        contentDescription = "Nightscout",
                        modifier = Modifier
                            .size(21.dp)
                            .padding(start = 5.dp)
                    )
                }
            }

            // Profile name row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(app.aaps.core.ui.R.string.profile),
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 14.sp
                )

                Text(
                    text = ":",
                    modifier = Modifier.padding(end = 4.dp),
                    fontSize = 14.sp
                )

                val profileName = when (profileSwitch) {
                    is ProfileSealed.PS -> profileSwitch.value.getCustomizedName(decimalFormatter)
                    is ProfileSealed.EPS -> profileSwitch.value.originalCustomizedName
                    else -> profileSwitch.profileName
                }

                Text(
                    text = profileName,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Invalid indicator
                if (!profileSwitch.isValid) {
                    Text(
                        text = stringResource(app.aaps.core.ui.R.string.invalid),
                        modifier = Modifier.padding(start = 5.dp),
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }

                // Checkbox for removal
                if (isRemovingMode && profileSwitch is ProfileSealed.PS && profileSwitch.isValid) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Percentage and timeshift row (only for PS)
            if (profileSwitch is ProfileSealed.PS && (profileSwitch.value.percentage != 100 || profileSwitch.value.timeshift != 0L)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = " ",
                        modifier = Modifier.padding(start = 4.dp),
                        fontSize = 14.sp
                    )

                    if (profileSwitch.value.percentage != 100) {
                        Text(
                            text = "${profileSwitch.value.percentage}%",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp
                        )
                    }

                    if (profileSwitch.value.timeshift != 0L) {
                        Text(
                            text = "${T.msecs(profileSwitch.value.timeshift).hours()}h",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
