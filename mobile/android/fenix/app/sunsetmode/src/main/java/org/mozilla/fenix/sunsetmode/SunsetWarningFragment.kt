package org.mozilla.fenix.sunsetmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content

class SunsetWarningFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        val sunsetWarning = arguments?.getString("sunsetWarning")
            ?: throw IllegalStateException("no warning")
        MaterialTheme {
            SunsetWarningComposable(sunsetWarning)
        }
    }

}