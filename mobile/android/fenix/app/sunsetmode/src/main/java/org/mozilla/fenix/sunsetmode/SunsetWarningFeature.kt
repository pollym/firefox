package org.mozilla.fenix.sunsetmode

import android.util.Log

class SunsetWarningFeature() {
    init {
        Log.d("polly","hello from the new module")
    }

    fun showWarning(warning: SunsetWarning) {
        Log.d("polly","lets show a warning")
    }
}