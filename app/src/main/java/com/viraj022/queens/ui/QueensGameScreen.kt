package com.viraj022.queens.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viraj022.queens.model.Cell
import com.viraj022.queens.model.CellState
import com.viraj022.queens.model.GameUiState
import com.viraj022.queens.ui.components.QueenIcon
import com.viraj022.queens.ui.components.XMarkIcon
import com.viraj022.queens.ui.theme.ButtonSurface
import com.viraj022.queens.ui.theme.ConflictCoral
import com.viraj022.queens.ui.theme.CreamBackground
import com.viraj022.queens.ui.theme.GridBorderColor
import com.viraj022.queens.ui.theme.QueenGold
import com.viraj022.queens.ui.theme.RegionColors
import com.viraj022.queens.ui.theme.SoftGridLine
import com.viraj022.queens.ui.theme.TextPrimary
import com.viraj022.queens.ui.theme.TextSecondary
import com.viraj022.queens.ui.theme.VictoryEmerald

@Composable
fun QueensGameRoute(viewModel: QueensViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QueensGameScreen(
        state = state,
        onCellTap = viewModel::onCellTap,
        onNewGame = { viewModel.newGame() },
        onReset = viewModel::resetBoard,
        onUndo = viewModel::undo,
        onToggleAutoX = viewModel::toggleAutoX,
        onSizeSelected = { size -> viewModel.newGame(size) }
    )
}

@Composable
fun QueensGameScreen(
    state: GameUiState,
    onCellTap: (Int, Int) -> Unit,
    onNewGame: () -> Unit,
    onReset: () -> Unit,
    onUndo: () -> Unit,
    onToggleAutoX: () -> Unit,
    onSizeSelected: (Int) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Queens",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary
        )
        Text(
            text = "One queen per row, column, and region — never adjacent.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatElapsed(state.elapsedMillis),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { contentDescription = "Elapsed time" }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Auto-X",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = state.autoXEnabled,
                    onCheckedChange = { onToggleAutoX() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = QueenGold,
                        checkedThumbColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(6, 7, 8).forEach { size ->
                FilterChip(
                    selected = state.boardSize == size,
                    onClick = { onSizeSelected(size) },
                    label = { Text("${size}×$size") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QueenGold.copy(alpha = 0.20f),
                        selectedLabelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        BoardView(
            boardSize = state.board.size,
            cells = state.board.grid,
            onCellClick = { r, c ->
                val before = state.board.cellAt(r, c).state
                onCellTap(r, c)
                val next = when (before) {
                    CellState.EMPTY -> CellState.X_MARK
                    CellState.X_MARK -> CellState.QUEEN
                    CellState.QUEEN -> CellState.EMPTY
                }
                if (next == CellState.QUEEN) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                } else {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ControlButton(
                label = "New Game",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNewGame()
                },
                modifier = Modifier.weight(1f),
                emphasized = true
            )
            ControlButton(
                label = "Reset",
                onClick = onReset,
                modifier = Modifier.weight(1f)
            )
            ControlButton(
                label = "Undo",
                onClick = onUndo,
                modifier = Modifier.weight(1f),
                enabled = state.canUndo
            )
        }

        AnimatedVisibility(
            visible = state.isSolved,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            enter = fadeIn(tween(280)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = VictoryEmerald,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "Puzzle solved",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (emphasized) QueenGold else ButtonSurface,
            contentColor = if (emphasized) Color.White else TextPrimary,
            disabledContainerColor = ButtonSurface.copy(alpha = 0.5f),
            disabledContentColor = TextSecondary.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun BoardView(
    boardSize: Int,
    cells: List<List<Cell>>,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .border(2.dp, SoftGridLine, RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        for (r in 0 until boardSize) {
            Row(modifier = Modifier.weight(1f)) {
                for (c in 0 until boardSize) {
                    CellView(
                        cell = cells[r][c],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { onCellClick(r, c) }
                    )
                }
            }
        }
    }
}

@Composable
fun CellView(
    cell: Cell,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(cell.state) {
        scale.snapTo(0.86f)
        scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
    }

    val backgroundColor = if (cell.isConflicting) {
        ConflictCoral
    } else {
        RegionColors.getOrElse(cell.regionId % RegionColors.size) { Color.White }
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(0.5.dp, GridBorderColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = "Cell ${cell.row},${cell.col} ${cell.state.name}"
            },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.scale(scale.value)) {
            when (cell.state) {
                CellState.X_MARK -> XMarkIcon(Modifier.size(28.dp))
                CellState.QUEEN -> QueenIcon(Modifier.size(36.dp))
                CellState.EMPTY -> Unit
            }
        }
    }
}

private fun formatElapsed(millis: Long): String {
    val totalSec = (millis / 1000).coerceAtLeast(0)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
