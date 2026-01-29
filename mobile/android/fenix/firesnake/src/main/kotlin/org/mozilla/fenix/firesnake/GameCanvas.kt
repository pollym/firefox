/*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/

package org.mozilla.fenix.firesnake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

@Composable
fun GameCanvas(state: GameState, onSize: (Size, Offset) -> Unit) {
    val kitHeadDrawable = ContextCompat.getDrawable(LocalContext.current, R.drawable.kit_head)
    val kitTailDrawable = ContextCompat.getDrawable(LocalContext.current, R.drawable.kit_tail)
    val kitImageMargin = 0  // might need this to make the head stand out more

    val cellSize = state.cellSize.toInt()
    val kitHead = if (cellSize > 0) {
        kitHeadDrawable?.toBitmap(
            cellSize + kitImageMargin,
            cellSize + kitImageMargin
        )?.asImageBitmap()
    } else null

    val kitTail = if (cellSize > 0) {
        kitTailDrawable?.toBitmap(
            cellSize + kitImageMargin,
            cellSize + kitImageMargin
        )?.asImageBitmap()
    } else null

    Canvas(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxSize()
    ) {
        onSize(size, center)
        drawHead(state, kitHead, kitImageMargin)
        drawBody(state)
        drawTail(state, kitTail)
        drawFood(state)
    }
}

fun DrawScope.drawHead(state: GameState, kitHeadBitmap: ImageBitmap?, kitHeadMargin: Int) {
    if (kitHeadBitmap == null) return
    val head = state.snake.first()
    drawImage(
        image = kitHeadBitmap,
        topLeft = Offset(
            (head.x * state.cellSize) - kitHeadMargin,
            (head.y * state.cellSize) - kitHeadMargin
        ),
    )
}

fun DrawScope.drawBody(state: GameState) {
    val brush = Brush.linearGradient(listOf(Color.Red, Color.Yellow))
    state.snake.drop(1).dropLast(1).forEach { (x, y) ->
        drawRect(
            brush = brush,
            topLeft = Offset(x * state.cellSize, y * state.cellSize),
            size = Size(state.cellSize, state.cellSize)
        )
    }
}

fun DrawScope.drawTail(state: GameState, kitTailBitmap: ImageBitmap?) {
    if (kitTailBitmap == null) return
    val tail = state.snake.last()
    drawRect(
        color = Color.White,
        topLeft = Offset(tail.x * state.cellSize, tail.y * state.cellSize),
        size = Size(state.cellSize, state.cellSize)
    )
}

fun DrawScope.drawFood(state: GameState) {
    drawRect(
        color = Color.Green,
        topLeft = Offset(state.food.x * state.cellSize, state.food.y * state.cellSize),
        size = Size(state.cellSize, state.cellSize)
    )
}

@Preview
@Composable
fun GameCanvasPreview() {
    MaterialTheme {
        GameCanvas(
            GameState(size = Size(600f, 1000f)),
            onSize = { _, _ -> }
        )
    }
}