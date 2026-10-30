package com.viraj022.queens.model

enum class CellState {
    EMPTY,
    X_MARK,
    QUEEN
}

data class Cell(
    val row: Int,
    val col: Int,
    val regionId: Int,
    val state: CellState = CellState.EMPTY,
    val isConflicting: Boolean = false
)

data class Board(
    val size: Int,
    val grid: List<List<Cell>>
) {
    init {
        require(size > 0) { "Board size must be positive" }
        require(grid.size == size) { "Grid row count must match size" }
        require(grid.all { it.size == size }) { "Grid must be square" }
    }

    fun cellAt(row: Int, col: Int): Cell = grid[row][col]

    fun withCell(row: Int, col: Int, transform: (Cell) -> Cell): Board {
        val updated = grid.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, cell ->
                if (r == row && c == col) transform(cell) else cell
            }
        }
        return copy(grid = updated)
    }

    fun queens(): List<Cell> = grid.flatten().filter { it.state == CellState.QUEEN }
}

/**
 * Immutable snapshot of everything the Compose UI needs to render.
 */
data class GameUiState(
    val board: Board,
    val isSolved: Boolean = false,
    val elapsedMillis: Long = 0L,
    val autoXEnabled: Boolean = true,
    val boardSize: Int = board.size,
    val canUndo: Boolean = false,
    val moveCount: Int = 0
)
