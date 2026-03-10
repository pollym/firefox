/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.fenix.longfox

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {

    @Test
    fun `fox moves down`() {
        val state = state(direction = Direction.DOWN).moveFox()
        assertEquals(GridPoint(5, 6), state.fox.first())
    }

    @Test
    fun `fox moves up`() {
        val state = state(direction = Direction.UP).moveFox()
        assertEquals(GridPoint(5, 4), state.fox.first())
    }

    @Test
    fun `fox moves left`() {
        val state = state(direction = Direction.LEFT).moveFox()
        assertEquals(GridPoint(4, 5), state.fox.first())
    }

    @Test
    fun `fox moves right`() {
        val state = state(direction = Direction.RIGHT).moveFox()
        assertEquals(GridPoint(6, 5), state.fox.first())
    }

    @Test
    fun `tail is dropped on normal move`() {
        val initial = state()
        val state = initial.moveFox()
        assertEquals(initial.fox.size, state.fox.size)
    }

    // --- moveFox: wall collisions ---
    // NB we are on a 10 x 10 grid

    @Test
    fun `game is not over when fox moves to a perfectly reasonable place`() {
        val state = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(2, 2), GridPoint(2, 1)),
        ).moveFox()
        assertFalse(state.isGameOver)
    }

    @Test
    fun `game over when fox hits bottom wall`() {
        val state = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(5, 9), GridPoint(5, 8)),
        )
        assertFalse(state.isGameOver)
        assertTrue(state.moveFox().isGameOver)
    }

    @Test
    fun `game over when fox hits top wall`() {
        val state = state(
            direction = Direction.UP,
            fox = listOf(GridPoint(5, 0), GridPoint(5, 1)),
        )
        assertFalse(state.isGameOver)
        assertTrue(state.moveFox().isGameOver)
    }

    @Test
    fun `game over when fox hits left wall`() {
        val state = state(
            direction = Direction.LEFT,
            fox = listOf(GridPoint(0, 5), GridPoint(1, 5)),
        )
        assertFalse(state.isGameOver)
        assertTrue(state.moveFox().isGameOver)
    }

    @Test
    fun `game over when fox hits right wall`() {
        val state = state(
            direction = Direction.RIGHT,
            fox = listOf(GridPoint(9, 5), GridPoint(8, 5)),
        )
        assertFalse(state.isGameOver)
        assertTrue(state.moveFox().isGameOver)
    }

    @Test
    fun `no game over one step before bottom wall`() {
        val state = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(5, 8), GridPoint(5, 7)),
        )
        assertFalse(state.isGameOver)
        assertFalse(state.moveFox().isGameOver)
    }

    // --- moveFox: self collision ---

    @Test
    fun `game over when fox hits itself`() {
        // 1234567
        //1
        //2
        //3
        //4  🟧🟧
        //5  🦊🟧
        //6
        val state = state(
            direction = Direction.RIGHT,
            fox = listOf(
                GridPoint(3, 5),
                GridPoint(4, 5),
                GridPoint(4, 4),
                GridPoint(3, 4),
                GridPoint(3, 5), // loop back — new head will land on fox[1]
            ),
        )
        assertFalse(state.isGameOver)
        assertTrue(state.moveFox().isGameOver)
    }

    // --- moveFox: eating food ---

    @Test
    fun `fox grows when eating food`() {
        val hungryFox = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(5, 5), GridPoint(5, 4)),
            food = GridPoint(5, 6),
        )
        val fedFox = hungryFox.moveFox()
        assertEquals(hungryFox.fox.size + 1, fedFox.fox.size)
    }

    @Test
    fun `score increments when eating food`() {
        val state = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(5, 5), GridPoint(5, 4)),
            food = GridPoint(5, 6),
        ).moveFox()
        assertEquals(1, state.score)
    }

    @Test
    fun `food moves to new position after being eaten`() {
        val food = GridPoint(5, 6)
        val state = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(5, 5), GridPoint(5, 4)),
            food = food,
        ).moveFox()
        assertNotEquals(food, state.food)
    }

    @Test
    fun `no game over when eating food at grid edge`() {
        val state = state(
            direction = Direction.RIGHT,
            fox = listOf(GridPoint(8, 5), GridPoint(7, 5)),
            food = GridPoint(9, 5),
        ).moveFox()
        assertFalse(state.isGameOver)
        assertEquals(1, state.score)
    }

    // --- onTap ---

    @Test
    fun `tapping left of head while moving down turns fox left`() {
        // cellSize = 20f, headX = 5 * 20 = 100
        val state =
            state(
                direction = Direction.DOWN, 
                fox = listOf(GridPoint(5, 5), GridPoint(5, 4))
            ).onTap(
                Offset(x = 50f, y = 200f)
            )
        assertEquals(Direction.LEFT, state.direction)
    }

    @Test
    fun `tapping right of head while moving down turns fox right`() {
        val state =
            state(
                direction = Direction.DOWN, 
                fox = listOf(GridPoint(5, 5), GridPoint(5, 4))
            ).onTap(
                Offset(x = 150f, y = 200f)
            )
        assertEquals(Direction.RIGHT, state.direction)
    }

    @Test
    fun `tapping above head while moving right turns fox up`() {
        // headY = 5 * 20 = 100
        val state =
            state(
                direction = Direction.RIGHT,
                fox = listOf(GridPoint(5, 5), GridPoint(4, 5))
            ).onTap(
                Offset(x = 200f, y = 50f)
            )
        assertEquals(Direction.UP, state.direction)
    }

    @Test
    fun `tapping below head while moving right turns fox down`() {
        val state =
            state(
                direction = Direction.RIGHT,
                fox = listOf(GridPoint(5, 5), GridPoint(4, 5))
            ).onTap(
                Offset(x = 200f, y = 150f)
            )
        assertEquals(Direction.DOWN, state.direction)
    }

    // --- shouldersDirection / tailDirection ---

    @Test
    fun `shouldersDirection reflects the direction from shoulder to body`() {
        val state = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(5, 5), GridPoint(5, 4), GridPoint(5, 3)),
        )
        assertEquals(Direction.DOWN, state.shouldersDirection)
    }

    @Test
    fun `tailDirection reflects the direction from tail segment outward`() {
        val state = state(
            direction = Direction.DOWN,
            fox = listOf(GridPoint(5, 5), GridPoint(5, 4), GridPoint(5, 3)),
        )
        assertEquals(Direction.UP, state.tailDirection)
    }

    private fun state(
        direction: Direction = Direction.DOWN,
        fox: List<GridPoint> = listOf(GridPoint(5, 5), GridPoint(5, 4), GridPoint(5, 3)),
        food: GridPoint = GridPoint(0, 0),
        numCells: Int = 10,
    ): GameState {
        val cellSize = 20f
        return GameState(
            size = Size(numCells * cellSize, numCells * cellSize),
            fox = fox,
            food = food,
            direction = direction,
            numCells = numCells,
        )
    }
}
