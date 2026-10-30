package com.viraj022.queens.engine

import com.viraj022.queens.model.Board
import com.viraj022.queens.model.Cell
import com.viraj022.queens.model.CellState
import kotlin.math.abs

/**
 * Pure Kotlin rules engine for LinkedIn-style Queens.
 * No Android dependencies — unit-testable on the JVM.
 */
object QueensEngine {

    /** EMPTY → X_MARK → QUEEN → EMPTY */
    fun nextState(current: CellState): CellState = when (current) {
        CellState.EMPTY -> CellState.X_MARK
        CellState.X_MARK -> CellState.QUEEN
        CellState.QUEEN -> CellState.EMPTY
    }

    fun toggleCell(board: Board, row: Int, col: Int): Board {
        require(row in 0 until board.size && col in 0 until board.size)
        val updated = board.withCell(row, col) { cell ->
            cell.copy(state = nextState(cell.state), isConflicting = false)
        }
        return validateBoard(updated)
    }

    fun setCellState(board: Board, row: Int, col: Int, state: CellState): Board {
        require(row in 0 until board.size && col in 0 until board.size)
        val updated = board.withCell(row, col) { it.copy(state = state, isConflicting = false) }
        return validateBoard(updated)
    }

    /**
     * Flags queens that share a row, column, region, or 8-neighbor adjacency.
     */
    fun validateBoard(board: Board): Board {
        val queens = board.queens()
        val conflicting = mutableSetOf<Pair<Int, Int>>()

        for (i in queens.indices) {
            for (j in i + 1 until queens.size) {
                val q1 = queens[i]
                val q2 = queens[j]
                if (conflicts(q1, q2)) {
                    conflicting += q1.row to q1.col
                    conflicting += q2.row to q2.col
                }
            }
        }

        val grid = board.grid.map { row ->
            row.map { cell ->
                cell.copy(isConflicting = (cell.row to cell.col) in conflicting)
            }
        }
        return board.copy(grid = grid)
    }

    fun conflicts(a: Cell, b: Cell): Boolean {
        if (a.row == b.row) return true
        if (a.col == b.col) return true
        if (a.regionId == b.regionId) return true
        return abs(a.row - b.row) <= 1 && abs(a.col - b.col) <= 1
    }

    /**
     * Solved when there are exactly N non-conflicting queens covering every region.
     */
    fun isSolved(board: Board): Boolean {
        val validated = validateBoard(board)
        val queens = validated.queens()
        if (queens.size != validated.size) return false
        if (validated.grid.flatten().any { it.isConflicting }) return false
        return queens.map { it.regionId }.toSet().size == validated.size
    }

    /**
     * After placing a non-conflicting queen, mark remaining empty cells in its
     * row, column, and 8-neighbors with X (Auto-X assist).
     */
    fun applyAutoX(board: Board, queenRow: Int, queenCol: Int): Board {
        val cell = board.cellAt(queenRow, queenCol)
        if (cell.state != CellState.QUEEN || cell.isConflicting) return board

        val size = board.size
        val grid = board.grid.map { row ->
            row.map { c ->
                if (c.state != CellState.EMPTY) return@map c
                val sameRow = c.row == queenRow
                val sameCol = c.col == queenCol
                val adjacent = abs(c.row - queenRow) <= 1 && abs(c.col - queenCol) <= 1 &&
                    !(c.row == queenRow && c.col == queenCol)
                if (sameRow || sameCol || adjacent) {
                    c.copy(state = CellState.X_MARK)
                } else {
                    c
                }
            }
        }
        return validateBoard(board.copy(grid = grid))
    }

    fun resetMarks(board: Board): Board {
        val grid = board.grid.map { row ->
            row.map { it.copy(state = CellState.EMPTY, isConflicting = false) }
        }
        return board.copy(grid = grid)
    }

    fun boardFromRegions(regions: List<List<Int>>): Board {
        val size = regions.size
        require(size > 0 && regions.all { it.size == size })
        val grid = List(size) { r ->
            List(size) { c ->
                Cell(row = r, col = c, regionId = regions[r][c])
            }
        }
        return Board(size = size, grid = grid)
    }
}
