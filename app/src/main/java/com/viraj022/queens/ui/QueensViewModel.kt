package com.viraj022.queens.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viraj022.queens.engine.QueensEngine
import com.viraj022.queens.generator.PuzzleGenerator
import com.viraj022.queens.model.CellState
import com.viraj022.queens.model.GameUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class QueensViewModel : ViewModel() {

    private val history = ArrayDeque<GameUiState>(64)
    private var timerJob: Job? = null
    private var startedAtElapsed = 0L
    private var runningSinceWallMs: Long? = null

    private val _uiState = MutableStateFlow(
        GameUiState(board = PuzzleGenerator.sample6x6(), boardSize = 6)
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        startTimer()
    }

    fun onCellTap(row: Int, col: Int) {
        val current = _uiState.value
        if (current.isSolved) return

        pushHistory(current)
        val before = current.board.cellAt(row, col).state
        var board = QueensEngine.toggleCell(current.board, row, col)
        val after = board.cellAt(row, col).state

        if (current.autoXEnabled && after == CellState.QUEEN && !board.cellAt(row, col).isConflicting) {
            board = QueensEngine.applyAutoX(board, row, col)
        }

        val solved = QueensEngine.isSolved(board)
        _uiState.update {
            it.copy(
                board = board,
                isSolved = solved,
                canUndo = history.isNotEmpty(),
                moveCount = it.moveCount + 1
            )
        }

        if (solved) {
            pauseTimer()
        } else if (before == CellState.EMPTY && runningSinceWallMs == null) {
            startTimer()
        }
    }

    fun newGame(size: Int = _uiState.value.boardSize) {
        val clamped = size.coerceIn(6, 8)
        history.clear()
        startedAtElapsed = 0L
        val board = PuzzleGenerator.generate(clamped, Random.Default)
        _uiState.value = GameUiState(
            board = board,
            boardSize = clamped,
            autoXEnabled = _uiState.value.autoXEnabled,
            elapsedMillis = 0L
        )
        startTimer(reset = true)
    }

    fun resetBoard() {
        val current = _uiState.value
        pushHistory(current)
        val board = QueensEngine.resetMarks(current.board)
        _uiState.update {
            it.copy(
                board = board,
                isSolved = false,
                canUndo = history.isNotEmpty(),
                moveCount = 0,
                elapsedMillis = 0L
            )
        }
        startTimer(reset = true)
    }

    fun undo() {
        val previous = history.removeLastOrNull() ?: return
        _uiState.value = previous.copy(canUndo = history.isNotEmpty())
        if (previous.isSolved) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun setAutoX(enabled: Boolean) {
        _uiState.update { it.copy(autoXEnabled = enabled) }
    }

    fun toggleAutoX() {
        setAutoX(!_uiState.value.autoXEnabled)
    }

    private fun pushHistory(state: GameUiState) {
        if (history.size >= 64) history.removeFirst()
        history.addLast(state.copy(canUndo = true))
    }

    private fun startTimer(reset: Boolean = false) {
        if (reset) {
            startedAtElapsed = 0L
            runningSinceWallMs = System.currentTimeMillis()
            _uiState.update { it.copy(elapsedMillis = 0L) }
        } else if (runningSinceWallMs == null) {
            runningSinceWallMs = System.currentTimeMillis()
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val since = runningSinceWallMs
                if (since != null && !_uiState.value.isSolved) {
                    val elapsed = startedAtElapsed + (System.currentTimeMillis() - since)
                    _uiState.update { it.copy(elapsedMillis = elapsed) }
                }
                delay(250)
            }
        }
    }

    private fun pauseTimer() {
        val since = runningSinceWallMs
        if (since != null) {
            startedAtElapsed += System.currentTimeMillis() - since
            runningSinceWallMs = null
            _uiState.update { it.copy(elapsedMillis = startedAtElapsed) }
        }
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
