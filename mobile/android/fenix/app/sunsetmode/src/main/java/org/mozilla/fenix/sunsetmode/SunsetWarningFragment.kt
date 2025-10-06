package org.mozilla.fenix.sunsetmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SunsetWarningFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_sunset_warning, container, false)
        val sunsetWarning = arguments?.getString("sunsetWarning")
            ?: throw IllegalStateException("no warning")
//            val composeView = view as? ComposeView ?: throw IllegalStateException("no compose view")
//            composeView.apply {
//                setContent {
//                    MaterialTheme {
//                        SummaryComposable(summary)
//                    }
//                }
//            }
        return view
    }

}