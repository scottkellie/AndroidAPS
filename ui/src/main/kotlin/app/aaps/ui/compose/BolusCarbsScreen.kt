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
import androidx.compose.foundation.layout.RowScope
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
import app.aaps.core.data.model.BCR
import app.aaps.core.data.model.BS
import app.aaps.core.data.model.CA
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.Profile
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventTreatmentChange
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.objects.extensions.iobCalc
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Composable screen displaying boluses and carbs in a combined list.
 *
 * @param persistenceLayer Database layer for bolus and carbs data
 * @param profileFunction Profile function for profile data
 * @param activePlugin Active plugin for IOB calculations
 * @param rh Resource helper for string resources
 * @param dateUtil Date utility for formatting dates and times
 * @param decimalFormatter Formatter for decimal values
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param rxBus RxBus for observing treatment changes
 * @param aapsSchedulers Schedulers for RxJava operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolusCarbsScreen(
    persistenceLayer: PersistenceLayer,
    profileFunction: ProfileFunction,
    activePlugin: ActivePlugin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    uiInteraction: UiInteraction,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers,
    setToolbarActions: (@Composable RowScope.() -> Unit) -> Unit
) {
    val context = LocalContext.current

    var mealLinks by remember { mutableStateOf<List<MealLink>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInvalidated by remember { mutableStateOf(false) }
    var isRemovingMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<MealLink>() }
    var refreshKey by remember { mutableStateOf(0) }

    val millsToThePast = T.days(30).msecs()

    // Load data
    LaunchedEffect(showInvalidated, refreshKey) {
        isLoading = true
        mealLinks = withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()

            val boluses = if (showInvalidated) {
                persistenceLayer.getBolusesFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                    .map { MealLink(bolus = it) }
            } else {
                persistenceLayer.getBolusesFromTime(now - millsToThePast, false).blockingGet()
                    .map { MealLink(bolus = it) }
            }

            val carbs = if (showInvalidated) {
                persistenceLayer.getCarbsFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                    .map { MealLink(carbs = it) }
            } else {
                persistenceLayer.getCarbsFromTime(now - millsToThePast, false).blockingGet()
                    .map { MealLink(carbs = it) }
            }

            val calcs = if (showInvalidated) {
                persistenceLayer.getBolusCalculatorResultsIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
                    .map { MealLink(bolusCalculatorResult = it) }
            } else {
                persistenceLayer.getBolusCalculatorResultsFromTime(now - millsToThePast, false).blockingGet()
                    .map { MealLink(bolusCalculatorResult = it) }
            }

            (boluses + carbs + calcs).sortedByDescending {
                it.bolusCalculatorResult?.timestamp ?: it.bolus?.timestamp ?: it.carbs?.timestamp ?: 0L
            }
        }
        isLoading = false
    }

    // Subscribe to treatment changes
    DisposableEffect(Unit) {
        val disposable = CompositeDisposable()
        disposable += rxBus
            .toObservable(EventTreatmentChange::class.java)
            .observeOn(aapsSchedulers.io)
            .debounce(1L, TimeUnit.SECONDS)
            .subscribe {
                refreshKey++
            }

        onDispose {
            disposable.clear()
        }
    }

    val profile = remember(refreshKey) { profileFunction.getProfile() }

    // Update toolbar actions whenever state changes
    SideEffect {
        setToolbarActions {
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
                                val ml = selectedItems[0]
                                val bolus = ml.bolus
                                if (bolus != null) {
                                    "${rh.gs(app.aaps.core.ui.R.string.configbuilder_insulin)}: ${rh.gs(app.aaps.core.ui.R.string.format_insulin_units, bolus.amount)}\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(bolus.timestamp)}"
                                } else {
                                    val carbs = ml.carbs
                                    if (carbs != null) {
                                        "${rh.gs(app.aaps.core.ui.R.string.carbs)}: ${rh.gs(app.aaps.core.objects.R.string.format_carbs, carbs.amount.toInt())}\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(carbs.timestamp)}"
                                    } else {
                                        rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                                    }
                                }
                            } else {
                                rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selectedItems.size)
                            }

                            uiInteraction.showOkCancelDialog(
                                context = context,
                                title = rh.gs(app.aaps.core.ui.R.string.removerecord),
                                message = confirmationText,
                                ok = {
                                    selectedItems.forEach { ml ->
                                        ml.bolus?.let { bolus ->
                                            persistenceLayer.invalidateBolus(
                                                bolus.id,
                                                action = Action.BOLUS_REMOVED,
                                                source = Sources.Treatments,
                                                listValues = listOf(
                                                    ValueWithUnit.Timestamp(bolus.timestamp),
                                                    ValueWithUnit.Insulin(bolus.amount)
                                                )
                                            ).subscribe()
                                        }
                                        ml.carbs?.let { carb ->
                                            persistenceLayer.invalidateCarbs(
                                                carb.id,
                                                action = Action.CARBS_REMOVED,
                                                source = Sources.Treatments,
                                                listValues = listOf(
                                                    ValueWithUnit.Timestamp(carb.timestamp),
                                                    ValueWithUnit.Gram(carb.amount.toInt())
                                                )
                                            ).subscribe()
                                        }
                                        ml.bolusCalculatorResult?.let { bolusCalculatorResult ->
                                            persistenceLayer.invalidateBolusCalculatorResult(
                                                bolusCalculatorResult.id,
                                                action = Action.BOLUS_CALCULATOR_RESULT_REMOVED,
                                                source = Sources.Treatments,
                                                listValues = listOf(ValueWithUnit.Timestamp(bolusCalculatorResult.timestamp))
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

                    mealLinks.isEmpty() -> {
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
                                items = mealLinks,
                                key = { _, item ->
                                    item.bolus?.id ?: item.carbs?.id ?: item.bolusCalculatorResult?.id ?: 0L
                                }
                            ) { index, ml ->
                                MealLinkItem(
                                    mealLink = ml,
                                    showDate = index == 0 || !dateUtil.isSameDayGroup(
                                        ml.bolusCalculatorResult?.timestamp ?: ml.bolus?.timestamp ?: ml.carbs?.timestamp ?: 0L,
                                        mealLinks[index - 1].bolusCalculatorResult?.timestamp ?: mealLinks[index - 1].bolus?.timestamp ?: mealLinks[index - 1].carbs?.timestamp ?: 0L
                                    ),
                                    isRemovingMode = isRemovingMode,
                                    isSelected = selectedItems.contains(ml),
                                    onSelectionChange = { selected ->
                                        if (selected) {
                                            selectedItems.add(ml)
                                        } else {
                                            selectedItems.remove(ml)
                                        }
                                    },
                                    profile = profile,
                                    activePlugin = activePlugin,
                                    rh = rh,
                                    dateUtil = dateUtil,
                                    decimalFormatter = decimalFormatter,
                                    elementColors = elementColors,
                                    showInvalidated = showInvalidated
                                )
                            }
                        }
                    }
                }
            }
        }
}

data class MealLink(
    val bolus: BS? = null,
    val carbs: CA? = null,
    val bolusCalculatorResult: BCR? = null
)

@Composable
private fun MealLinkItem(
    mealLink: MealLink,
    showDate: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    profile: Profile?,
    activePlugin: ActivePlugin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    elementColors: app.aaps.core.ui.compose.ElementColors,
    showInvalidated: Boolean
) {
    val timestamp = mealLink.bolusCalculatorResult?.timestamp ?: mealLink.bolus?.timestamp ?: mealLink.carbs?.timestamp ?: 0L

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
                    text = dateUtil.dateStringRelative(timestamp, rh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bolus Calculator Result (Metadata)
            mealLink.bolusCalculatorResult?.let { bcr ->
                if (bcr.isValid || showInvalidated) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateUtil.timeString(bcr.timestamp),
                            modifier = Modifier.padding(start = 5.dp),
                            fontSize = 14.sp
                        )

                        Box(modifier = Modifier.weight(1f))

                        Text(
                            text = rh.gs(R.string.calculation_short),
                            fontSize = 14.sp,
                            color = Color(0xFF2196F3) // colorCalculatorButton
                        )

                        if (bcr.ids.nightscoutId != null) {
                            Text(
                                text = "NS",
                                modifier = Modifier.padding(start = 5.dp),
                                fontSize = 14.sp,
                                color = Color(elementColors.tempTarget.value)
                            )
                        }

                        if (isRemovingMode && bcr.isValid) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = onSelectionChange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Bolus
            mealLink.bolus?.let { bolus ->
                if (bolus.isValid || showInvalidated) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateUtil.timeString(bolus.timestamp),
                            modifier = Modifier.padding(start = 5.dp),
                            fontSize = 14.sp,
                            color = if (bolus.timestamp > dateUtil.now()) Color(0xFFFFAA00) else MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = rh.gs(app.aaps.core.ui.R.string.format_insulin_units, bolus.amount),
                            modifier = Modifier.padding(start = 10.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // IOB
                        profile?.let { prof ->
                            val iob = bolus.iobCalc(activePlugin, System.currentTimeMillis(), prof.dia)
                            if (iob.iobContrib > 0.01) {
                                Text(
                                    text = "IOB:",
                                    modifier = Modifier.padding(start = 10.dp, end = 5.dp),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = rh.gs(app.aaps.core.ui.R.string.format_insulin_units, iob.iobContrib),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(elementColors.tempTarget.value)
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f))

                        // Bolus type
                        Text(
                            text = when (bolus.type) {
                                BS.Type.SMB -> "SMB"
                                BS.Type.NORMAL -> rh.gs(R.string.meal_bolus)
                                BS.Type.PRIMING -> rh.gs(R.string.prime)
                            },
                            fontSize = 14.sp
                        )

                        if (bolus.ids.nightscoutId != null) {
                            Text(
                                text = "NS",
                                modifier = Modifier.padding(start = 5.dp),
                                fontSize = 14.sp,
                                color = Color(elementColors.tempTarget.value)
                            )
                        }

                        if (bolus.ids.isPumpHistory()) {
                            Text(
                                text = "PH",
                                modifier = Modifier.padding(start = 5.dp),
                                fontSize = 14.sp,
                                color = Color(elementColors.tempTarget.value)
                            )
                        }

                        if (!bolus.isValid) {
                            Text(
                                text = stringResource(app.aaps.core.ui.R.string.invalid),
                                modifier = Modifier.padding(start = 5.dp),
                                fontSize = 14.sp,
                                color = Color.Red
                            )
                        }

                        if (isRemovingMode && bolus.isValid) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = onSelectionChange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Carbs
            mealLink.carbs?.let { carbs ->
                if (carbs.isValid || showInvalidated) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateUtil.timeString(carbs.timestamp),
                            modifier = Modifier.padding(start = 5.dp),
                            fontSize = 14.sp
                        )

                        Text(
                            text = rh.gs(app.aaps.core.ui.R.string.carbs) + ":",
                            modifier = Modifier.padding(start = 10.dp, end = 5.dp),
                            fontSize = 14.sp
                        )

                        Text(
                            text = rh.gs(app.aaps.core.objects.R.string.format_carbs, carbs.amount.toInt()),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (carbs.duration > 0) {
                            Text(
                                text = rh.gs(app.aaps.core.ui.R.string.format_mins, T.msecs(carbs.duration).mins().toInt()),
                                modifier = Modifier.padding(start = 10.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.weight(1f))

                        if (carbs.ids.nightscoutId != null) {
                            Text(
                                text = "NS",
                                modifier = Modifier.padding(start = 5.dp),
                                fontSize = 14.sp,
                                color = Color(elementColors.tempTarget.value)
                            )
                        }

                        if (carbs.ids.isPumpHistory()) {
                            Text(
                                text = "PH",
                                modifier = Modifier.padding(start = 5.dp),
                                fontSize = 14.sp,
                                color = Color(elementColors.tempTarget.value)
                            )
                        }

                        if (!carbs.isValid) {
                            Text(
                                text = stringResource(app.aaps.core.ui.R.string.invalid),
                                modifier = Modifier.padding(start = 5.dp),
                                fontSize = 14.sp,
                                color = Color.Red
                            )
                        }

                        if (isRemovingMode && carbs.isValid) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = onSelectionChange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Notes
            val notes = mealLink.carbs?.notes ?: mealLink.bolus?.notes ?: ""
            if (notes.isNotEmpty()) {
                Text(
                    text = notes,
                    modifier = Modifier.padding(start = 20.dp, end = 10.dp, bottom = 5.dp),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
