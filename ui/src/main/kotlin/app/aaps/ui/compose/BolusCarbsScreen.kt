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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import app.aaps.core.ui.compose.icons.Calculator
import app.aaps.core.ui.compose.icons.Carbs
import app.aaps.core.ui.compose.icons.Ns
import app.aaps.core.ui.compose.icons.Prime
import app.aaps.core.ui.compose.icons.Pump
import app.aaps.core.ui.compose.icons.Smb
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
import app.aaps.core.ui.compose.GeneralColors
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    setToolbarConfig: (ToolbarConfig) -> Unit,
    onNavigateBack: () -> Unit = { }
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
        val generalColors = AapsTheme.generalColors

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
                        // Group items by day for sticky headers (optimized with derivedStateOf)
                        val groupedByDay by remember {
                            derivedStateOf {
                                mealLinks.groupBy { ml ->
                                    val timestamp = ml.bolusCalculatorResult?.timestamp ?: ml.bolus?.timestamp ?: ml.carbs?.timestamp ?: 0L
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
                                        text = dateUtil.dateStringRelative(
                                            itemsForDay.first().let { it.bolusCalculatorResult?.timestamp ?: it.bolus?.timestamp ?: it.carbs?.timestamp ?: 0L },
                                            rh
                                        ),
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
                                    key = { item ->
                                        item.bolus?.id ?: item.carbs?.id ?: item.bolusCalculatorResult?.id ?: 0L
                                    }
                                ) { ml ->
                                    MealLinkItem(
                                        mealLink = ml,
                                        isRemovingMode = isRemovingMode,
                                        isSelected = selectedItems.contains(ml),
                                        onClick = {
                                            if (isRemovingMode && ml.isValid()) {
                                                // Haptic feedback for selection toggle
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                // Toggle selection
                                                if (selectedItems.contains(ml)) {
                                                    selectedItems.remove(ml)
                                                } else {
                                                    selectedItems.add(ml)
                                                }
                                            }
                                        },
                                        onLongPress = {
                                            if (ml.isValid() && !isRemovingMode) {
                                                // Haptic feedback for selection mode entry
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                // Enter selection mode and select this item
                                                isRemovingMode = true
                                                selectedItems.clear()
                                                selectedItems.add(ml)
                                            }
                                        },
                                        profile = profile,
                                        activePlugin = activePlugin,
                                        rh = rh,
                                        dateUtil = dateUtil,
                                        decimalFormatter = decimalFormatter,
                                        generalColors = generalColors,
                                        showInvalidated = showInvalidated
                                    )
                                }
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
) {
    fun isValid(): Boolean {
        return (bolus?.isValid ?: true) && (carbs?.isValid ?: true) && (bolusCalculatorResult?.isValid ?: true)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MealLinkItem(
    mealLink: MealLink,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    profile: Profile?,
    activePlugin: ActivePlugin,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter,
    generalColors: GeneralColors,
    showInvalidated: Boolean
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

            // Bolus Calculator Result (Metadata)
            mealLink.bolusCalculatorResult?.let { bcr ->
                if (bcr.isValid || showInvalidated) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateUtil.timeString(bcr.timestamp),
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 14.sp
                        )

                        Box(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Calculator,
                            contentDescription = "Calculator",
                            modifier = Modifier.size(21.dp),
                            tint = Color(generalColors.calculator.value)
                        )

                        if (bcr.ids.nightscoutId != null) {
                            Icon(
                                imageVector = Ns,
                                contentDescription = "Nightscout",
                                modifier = Modifier
                                    .size(21.dp)
                                    .padding(start = 5.dp)
                            )
                        }

                        if (isRemovingMode && bcr.isValid) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onClick() },
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
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateUtil.timeString(bolus.timestamp),
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 14.sp,
                            color = if (bolus.timestamp > dateUtil.now()) Color(0xFFFFAA00) else MaterialTheme.colorScheme.onSurface
                        )

                        // Bolus amount with IOB
                        profile?.let { prof ->
                            val iob = bolus.iobCalc(activePlugin, System.currentTimeMillis(), prof.dia)
                            val bolusText = if (iob.iobContrib > 0.01) {
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(decimalFormatter.to2Decimal(bolus.amount))
                                        append("U ")
                                    }
                                    withStyle(style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(generalColors.activeInsulinText.value)
                                    )) {
                                        append("(")
                                        append(decimalFormatter.to2Decimal(iob.iobContrib))
                                        append("U)")
                                    }
                                }
                            } else {
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(decimalFormatter.to2Decimal(bolus.amount))
                                        append("U")
                                    }
                                }
                            }

                            Text(
                                text = bolusText,
                                modifier = Modifier.padding(start = 10.dp),
                                fontSize = 14.sp
                            )
                        } ?: run {
                            Text(
                                text = decimalFormatter.to2Decimal(bolus.amount) + "U",
                                modifier = Modifier.padding(start = 10.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.weight(1f))

                        // Bolus type
                        when (bolus.type) {
                            BS.Type.SMB -> {
                                Icon(
                                    imageVector = Smb,
                                    contentDescription = "SMB",
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            BS.Type.NORMAL -> {
                                Icon(
                                    imageVector = Carbs,
                                    contentDescription = "Meal",
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            BS.Type.PRIMING -> {
                                Icon(
                                    imageVector = Prime,
                                    contentDescription = "Prime",
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        if (bolus.ids.nightscoutId != null) {
                            Icon(
                                imageVector = Ns,
                                contentDescription = "Nightscout",
                                modifier = Modifier
                                    .size(21.dp)
                                    .padding(start = 5.dp)
                            )
                        }

                        if (bolus.ids.isPumpHistory()) {
                            Icon(
                                imageVector = Pump,
                                contentDescription = "Pump History",
                                modifier = Modifier
                                    .size(21.dp)
                                    .padding(start = 5.dp)
                            )
                        }

                        if (!bolus.isValid) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Invalid",
                                modifier = Modifier
                                    .size(21.dp)
                                    .padding(start = 5.dp),
                                tint = Color.Red
                            )
                        }

                        if (isRemovingMode && bolus.isValid) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onClick() },
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
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateUtil.timeString(carbs.timestamp),
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 14.sp
                        )

                        Text(
                            text = rh.gs(app.aaps.core.ui.R.string.carbs) + ":",
                            modifier = Modifier.padding(start = 8.dp, end = 4.dp),
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
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        carbs.notes?.takeIf { it.isNotEmpty() }?.let { notes ->
                            Text(
                                text = notes,
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Box(modifier = Modifier.weight(1f))

                        if (carbs.ids.nightscoutId != null) {
                            Icon(
                                imageVector = Ns,
                                contentDescription = "Nightscout",
                                modifier = Modifier
                                    .size(21.dp)
                                    .padding(start = 5.dp)
                            )
                        }

                        if (carbs.ids.isPumpHistory()) {
                            Icon(
                                imageVector = Pump,
                                contentDescription = "Pump History",
                                modifier = Modifier
                                    .size(21.dp)
                                    .padding(start = 5.dp)
                            )
                        }

                        if (!carbs.isValid) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Invalid",
                                modifier = Modifier
                                    .size(21.dp)
                                    .padding(start = 5.dp),
                                tint = Color.Red
                            )
                        }

                        if (isRemovingMode && carbs.isValid) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onClick() },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Bolus notes (carbs notes are shown inline)
            mealLink.bolus?.let { bolus ->
                bolus.notes?.takeIf { it.isNotEmpty() && mealLink.carbs == null }?.let { notes ->
                    Text(
                        text = notes,
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp, bottom = 3.dp),
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
