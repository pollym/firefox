/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.longfox

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LongFoxDataStore(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "longfox")
    private val hiscoreKey = intPreferencesKey("hiscore")

    fun hiscoreFlow(): Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[hiscoreKey] ?: 0
    }

    suspend fun saveIfHiscore(newScore: Int) {
        context.dataStore.updateData { preferences ->
            if (newScore <= (preferences[hiscoreKey] ?: 0))
                preferences
            else preferences.toMutablePreferences().also { preferences ->
                preferences[hiscoreKey] = newScore
            }
        }
    }
}
