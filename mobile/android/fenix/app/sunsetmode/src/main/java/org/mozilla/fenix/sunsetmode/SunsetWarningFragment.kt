package org.mozilla.fenix.sunsetmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content

class SunsetWarningFragment : Fragment() {

    companion object {
        const val SUNSET_WARNING_KEY = "sunsetWarning"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  = content {
        val sunsetWarning = arguments?.getString("sunsetWarning")
            ?: throw IllegalStateException("no warning")
        MaterialTheme {
            val dialogVisible = remember { mutableStateOf(true) }
            when {
                dialogVisible.value -> {
                    SunsetWarningDialog(
                        sunsetWarning,
                        {
                            dialogVisible.value = false
                            this@SunsetWarningFragment.parentFragmentManager.popBackStack()
                        }
                    )
                }
            }
        }
    }

}