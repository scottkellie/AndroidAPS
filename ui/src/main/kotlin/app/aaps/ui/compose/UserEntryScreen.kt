package app.aaps.ui.compose

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import app.aaps.core.data.model.UE
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventNewHistoryData
import app.aaps.core.interfaces.userEntry.UserEntryPresentationHelper
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Composable screen displaying user entry log with optional loop records filtering.
 *
 * @param persistenceLayer Database layer for user entry data
 * @param rh Resource helper for string resources
 * @param dateUtil Date utility for formatting dates and times
 * @param userEntryPresentationHelper Helper for formatting user entry display
 * @param rxBus RxBus for observing history data changes
 * @param aapsSchedulers Schedulers for RxJava operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEntryScreen(
    persistenceLayer: PersistenceLayer,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    userEntryPresentationHelper: UserEntryPresentationHelper,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers
) {
    val context = LocalContext.current

    var userEntries by remember { mutableStateOf<List<UE>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showLoop by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    val millsToThePastFiltered = T.days(30).msecs()
    val millsToThePastUnFiltered = T.days(3).msecs()

    // Load data
    LaunchedEffect(showLoop, refreshKey) {
        isLoading = true
        userEntries = withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (showLoop) {
                persistenceLayer.getUserEntryDataFromTime(now - millsToThePastUnFiltered).blockingGet()
            } else {
                persistenceLayer.getUserEntryFilteredDataFromTime(now - millsToThePastFiltered).blockingGet()
            }
        }
        isLoading = false
    }

    // Subscribe to history data changes
    DisposableEffect(Unit) {
        val disposable = CompositeDisposable()
        disposable += rxBus
            .toObservable(EventNewHistoryData::class.java)
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show/Hide loop records button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = if (showLoop) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showLoop) "Hide loop records" else "Show loop records",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!showLoop) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.show_loop)) },
                                onClick = {
                                    showLoop = true
                                    showMenu = false
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.hide_loop)) },
                                onClick = {
                                    showLoop = false
                                    showMenu = false
                                }
                            )
                        }
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

                    userEntries.isEmpty() -> {
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
                                items = userEntries,
                                key = { _, item -> item.id }
                            ) { index, ue ->
                                UserEntryItem(
                                    userEntry = ue,
                                    showDate = index == 0 || !dateUtil.isSameDayGroup(ue.timestamp, userEntries[index - 1].timestamp),
                                    userEntryPresentationHelper = userEntryPresentationHelper,
                                    rh = rh,
                                    dateUtil = dateUtil
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
private fun UserEntryItem(
    userEntry: UE,
    showDate: Boolean,
    userEntryPresentationHelper: UserEntryPresentationHelper,
    rh: ResourceHelper,
    dateUtil: DateUtil
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
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
                    text = dateUtil.dateStringRelative(userEntry.timestamp, rh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Main content row: Time, Action, Source Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = dateUtil.timeStringWithSeconds(userEntry.timestamp),
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 14.sp
                )

                // Action - use AndroidView to display Spanned text with colors
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            textSize = 14f
                            setPadding(10.dpToPx(context), 0, 0, 0)
                        }
                    },
                    update = { textView ->
                        textView.text = userEntryPresentationHelper.actionToColoredString(userEntry.action)
                    },
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f)
                )

                // Source Icon
                Image(
                    painter = painterResource(id = userEntryPresentationHelper.iconId(userEntry.source)),
                    contentDescription = "Source: ${userEntry.source}",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
            }

            // Values row
            val valuesText = userEntryPresentationHelper.listToPresentationString(userEntry.values)
            if (valuesText.isNotEmpty()) {
                Text(
                    text = valuesText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 10.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Notes row
            if (userEntry.note.isNotEmpty()) {
                Text(
                    text = userEntry.note,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 10.dp, bottom = 5.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Helper function to convert dp to pixels
 */
private fun Int.dpToPx(context: android.content.Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}
