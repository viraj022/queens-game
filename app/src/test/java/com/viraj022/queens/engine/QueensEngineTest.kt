package com.viraj022.queens.engine

import com.viraj022.queens.generator.PuzzleGenerator
import com.viraj022.queens.model.CellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueensEngineTest {

    @Test
    fun toggleCell_cyclesEmptyToXToQueenToEmpty() {
        var board = PuzzleGenerator.sample6x6()
        board = QueensEngine.toggleCell(board, 0, 0)
        assertEquals(CellState.X_MARK, board.cellAt(0, 0).state)

        board = QueensEngine.toggleCell(board, 0, 0)
        assertEquals(CellState.QUEEN, board.cellAt(0, 0).state)

        board = QueensEngine.toggleCell(board, 0, 0)
        assertEquals(CellState.EMPTY, board.cellAt(0, 0).state)
    }

    @Test
    fun validateBoard_flagsSameRowQueens() {
        var board = PuzzleGenerator.sample6x6()
        board = QueensEngine.setCellState(board, 0, 0, CellState.QUEEN)
        board = QueensEngine.setCellState(board, 0, 5, CellState.QUEEN)

        assertTrue(board.cellAt(0, 0).isConflicting)
        assertTrue(board.cellAt(0, 5).isConflicting)
        assertFalse(QueensEngine.isSolved(board))
    }

    @Test
    fun validateBoard_flagsAdjacentDiagonalQueens() {
        var board = PuzzleGenerator.sample6x6()
        board = QueensEngine.setCellState(board, 1, 1, CellState.QUEEN)
        board = QueensEngine.setCellState(board, 2, 2, CellState.QUEEN)

        assertTrue(board.cellAt(1, 1).isConflicting)
        assertTrue(board.cellAt(2, 2).isConflicting)
    }

    @Test
    fun isSolved_trueForKnownSampleSolution() {
        // Solution for SAMPLE_6X6_REGIONS (verified by generator search)
        val solution = PuzzleGenerator.findAnySolution(PuzzleGenerator.sample6x6())
        assertTrue("Sample 6x6 must be solvable", solution != null)

        var board = PuzzleGenerator.sample6x6()
        for ((r, c) in solution!!) {
            board = QueensEngine.setCellState(board, r, c, CellState.QUEEN)
        }
        assertTrue(QueensEngine.isSolved(board))
        assertEquals(6, board.queens().size)
        assertTrue(board.grid.flatten().none { it.isConflicting })
    }

    @Test
    fun isSolved_falseWhenMissingQueens() {
        val board = PuzzleGenerator.sample6x6()
        assertFalse(QueensEngine.isSolved(board))
    }

    @Test
    fun applyAutoX_marksRowColumnAndNeighbors() {
        var board = PuzzleGenerator.sample6x6()
        board = QueensEngine.setCellState(board, 2, 3, CellState.QUEEN)
        board = QueensEngine.applyAutoX(board, 2, 3)

        // Same row empties become X
        for (c in 0 until 6) {
            if (c == 3) continue
            assertEquals(
                "row auto-x at (2,$c)",
                CellState.X_MARK,
                board.cellAt(2, c).state
            )
        }
        // Same column
        for (r in 0 until 6) {
            if (r == 2) continue
            assertEquals(
                "col auto-x at ($r,3)",
                CellState.X_MARK,
                board.cellAt(r, 3).state
            )
        }
        // Diagonal neighbor
        assertEquals(CellState.X_MARK, board.cellAt(1, 2).state)
        assertEquals(CellState.X_MARK, board.cellAt(1, 4).state)
        assertEquals(CellState.X_MARK, board.cellAt(3, 2).state)
        assertEquals(CellState.X_MARK, board.cellAt(3, 4).state)
        assertEquals(CellState.QUEEN, board.cellAt(2, 3).state)
    }

    @Test
    fun applyAutoX_skipsConflictingQueen() {
        var board = PuzzleGenerator.sample6x6()
        board = QueensEngine.setCellState(board, 0, 0, CellState.QUEEN)
        board = QueensEngine.setCellState(board, 0, 4, CellState.QUEEN)
        val before = board
        val after = QueensEngine.applyAutoX(board, 0, 0)
        // Conflicting — Auto-X should leave board unchanged (aside from validation flags)
        assertEquals(before.grid.map { row -> row.map { it.state } },
            after.grid.map { row -> row.map { it.state } })
    }
}
