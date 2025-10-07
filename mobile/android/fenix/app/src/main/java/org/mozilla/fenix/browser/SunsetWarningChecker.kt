package org.mozilla.fenix.browser

import android.os.Build
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.mozilla.fenix.sunsetmode.SunsetWarning
import org.mozilla.fenix.R

class SunsetWarningChecker {
    val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    init {
        with (remoteConfig) {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }

    fun checkWarnings(onWarning: (SunsetWarning) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            val upcomingMinSdk = remoteConfig["upcomingMinSdkVersion"].asLong().toInt()
            val upcomingRaiseDate = remoteConfig["upcomingRaiseDate"].asLong()
            Log.d("polly", "remote upcomingMinSdk = $upcomingMinSdk")
            Log.d("polly", "remote upcomingRaiseDate = $upcomingRaiseDate")
            if (Build.VERSION.SDK_INT <= upcomingMinSdk) {
                onWarning(
                    SunsetWarning(
                        upcomingRaiseVersion = upcomingMinSdk,
                        upcomingRaiseDate = upcomingRaiseDate
                    )
                )
            }
        }
    }
}
