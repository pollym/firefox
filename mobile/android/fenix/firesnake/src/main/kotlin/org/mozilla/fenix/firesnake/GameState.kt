/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.firesnake

import android.graphics.Point
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.mozilla.fenix.firesnake.Direction.DOWN
import org.mozilla.fenix.firesnake.Direction.LEFT
import org.mozilla.fenix.firesnake.Direction.RIGHT
import org.mozilla.fenix.firesnake.Direction.UP
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class GridPoint(val x: Int, val y: Int)

data class GameState(
    val size: Size = Size(0f, 0f),
    val centre: Offset = Offset(0f, 0f),
    val snake: List<GridPoint> = listOf(GridPoint(5, 5), GridPoint(4, 5)),
    val food: GridPoint = GridPoint(8, 8),
    val direction: Direction = RIGHT,
    val isGameOver: Boolean = false,
    val score: Int = 0,
) {
    val numCellsWide = 15
    val cellSize = size.minDimension / numCellsWide

    fun onSized(size: Size, centre: Offset): GameState {
        return if (this.size == size && this.centre == centre) this
        else copy(size = size, centre = centre)
    }
    fun toPx(gridPoint: GridPoint): Point =
        Point((gridPoint.x * cellSize).toInt(), (gridPoint.y * cellSize).toInt())

    fun toGridPoint(x: Int, y: Int): GridPoint =
        GridPoint((x / cellSize).toInt(), (y / cellSize).toInt())

    fun randomGridPoint(): GridPoint = toGridPoint(
        Random.nextInt(size.width.toInt()), Random.nextInt(size.height.toInt())
    )

    fun moveSnake(): GameState {
        val head = snake.first()
        val newHead = when (direction) {
            UP -> head.copy(y = head.y - 1)
            DOWN -> head.copy(y = head.y + 1)
            LEFT -> head.copy(x = head.x - 1)
            RIGHT -> head.copy(x = head.x + 1)
        }
        val newHeadPos = toPx(newHead)

        val collidedWithSelf = newHead in snake.drop(1)
        val collidedWithEdge = !withinBounds(newHeadPos)
        val collidedWithFood = newHead == food
        val isGameOver = collidedWithSelf || collidedWithEdge

        return if (collidedWithFood && !isGameOver) {
            copy(
                food = randomGridPoint(),
                snake = listOf(newHead) + snake,
                isGameOver = false,
                score = score + 1
            )
        } else {
            copy(
                snake = listOf(newHead) + snake.dropLast(1),
                isGameOver = isGameOver,
            )
        }
    }

    private fun withinBounds(newHeadPos: Point): Boolean {
        val width = size.width
        val height = size.height
        val left = (centre.x - width / 2).toInt()
        val right = (centre.x + width / 2).toInt()
        val top = (centre.y - height / 2).toInt()
        val bottom = (centre.y + height / 2).toInt()
        return newHeadPos.x in left..right && newHeadPos.y in top..bottom
    }

    fun onTap(offset: Offset): GameState {
        val (x, y) = offset
        val snakeHeadPos = toPx(snake.first())
        val newDirection = when (direction) {
            UP, DOWN -> if (x < snakeHeadPos.x) LEFT else RIGHT
            LEFT, RIGHT -> if (y < snakeHeadPos.y) UP else DOWN
        }
        return copy(direction = newDirection)
    }

}