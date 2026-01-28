/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.firesnake

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FireSnakeGameScreen() {
    var gameState by remember { mutableStateOf(GameState()) }
    val restartGame = { gameState = GameState() }
    val onSize: (Size, Offset) -> Unit = { size, offset -> gameState = gameState.onSized(size, offset) }
    val onTap: (Offset) -> Unit = { offset ->
        gameState = gameState.onTap(offset)
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
            .padding(horizontal = 32.dp, vertical = 128.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = onTap)
            },
        shape = RoundedCornerShape(32.dp),
    ) {
        LaunchedEffect(gameState) {
            while (!gameState.isGameOver) {
                delay(180L)
                gameState = gameState.moveSnake()
            }
        }
        if (gameState.isGameOver) {
            GameOverScreen(restartGame)
        }
        GameCanvas(gameState, onSize)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = "Score: ${gameState.score}",
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun FireSnakeGameScreenPreview() {
    MaterialTheme {
        FireSnakeGameScreen()
    }
}
