package com.spongycode.chessapp.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spongycode.chess_core.ChessEngine
import com.spongycode.chess_core.Color
import com.spongycode.chess_core.toShortFormat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel : ViewModel() {
    private val chessEngine = ChessEngine()

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState = _gameState.asStateFlow()

    private val _viewEffect = MutableSharedFlow<GameViewEffect>()
    val viewEffect = _viewEffect.asSharedFlow()

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
                    piece = cell.piece?.toShortFormat(),
                    showPawnPromotionDialog = false
                )
                initialBoardState["${'A' + col}${8 - row}".lowercase(Locale.ROOT)] = cellState
            }
        }
        _gameState.value = GameUiState(
            boardState = initialBoardState,
            selectedPosition = null,
            winner = chessEngine.getWinner(),
            currentPlayer = chessEngine.getCurrentPlayer()
        )
    }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.CellTap -> {
                val updatedBoardState = _gameState.value.boardState.mapValues { (_, state) ->
                    state.copy(showDotIndicator = false)
                }.toMutableMap()

                if (_gameState.value.boardState[event.position.take(2)]?.showDotIndicator == true &&
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
                    updatedBoardState[move.take(2).lowercase(Locale.ROOT)] =
                        updatedBoardState[move.take(2).lowercase(Locale.ROOT)]?.copy(
                            showDotIndicator = true,
                            showPawnPromotionDialog = move.last() == '+'
                        )
                            ?: CellState(
                                showDotIndicator = true,
                                piece = null,
                                showPawnPromotionDialog = move.last() == '+'
                            )
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
                viewModelScope.launch {
                    _viewEffect.emit(GameViewEffect.OnReset)
                }
            }

            GameEvent.ResetConfirm -> {
                chessEngine.reset()
                refreshBoard()
            }

            is GameEvent.PawnPromotion -> {
                viewModelScope.launch {
                    _viewEffect.emit(GameViewEffect.OnPawnPromotion(event.position))
                }
            }
        }
    }
}

data class GameUiState(
    val boardState: Map<String, CellState> = mapOf(),
    val selectedPosition: String? = null,
    val winner: Color? = null,
    val currentPlayer: Color = Color.WHITE
)

data class CellState(
    val showDotIndicator: Boolean,
    val piece: String?,
    val showPawnPromotionDialog: Boolean
)

sealed interface GameEvent {
    data class CellTap(val position: String) : GameEvent
    data class PawnPromotion(val position: String) : GameEvent
    data object Undo : GameEvent
    data object Reset : GameEvent
    data object ResetConfirm : GameEvent
}

sealed interface GameViewEffect {
    data class OnPawnPromotion(val position: String) : GameViewEffect
    data object OnReset : GameViewEffect
}
