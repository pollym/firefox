/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.longfox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.mozilla.fenix.longfox.GameState.Companion.CELL_SIZE_DP
import org.mozilla.fenix.longfox.GameState.Companion.FRAME_INTERVAL_TIME_MS

@Composable
fun NewGameScreen(
    numCells: Int,
    longFoxDataStore: LongFoxDataStore,
    startGame: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val hiscore by longFoxDataStore.hiscoreFlow()
        .collectAsState(initial = 0, coroutineScope.coroutineContext)

    var gameState by remember {
        mutableStateOf(
            GameState(
                numCells = numCells,
                demoMode = true
            )
        )
    }

    LaunchedEffect(gameState) {
        while (true) {
            delay(FRAME_INTERVAL_TIME_MS)
            gameState = gameState.moveFox()
        }
    }

    Box(
        modifier = Modifier
            .size((CELL_SIZE_DP * gameState.numCellsWide).dp)
            .background(Color.DarkGray)
            .clickable { startGame() },
    ) {
//        GameCanvas(
//            state = gameState, onSize = { size -> gameState = gameState.onSized(size) })
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fontSize = 16.sp,
                color = Color.Green,
                text = "tap anywhere to play !"
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                fontSize = 22.sp,
                color = Color.Yellow,
                text = "HISCORE: $hiscore"
            )
        }

    }

}

@Preview
@Composable
fun NewGameScreenPreview() {
    NewGameScreen(
        numCells = 20,
        longFoxDataStore = LongFoxDataStore(LocalContext.current),
        startGame = {},
    )
}
