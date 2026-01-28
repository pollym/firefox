/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.firesnake

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun GameOverScreen(restartGame: () -> Unit) {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(Color.Red)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .background(Color.DarkGray)
                .padding(24.dp)
                .fillMaxSize()
                .clickable { restartGame() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                color = Color.White,
                text = "GAME OVER!!"
            )
        }
    }
}

@Preview
@Composable
fun GameOverScreenPreview() {
    GameOverScreen {}
}