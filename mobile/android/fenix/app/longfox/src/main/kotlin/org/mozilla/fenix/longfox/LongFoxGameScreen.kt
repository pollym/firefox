/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.longfox

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.mozilla.fenix.longfox.GameState.Companion.CELL_SIZE_DP
import org.mozilla.fenix.longfox.GameState.Companion.FRAME_INTERVAL_TIME_MS

@Composable
fun LongFoxGameScreen() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue),
    ) {
        val numCells = (minOf(maxWidth, maxHeight).value / CELL_SIZE_DP).toInt()
        var gameState by remember(numCells) { mutableStateOf(GameState(numCells = numCells)) }
        val restartGame = { gameState = GameState(numCells = numCells) }
        val onSize: (Size, Offset) -> Unit =
            { size, offset -> gameState = gameState.onSized(size, offset) }
        val density = LocalDensity.current.density
        val canvasSizePx = CELL_SIZE_DP * numCells * density
        val canvasOffsetXPx = (maxWidth.value * density - canvasSizePx) / 2f
        val canvasOffsetYPx = (maxHeight.value * density - canvasSizePx) / 2f
        val onTap by rememberUpdatedState { offset: Offset ->
            gameState = gameState.onTap(
                Offset(offset.x - canvasOffsetXPx, offset.y - canvasOffsetYPx)
            )
        }
        val soundEffectsPlayer = SoundEffectsPlayer(LocalContext.current)
        LaunchedEffect(gameState) {
            while (!gameState.isGameOver) {
                delay(FRAME_INTERVAL_TIME_MS)
                val oldScore = gameState.score
                gameState = gameState.moveFox()
                val newScore = gameState.score
                if (newScore > oldScore) {
                    soundEffectsPlayer.playSound(R.raw.eatfood)
                } else {
                    // beep boop
                    if (gameState.beepNext) {
                        soundEffectsPlayer.playSound(R.raw.beep)
                    } else {
                        soundEffectsPlayer.playSound(R.raw.boop)
                    }
                }
                gameState = gameState.toggleBeepNext()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onTap(it) })
                },
            contentAlignment = Alignment.Center,
        ) {
            if (gameState.isGameOver) {
                soundEffectsPlayer.playSound(R.raw.sadwobble)
                GameOverScreen(restartGame)
            } else {
                GameCanvas(gameState, onSize)
            }
        }
        if (!gameState.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "Score: ${gameState.score}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                )
            }
        }
    }
}

@Preview
@Composable
fun LongFoxGameScreenPreview() {
    MaterialTheme {
        LongFoxGameScreen()
    }
}
