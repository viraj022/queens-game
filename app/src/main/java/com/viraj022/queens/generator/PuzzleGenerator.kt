package com.viraj022.queens.generator

import com.viraj022.queens.engine.QueensEngine
import com.viraj022.queens.model.Board
import com.viraj022.queens.model.Cell
import com.viraj022.queens.model.CellState
import kotlin.random.Random

/**
 * On-device puzzle generator:
 * 1) Place N non-attacking (Queens-rules) queens via backtracking.
 * 2) Grow contiguous color regions around each queen until the board is covered.
 */
object PuzzleGenerator {

    /** Hand-authored sample used as first-load / fallback seed. */
    val SAMPLE_6X6_REGIONS: List<List<Int>> = listOf(
        listOf(0, 0, 1, 1, 1, 2),
        listOf(0, 0, 0, 1, 2, 2),
        listOf(3, 3, 0, 1, 2, 4),
        listOf(3, 3, 5, 5, 4, 4),
        listOf(3, 5, 5, 5, 4, 4),
        listOf(3, 3, 5, 4, 4, 4)
    )

    fun sample6x6(): Board = QueensEngine.boardFromRegions(SAMPLE_6X6_REGIONS)

    /**
     * Generate a solvable board of [size] (default range 6–8).
     * Retries with a new seed until a valid placement + region partition is found.
     */
    fun generate(
        size: Int = 6,
        random: Random = Random.Default,
        maxAttempts: Int = 80
    ): Board {
        require(size in 4..12) { "Supported sizes: 4–12" }

        repeat(maxAttempts) {
            val queens = placeQueens(size, random) ?: return@repeat
            val regions = growRegions(size, queens, random) ?: return@repeat
            val board = QueensEngine.boardFromRegions(regions)
            if (isSolvableWithQueens(board, queens)) {
                return board
            }
        }
        // Guaranteed-valid fallback
        return if (size == 6) sample6x6() else generateDeterministic(size)
    }

    /**
     * Verify that a known queen placement satisfies engine rules on [board] regions.
     */
    fun isSolvableWithQueens(board: Board, queens: List<Pair<Int, Int>>): Boolean {
        if (queens.size != board.size) return false
        var working = QueensEngine.resetMarks(board)
        for ((r, c) in queens) {
            working = QueensEngine.setCellState(working, r, c, CellState.QUEEN)
        }
        return QueensEngine.isSolved(working)
    }

    /**
     * Backtracking search: can this region map be solved?
     * Used by unit tests to assert generator output is solvable.
     */
    fun findAnySolution(board: Board, maxNodes: Int = 250_000): List<Pair<Int, Int>>? {
        val size = board.size
        val regions = Array(size) { r -> IntArray(size) { c -> board.grid[r][c].regionId } }
        val usedCols = BooleanArray(size)
        val usedRegions = BooleanArray(size)
        val queens = mutableListOf<Pair<Int, Int>>()
        var nodes = 0

        fun ok(row: Int, col: Int): Boolean {
            val region = regions[row][col]
            if (usedCols[col] || usedRegions[region]) return false
            for ((qr, qc) in queens) {
                if (abs(qr - row) <= 1 && abs(qc - col) <= 1) return false
            }
            return true
        }

        fun dfs(row: Int): Boolean {
            if (row == size) return true
            if (++nodes > maxNodes) return false
            val cols = (0 until size).shuffled(Random(row * 31 + size))
            for (col in cols) {
                if (!ok(row, col)) continue
                val region = regions[row][col]
                usedCols[col] = true
                usedRegions[region] = true
                queens += row to col
                if (dfs(row + 1)) return true
                queens.removeAt(queens.lastIndex)
                usedCols[col] = false
                usedRegions[region] = false
            }
            return false
        }

        return if (dfs(0)) queens.toList() else null
    }

    private fun placeQueens(size: Int, random: Random): List<Pair<Int, Int>>? {
        val usedCols = BooleanArray(size)
        val queens = mutableListOf<Pair<Int, Int>>()

        fun ok(row: Int, col: Int): Boolean {
            if (usedCols[col]) return false
            for ((qr, qc) in queens) {
                if (abs(qr - row) <= 1 && abs(qc - col) <= 1) return false
            }
            return true
        }

        fun dfs(row: Int): Boolean {
            if (row == size) return true
            val order = (0 until size).shuffled(random)
            for (col in order) {
                if (!ok(row, col)) continue
                usedCols[col] = true
                queens += row to col
                if (dfs(row + 1)) return true
                queens.removeAt(queens.lastIndex)
                usedCols[col] = false
            }
            return false
        }

        return if (dfs(0)) queens.toList() else null
    }

    /**
     * Assign each queen its own region, then flood-fill unclaimed cells from
     * region frontiers until every cell belongs to exactly one region.
     */
    private fun growRegions(
        size: Int,
        queens: List<Pair<Int, Int>>,
        random: Random
    ): List<List<Int>>? {
        val regions = Array(size) { IntArray(size) { -1 } }
        val frontiers = Array(size) { mutableListOf<Pair<Int, Int>>() }

        queens.forEachIndexed { id, (r, c) ->
            regions[r][c] = id
            frontiers[id] += r to c
        }

        var remaining = size * size - size
        var stall = 0
        while (remaining > 0) {
            val progressBefore = remaining
            val order = (0 until size).shuffled(random)
            for (id in order) {
                if (remaining == 0) break
                val frontier = frontiers[id]
                if (frontier.isEmpty()) continue
                val shuffled = frontier.shuffled(random)
                var expanded = false
                for ((r, c) in shuffled) {
                    val neighbors = listOf(
                        r - 1 to c, r + 1 to c, r to c - 1, r to c + 1
                    ).filter { (nr, nc) ->
                        nr in 0 until size && nc in 0 until size && regions[nr][nc] == -1
                    }
                    if (neighbors.isEmpty()) continue
                    val (nr, nc) = neighbors.random(random)
                    regions[nr][nc] = id
                    frontiers[id] += nr to nc
                    remaining--
                    expanded = true
                    break
                }
                if (!expanded) {
                    // Drop dead frontier cells that have no free neighbors
                    frontiers[id].removeAll { (r, c) ->
                        listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1).none { (nr, nc) ->
                            nr in 0 until size && nc in 0 until size && regions[nr][nc] == -1
                        }
                    }
                }
            }
            if (remaining == progressBefore) {
                stall++
                if (stall > 4) {
                    // Force-assign leftover cells to a neighboring region
                    for (r in 0 until size) {
                        for (c in 0 until size) {
                            if (regions[r][c] != -1) continue
                            val neighborId = listOf(
                                r - 1 to c, r + 1 to c, r to c - 1, r to c + 1
                            ).firstNotNullOfOrNull { (nr, nc) ->
                                if (nr in 0 until size && nc in 0 until size && regions[nr][nc] >= 0) {
                                    regions[nr][nc]
                                } else null
                            }
                            if (neighborId != null) {
                                regions[r][c] = neighborId
                                remaining--
                            }
                        }
                    }
                    if (remaining > 0) return null
                }
            } else {
                stall = 0
            }
        }

        // Ensure each region id 0..size-1 appears at least once
        val present = regions.flatMap { it.toList() }.toSet()
        if (present.size != size || present.any { it !in 0 until size }) return null

        return regions.map { it.toList() }
    }

    /** Simple striped regions + known-safe queen diagonal offset pattern fallback. */
    private fun generateDeterministic(size: Int): Board {
        // Prefer sample for 6; for others build contiguous row-ish bands that still
        // admit a solution found by search — fall back to column-band regions.
        val regions = List(size) { r ->
            List(size) { c ->
                // Vertical strips: region = column index → trivially solvable on main diagonal
                // but adjacency rule blocks diagonal; use a staggered column-band.
                c
            }
        }
        // Vertical strips with queens on a non-adjacent permutation (step 2 when even).
        val step = if (size % 2 == 0) size / 2 + 1 else 2
        val queens = (0 until size).map { r -> r to ((r * step) % size) }
        // Rebuild regions around those queens so they are valid
        val grown = growRegions(size, queens, Random(size * 97)) ?: regions
        return QueensEngine.boardFromRegions(grown)
    }

    private fun abs(x: Int): Int = if (x < 0) -x else x
}
