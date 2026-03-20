/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.longfox

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Draw the body of the fox at the position given by the current game state.
 * We pass in some path objects because they're a bit expensive and we want this code to be fast.
 * The shoulders and the... square before the tail are special because they have rounded corners
 * to make the fox cuter
 *
 * @receiver the draw scope for the game canvas
 * @param state the current game state
 * @param shouldersPath the fox's shoulders
 * @param bottomPath the fox's bottom
 */
fun DrawScope.drawBody(state: GameState, shouldersPath: Path, bottomPath: Path) {
    val brush = Brush.linearGradient(listOf(Color.Red, Color.Yellow))
    val foxBody = state.fox.drop(1).dropLast(1)
    val cornerRadiusPx = state.cellSize / 2
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    when (foxBody.size) {
        1 -> {
            drawRoundRect(
                brush = brush,
                cornerRadius = cornerRadius,
                topLeft = Offset(
                    foxBody.first().x * state.cellSize,
                    foxBody.first().y * state.cellSize,
                ),
                size = Size(state.cellSize, state.cellSize),
            )
        }

        else -> {
            val shoulders = foxBody.first()
            shouldersPath.apply {
                reset()
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(
                                shoulders.x * state.cellSize,
                                shoulders.y * state.cellSize,
                            ),
                            size = Size(state.cellSize, state.cellSize),
                        ),
                        topLeft = when (state.shouldersDirection) {
                            Direction.UP, Direction.LEFT -> cornerRadius
                            Direction.DOWN, Direction.RIGHT -> CornerRadius.Zero
                        },
                        topRight = when (state.shouldersDirection) {
                            Direction.UP, Direction.RIGHT -> cornerRadius
                            Direction.DOWN, Direction.LEFT -> CornerRadius.Zero
                        },
                        bottomLeft = when (state.shouldersDirection) {
                            Direction.DOWN, Direction.LEFT -> cornerRadius
                            Direction.UP, Direction.RIGHT -> CornerRadius.Zero
                        },
                        bottomRight = when (state.shouldersDirection) {
                            Direction.DOWN, Direction.RIGHT -> cornerRadius
                            Direction.UP, Direction.LEFT -> CornerRadius.Zero
                        },
                    ),
                )
            }
            drawPath(shouldersPath, brush)

            foxBody.drop(1).dropLast(1).forEach { (x, y) ->
                drawRect(
                    brush = brush,
                    topLeft = Offset(x * state.cellSize, y * state.cellSize),
                    size = Size(state.cellSize, state.cellSize),
                )
            }

            val bottom = foxBody.last()
            bottomPath.apply {
                reset()
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(bottom.x * state.cellSize, bottom.y * state.cellSize),
                            size = Size(state.cellSize, state.cellSize),
                        ),
                        topLeft = when (state.tailDirection) {
                            Direction.UP, Direction.LEFT -> cornerRadius
                            Direction.DOWN, Direction.RIGHT -> CornerRadius.Zero
                        },
                        topRight = when (state.tailDirection) {
                            Direction.UP, Direction.RIGHT -> cornerRadius
                            Direction.DOWN, Direction.LEFT -> CornerRadius.Zero
                        },
                        bottomLeft = when (state.tailDirection) {
                            Direction.DOWN, Direction.LEFT -> cornerRadius
                            Direction.UP, Direction.RIGHT -> CornerRadius.Zero
                        },
                        bottomRight = when (state.tailDirection) {
                            Direction.DOWN, Direction.RIGHT -> cornerRadius
                            Direction.UP, Direction.LEFT -> CornerRadius.Zero
                        },
                    ),
                )
            }
            drawPath(bottomPath, brush)
        }
    }
}
