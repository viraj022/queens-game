package com.viraj022.queens.generator

import com.viraj022.queens.engine.QueensEngine
import com.viraj022.queens.model.CellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PuzzleGeneratorTest {

    @Test
    fun sample6x6_hasSixRegionsAndIsSolvable() {
        val board = PuzzleGenerator.sample6x6()
        assertEquals(6, board.size)
        val regionIds = board.grid.flatten().map { it.regionId }.toSet()
        assertEquals(6, regionIds.size)

        val solution = PuzzleGenerator.findAnySolution(board)
        assertNotNull(solution)
        assertEquals(6, solution!!.size)

        var solved = board
        for ((r, c) in solution) {
            solved = QueensEngine.setCellState(solved, r, c, CellState.QUEEN)
        }
        assertTrue(QueensEngine.isSolved(solved))
    }

    @Test
    fun generate_size6_producesSolvableBoard() {
        val board = PuzzleGenerator.generate(size = 6, random = Random(42))
        assertEquals(6, board.size)
        val regions = board.grid.flatten().map { it.regionId }.toSet()
        assertEquals(6, regions.size)

        val solution = PuzzleGenerator.findAnySolution(board)
        assertNotNull("Generated 6x6 must be solvable", solution)
        assertTrue(
            PuzzleGenerator.isSolvableWithQueens(board, solution!!)
        )
    }

    @Test
    fun generate_size7_and8_areSolvable() {
        for (size in listOf(7, 8)) {
            val board = PuzzleGenerator.generate(size = size, random = Random(size * 1000L + 7))
            assertEquals(size, board.size)
            val solution = PuzzleGenerator.findAnySolution(board)
            assertNotNull("Generated ${size}x$size must be solvable", solution)

            var working = board
            for ((r, c) in solution!!) {
                working = QueensEngine.setCellState(working, r, c, CellState.QUEEN)
            }
            assertTrue(QueensEngine.isSolved(working))
        }
    }

    @Test
    fun generate_regionsAreContiguousCountsMatchSize() {
        val board = PuzzleGenerator.generate(size = 6, random = Random(99))
        val byRegion = board.grid.flatten().groupBy { it.regionId }
        assertEquals(6, byRegion.size)
        byRegion.values.forEach { cells ->
            assertTrue(cells.isNotEmpty())
        }
    }
}
