package app.aaps.ui.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.ui.compose.ProfileSwitchScreen
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TreatmentsProfileSwitchFragment : DaggerFragment() {

    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var aapsSchedulers: AapsSchedulers
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var decimalFormatter: DecimalFormatter
    @Inject lateinit var uiInteraction: UiInteraction

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ProfileSwitchScreen(
                    persistenceLayer = persistenceLayer,
                    rh = rh,
                    dateUtil = dateUtil,
                    decimalFormatter = decimalFormatter,
                    uiInteraction = uiInteraction,
                    rxBus = rxBus,
                    aapsSchedulers = aapsSchedulers,
                    setToolbarActions = { } // No-op for fragment usage
                )
            }
        }
    }
}
