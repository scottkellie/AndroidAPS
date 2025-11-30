package app.aaps.ui.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.ui.compose.TempBasalScreen
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TreatmentsTemporaryBasalsFragment : DaggerFragment() {

    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var profileFunction: ProfileFunction
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var aapsSchedulers: AapsSchedulers
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var decimalFormatter: DecimalFormatter
    @Inject lateinit var uiInteraction: UiInteraction

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                TempBasalScreen(
                    persistenceLayer = persistenceLayer,
                    profileFunction = profileFunction,
                    activePlugin = activePlugin,
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
