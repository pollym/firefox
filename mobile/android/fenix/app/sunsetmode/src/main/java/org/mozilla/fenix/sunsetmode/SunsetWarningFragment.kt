package org.mozilla.fenix.sunsetmode

import android.app.Dialog
import android.os.Bundle
import androidx.compose.material3.MaterialTheme
import androidx.fragment.app.DialogFragment
import androidx.fragment.compose.content
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SunsetWarningFragment : DialogFragment() {

    companion object {
        const val SUNSET_WARNING_KEY = "sunsetWarning"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(true)
            .setView(content {
                val sunsetWarning = this.arguments?.getString(SUNSET_WARNING_KEY)
                    ?: throw IllegalStateException("no warning")
                MaterialTheme {
                    SunsetWarningComposable(sunsetWarning)
                }
            })
            .create()

}