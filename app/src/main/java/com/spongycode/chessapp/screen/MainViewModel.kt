package com.spongycode.chessapp.screen

import androidx.lifecycle.ViewModel
import com.spongycode.chess_core.ChessEngine
import com.spongycode.chess_core.Color
import com.spongycode.chess_core.toShortFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class MainViewModel : ViewModel() {
    private val chessEngine = ChessEngine()

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState = _gameState.asStateFlow()

    init {
        refreshBoard()
    }

    private fun refreshBoard() {
        val board = chessEngine.getBoard()
        val initialBoardState = mutableMapOf<String, CellState>()
        for (row in board.indices) {
            for (col in board[row].indices) {
                val cell = board[row][col]
                val cellState = CellState(
                    showDotIndicator = false,
                    piece = cell.piece?.toShortFormat()
                )
                initialBoardState["${'A' + col}${8 - row}".lowercase(Locale.ROOT)] = cellState
            }
        }
        _gameState.value = GameUiState(
            boardState = initialBoardState,
            selectedPosition = null,
            winner = chessEngine.getWinner()
        )
    }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.CellTap -> {
                val updatedBoardState = _gameState.value.boardState.mapValues { (_, state) ->
                    state.copy(showDotIndicator = false)
                }.toMutableMap()

                if (_gameState.value.boardState[event.position]?.showDotIndicator == true &&
                    _gameState.value.selectedPosition != null
                ) {
                    chessEngine.makeMove(_gameState.value.selectedPosition!!, event.position)
                    refreshBoard()
                    return
                }

                if (_gameState.value.selectedPosition == event.position) {
                    _gameState.value = _gameState.value.copy(
                        boardState = updatedBoardState,
                        selectedPosition = null,
                        winner = chessEngine.getWinner()
                    )
                    return
                }

                val moves = chessEngine.getMoves(event.position)
                moves.forEach { move ->
                    updatedBoardState[move.lowercase(Locale.ROOT)] =
                        updatedBoardState[move.lowercase(Locale.ROOT)]?.copy(showDotIndicator = true)
                            ?: CellState(showDotIndicator = true, piece = null)
                }

                _gameState.value = _gameState.value.copy(
                    boardState = updatedBoardState,
                    selectedPosition = event.position,
                    winner = chessEngine.getWinner()
                )
            }

            GameEvent.Undo -> {
                chessEngine.undo()
                refreshBoard()
            }

            GameEvent.Reset -> {
                chessEngine.reset()
                refreshBoard()
            }
        }
    }
}

data class GameUiState(
    val boardState: Map<String, CellState> = mapOf(),
    val selectedPosition: String? = null,
    val winner: Color? = null
)

data class CellState(
    val showDotIndicator: Boolean,
    val piece: String?
)

sealed interface GameEvent {
    data class CellTap(val position: String) : GameEvent
    data object Undo : GameEvent
    data object Reset : GameEvent
}
