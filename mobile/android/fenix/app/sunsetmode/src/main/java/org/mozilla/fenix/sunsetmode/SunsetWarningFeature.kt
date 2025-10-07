package org.mozilla.fenix.sunsetmode

import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

class SunsetWarningFeature() {

    var warningDialogShown = false
        internal set

    fun showDialog(container: ViewGroup, warning: SunsetWarning) {
        val context = container.context ?: return
        container.addView(
            ComposeView(context).apply {
                val warningString =
                    "We are about to raise the minimum supported android version to ${warning.upcomingRaiseVersion}!"
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    MaterialTheme {
                        val dialogVisible = remember { mutableStateOf (true) }
                        if (dialogVisible.value) {
                            SunsetWarningDialog (warningString) { dialogVisible.value = false }
                        }
                    }
                }
            }
        )
        warningDialogShown = true
    }
}