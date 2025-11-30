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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitchScreen(
    persistenceLayer: PersistenceLayer,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    uiInteraction: UiInteraction,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(
                                items = profileSwitches,
                                key = { _, item -> item.id }
                            ) { index, profileSwitch ->
                                ProfileSwitchItem(
                                    profileSwitch = profileSwitch,
                                    isActive = profileSwitch.id == currentlyActiveProfile?.id,
                                    isFuture = profileSwitch.timestamp > dateUtil.now(),
                                    showDate = index == 0 || !dateUtil.isSameDayGroup(profileSwitch.timestamp, profileSwitches[index - 1].timestamp),
                                    isRemovingMode = isRemovingMode,
                                    isSelected = selectedItems.contains(profileSwitch),
                                    onSelectionChange = { selected ->
                                        if (selected) {
                                            selectedItems.add(profileSwitch)
                                        } else {
                                            selectedItems.remove(profileSwitch)
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

@Composable
private fun ProfileSwitchItem(
    profileSwitch: ProfileSealed,
    isActive: Boolean,
    isFuture: Boolean,
    showDate: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    elementColors: app.aaps.core.ui.compose.ElementColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .then(
                if (isRemovingMode && profileSwitch is ProfileSealed.PS && profileSwitch.isValid) {
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
                    text = dateUtil.dateStringRelative(profileSwitch.timestamp, rh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Main content row - Date and Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile helper indicator
                if (profileSwitch is ProfileSealed.EPS) {
                    Text(
                        text = "PH",
                        modifier = Modifier.padding(start = 10.dp, end = 5.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.profileSwitch.value),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Time
                Text(
                    text = dateUtil.timeString(profileSwitch.timestamp),
                    modifier = Modifier.padding(start = 10.dp),
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
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 14.sp
                    )
                }

                // Spacer
                Box(modifier = Modifier.weight(1f))

                // NS indicator
                if (profileSwitch.ids?.nightscoutId != null) {
                    Text(
                        text = "NS",
                        modifier = Modifier.padding(end = 10.dp),
                        fontSize = 14.sp,
                        color = Color(elementColors.profileSwitch.value)
                    )
                }
            }

            // Profile name row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(app.aaps.core.ui.R.string.profile),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                Text(
                    text = ":",
                    modifier = Modifier.padding(end = 5.dp),
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
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }

                // Checkbox for removal
                if (isRemovingMode && profileSwitch is ProfileSealed.PS && profileSwitch.isValid) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onSelectionChange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Percentage and timeshift row (only for PS)
            if (profileSwitch is ProfileSealed.PS && (profileSwitch.value.percentage != 100 || profileSwitch.value.timeshift != 0L)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = " ",
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 14.sp
                    )

                    if (profileSwitch.value.percentage != 100) {
                        Text(
                            text = "${profileSwitch.value.percentage}%",
                            modifier = Modifier.padding(start = 10.dp),
                            fontSize = 14.sp
                        )
                    }

                    if (profileSwitch.value.timeshift != 0L) {
                        Text(
                            text = "${T.msecs(profileSwitch.value.timeshift).hours()}h",
                            modifier = Modifier.padding(start = 10.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
