/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.longfox

import android.annotation.SuppressLint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.mozilla.fenix.longfox.Direction.DOWN
import org.mozilla.fenix.longfox.Direction.LEFT
import org.mozilla.fenix.longfox.Direction.RIGHT
import org.mozilla.fenix.longfox.Direction.UP
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class GridPoint(val x: Int, val y: Int) {
    fun isAbove(secondPoint: GridPoint): Boolean = y < secondPoint.y
    fun isBelow(secondPoint: GridPoint): Boolean = y > secondPoint.y
    fun isLeftOf(secondPoint: GridPoint): Boolean = x < secondPoint.x
    @SuppressLint("Unused")
    fun isRightOf(secondPoint: GridPoint): Boolean = x > secondPoint.x
}

data class GameState(
    val size: Size = Size(0f, 0f),
    val fox: List<GridPoint> = listOf(GridPoint(5, 5), GridPoint(5, 4), GridPoint(5, 3), GridPoint(5, 2)),
    val food: GridPoint = GridPoint(8, 8),
    val direction: Direction = DOWN,
    val isGameOver: Boolean = false,
    val score: Int = 0,
    val beepNext: Boolean = true,
    val numCells: Int = 12,
    val demoMode: Boolean = false,
) {
    companion object {
        const val CELL_SIZE_DP = 20f
        const val FRAME_INTERVAL_TIME_MS = 100L
    }

    val numCellsWide = numCells
    val numCellsTall = numCellsWide
    val cellSize = (size.minDimension / numCellsWide).toInt().toFloat()

    val shouldersDirection: Direction = when {
        fox.size < 3 -> direction
        else -> getDirectionBetweenPoints(fox[1], fox[2])
    }

    val tailDirection: Direction = when {
        fox.size < 3 -> direction
        else -> getDirectionBetweenPoints(fox[fox.size - 2], fox[fox.size - 3])
    }

    fun toggleBeepNext(): GameState = copy(beepNext = !beepNext)

    private fun getDirectionBetweenPoints(thisPoint: GridPoint, otherPoint: GridPoint): Direction {
        return when {
            thisPoint.isAbove(otherPoint) -> UP
            thisPoint.isBelow(otherPoint) -> DOWN
            thisPoint.isLeftOf(otherPoint) -> LEFT
            else -> RIGHT
        }
    }

    fun onSized(size: Size): GameState {
        return if (this.size == size) this else copy(size = size)
    }

    fun randomGridPoint(): GridPoint = GridPoint(
        Random.nextInt(numCellsWide),
        Random.nextInt(numCellsTall),
    )

    fun moveFox(): GameState {
        val head = fox.first()
        val newHead = when (direction) {
            UP -> head.copy(y = head.y - 1)
            DOWN -> head.copy(y = head.y + 1)
            LEFT -> head.copy(x = head.x - 1)
            RIGHT -> head.copy(x = head.x + 1)
        }

        val collidedWithSelf = newHead in fox.drop(1)
        val collidedWithEdge = !withinBounds(newHead)
        val collidedWithFood = newHead == food
        val isGameOver = collidedWithSelf || collidedWithEdge

        return if (collidedWithFood && !isGameOver) {
            copy(
                food = randomGridPoint(),
                fox = listOf(newHead) + fox,
                isGameOver = false,
                score = score + 1,
            )
        } else {
            copy(
                fox = listOf(newHead) + fox.dropLast(1),
                isGameOver = isGameOver,
            )
        }
    }

    fun moveFoxDemo(): GameState {
        val head = fox.first()

        val newDirection = when {
            head.y >= numCellsTall - 2 -> {
                if (head.x < numCellsWide - 2) RIGHT else UP
            }
            head.y < 2 -> {
                if (head.x >= 2) LEFT else DOWN
            }
            else -> direction
        }

        val newHead = when (newDirection) {
            UP -> head.copy(y = head.y - 1)
            DOWN -> head.copy(y = head.y + 1)
            LEFT -> head.copy(x = head.x - 1)
            RIGHT -> head.copy(x = head.x + 1)
        }

        val newFox = listOf(newHead) + fox.dropLast(1)
        return copy(fox = newFox, direction = newDirection)
    }

    private fun withinBounds(point: GridPoint): Boolean =
        point.x in 0 until numCellsWide && point.y in 0 until numCellsTall

    fun onTap(offset: Offset): GameState {
        val (x, y) = offset
        val headX = fox.first().x * cellSize
        val headY = fox.first().y * cellSize
        val newDirection = when (direction) {
            UP, DOWN -> if (x < headX) LEFT else RIGHT
            LEFT, RIGHT -> if (y < headY) UP else DOWN
        }
        return copy(direction = newDirection)
    }
}
