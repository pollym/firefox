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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.CornerRadius.Companion.Zero
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import org.mozilla.fenix.firesnake.Direction.*

@Composable
fun GameCanvas(state: GameState, onSize: (Size, Offset) -> Unit) {
    val kitHeadDrawable = ContextCompat.getDrawable(LocalContext.current, R.drawable.kit_head)
    val kitTailDrawable = ContextCompat.getDrawable(LocalContext.current, R.drawable.kit_tail)
    val kitImageMargin = 0  // might need this to make the head stand out more

    val cellSize = state.cellSize.toInt()
    val kitHead = if (cellSize > 0) {
        kitHeadDrawable?.toBitmap(
            cellSize + kitImageMargin, cellSize + kitImageMargin
        )?.asImageBitmap()
    } else null

    val kitTail = if (cellSize > 0) {
        kitTailDrawable?.toBitmap(
            cellSize + kitImageMargin, cellSize + kitImageMargin
        )?.asImageBitmap()
    } else null

    val shouldersPath = Path()
    val bottomPath = Path()
    Canvas(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxSize()
    ) {
        onSize(size, center)
        drawHead(state, kitHead, kitImageMargin)
        drawBody(state, shouldersPath, bottomPath)
        drawTail(state, kitTail)
        drawFood(state)
    }
}

fun DrawScope.drawHead(state: GameState, kitHeadBitmap: ImageBitmap?, margin: Int) {
    if (kitHeadBitmap == null) return
    val head = state.snake.first()

    val topLeft = Offset(
        (head.x * state.cellSize) - margin, (head.y * state.cellSize) - margin
    )

    //rotating head is a bit Linda Blair
//    val rotateAngle = when (state.direction) {
//        UP -> 180F
//        DOWN -> 0F
//        LEFT -> 90F
//        RIGHT -> 270F
//    }
//    val pivotPoint = Offset(topLeft.x + state.cellSize / 2, topLeft.y + state.cellSize / 2)
//    rotate(rotateAngle, pivotPoint) {
    drawImage(
        image = kitHeadBitmap,
        topLeft = topLeft,
    )
//    }
}

fun DrawScope.drawBody(state: GameState, shouldersPath: Path, bottomPath: Path) {
    val brush = Brush.linearGradient(listOf(Color.Red, Color.Yellow))
    val snakeBody = state.snake.drop(1).dropLast(1)
    val cornerRadiusPx = state.cellSize/2
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    when (snakeBody.size) {
         1 -> {
             drawRoundRect(
                 brush = brush,
                 cornerRadius = cornerRadius,
                 topLeft = Offset(snakeBody.first().x * state.cellSize, snakeBody.first().y * state.cellSize),
                 size = Size(state.cellSize, state.cellSize)
             )
         }
        else -> {
            val shoulders = snakeBody.first()
            shouldersPath.apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(shoulders.x * state.cellSize, shoulders.y * state.cellSize),
                            size = Size(state.cellSize, state.cellSize)
                        ),
                        topLeft = when(state.shouldersDirection) {
                            UP, LEFT -> cornerRadius
                            DOWN, RIGHT -> Zero
                        },
                        topRight = when(state.shouldersDirection) {
                            UP, RIGHT -> cornerRadius
                            DOWN, LEFT -> Zero
                        },
                        bottomLeft = when(state.shouldersDirection) {
                            DOWN, LEFT -> cornerRadius
                            UP, RIGHT -> Zero
                        },
                        bottomRight = when(state.shouldersDirection) {
                            DOWN, RIGHT -> cornerRadius
                            UP, LEFT -> Zero
                        },
                    )
                )
            }
            drawPath(shouldersPath, brush)
//            drawRect(
//                brush = brush,
//                topLeft = Offset(shoulders.x * state.cellSize, shoulders.y * state.cellSize),
//                size = Size(state.cellSize, state.cellSize)
//            )


            snakeBody.drop(1).dropLast(1).forEach { (x, y) ->
                drawRect(
                    brush = brush,
                    topLeft = Offset(x * state.cellSize, y * state.cellSize),
                    size = Size(state.cellSize, state.cellSize)
                )
            }

            val bottom = snakeBody.last()
//            drawRect(
//                brush = brush,
//                topLeft = Offset(bottom.x * state.cellSize, bottom.y * state.cellSize),
//                size = Size(state.cellSize, state.cellSize)
//            )

            bottomPath.apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(bottom.x * state.cellSize, bottom.y * state.cellSize),
                            size = Size(state.cellSize, state.cellSize)
                        ),
                        topLeft = when(state.tailDirection) {
                            UP, LEFT -> cornerRadius
                            DOWN, RIGHT -> Zero
                        },
                        topRight = when(state.tailDirection) {
                            UP, RIGHT -> cornerRadius
                            DOWN, LEFT -> Zero
                        },
                        bottomLeft = when(state.tailDirection) {
                            DOWN, LEFT -> cornerRadius
                            UP, RIGHT -> Zero
                        },
                        bottomRight = when(state.tailDirection) {
                            DOWN, RIGHT -> cornerRadius
                            UP, LEFT -> Zero
                        },
                    )
                )
            }
            drawPath(bottomPath, brush)
        }
    }

}

fun DrawScope.drawTail(state: GameState, kitTailBitmap: ImageBitmap?) {
    if (kitTailBitmap == null) return
    val tail = state.snake.last()
    val rotateAngle = when (state.tailDirection) {
        UP -> 0F
        DOWN -> 180F
        LEFT -> 270F
        RIGHT -> 90F
    }
    val topLeft = Offset((tail.x * state.cellSize), (tail.y * state.cellSize))
    val pivotPoint = Offset(topLeft.x + state.cellSize / 2, topLeft.y + state.cellSize / 2)
    rotate(rotateAngle, pivotPoint) {
        drawImage(
            image = kitTailBitmap,
            topLeft = topLeft,
        )
    }
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
            GameState(size = Size(600f, 1000f)), onSize = { _, _ -> })
    }
}