package com.spongycode.chessapp.ui.screen.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.spongycode.chess_engine.ChessEngine
import com.spongycode.chess_engine.Player
import com.spongycode.chess_engine.toShortFormat
import com.spongycode.chessapp.model.Game
import com.spongycode.chessapp.model.Move
import com.spongycode.chessapp.model.PlayerColor
import com.spongycode.chessapp.repository.ChessRepository
import com.spongycode.chessapp.util.toPlayerColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: ChessRepository,
    private val firebaseDatabase: FirebaseDatabase
) : ViewModel() {
    private val chessEngine = ChessEngine()

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState = _gameState.asStateFlow()

    private val _viewEffect = MutableSharedFlow<GameViewEffect>()
    val viewEffect = _viewEffect.asSharedFlow()

    private val gameMoves = mutableListOf<Move>()
    private var player1: String? = null
    private var player2: String? = null
    private var player1Color: String? = null

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.CellTap -> {
                if ((repository.getUserId() !in listOf(player1, player2)) ||
                    (repository.getUserId() == player1 && _gameState.value.currentPlayer.toPlayerColor().name != player1Color) ||
                    (repository.getUserId() == player2 && _gameState.value.currentPlayer.toPlayerColor().name == player1Color) ||
                    (_gameState.value.winner != null)
                ) {
                    return
                }

                val updatedBoardState = _gameState.value.boardState.mapValues { (_, state) ->
                    state.copy(showDotIndicator = false)
                }.toMutableMap()

                if (_gameState.value.boardState[event.position.take(2)]?.showDotIndicator == true &&
                    _gameState.value.selectedPosition != null
                ) {
                    chessEngine.makeMove(_gameState.value.selectedPosition!!, event.position)
                    makeMoveToDatabase(
                        gameId = gameState.value.gameId,
                        move = Move(_gameState.value.selectedPosition!!, event.position)
                    )
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

            is GameEvent.JoinGameAtStart -> {
                _gameState.value = _gameState.value.copy(gameId = event.gameId)
                addListenerToGameId(event.gameId)
            }
        }
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
        val myColor: PlayerColor = when {
            repository.getUserId() == player1 -> if (player1Color == PlayerColor.WHITE.name) PlayerColor.WHITE else PlayerColor.BLACK
            repository.getUserId() == player2 -> if (player1Color == PlayerColor.WHITE.name) PlayerColor.BLACK else PlayerColor.WHITE
            else -> PlayerColor.BOTH
        }

        _gameState.value = _gameState.value.copy(
            myColor = myColor,
            boardState = initialBoardState,
            selectedPosition = null,
            winner = chessEngine.getWinner(),
            currentPlayer = chessEngine.getCurrentPlayer()
        )
    }

    private fun makeMoveToDatabase(gameId: String, move: Move) {
        val gameRef: DatabaseReference = firebaseDatabase.getReference("games")
        gameRef.child(gameId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val game = snapshot.getValue(Game::class.java)
                    if (game != null) {
                        val currentMoves = game.moves?.toMutableList()
                        currentMoves?.add(move)
                        val updates = mapOf(
                            "moves" to currentMoves,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        gameRef.child(gameId).updateChildren(updates).addOnSuccessListener {
                            println("Move added successfully")
                        }.addOnFailureListener {
                            println("Failed to add move: ${it.message}")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Database error: ${error.message}")
            }
        })
    }

    private fun addListenerToGameId(gameId: String) {
        val gameRef: DatabaseReference = firebaseDatabase.getReference("games")
        gameRef.child(gameId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val game = snapshot.getValue(Game::class.java)
                    game?.let {
                        player1 = game.player1
                        player2 = game.player2
                        player1Color = game.player1Color
                        _gameState.value =
                            _gameState.value.copy(gameStatus = game.status.toString())
                        when (it.status) {
                            GameStatus.ONGOING.name, GameStatus.WAITING_FOR_OPPONENT.name -> {
                                val moves = it.moves
                                processMoves(moves?.takeLast(moves.size - gameMoves.size))
                            }

                            else -> {

                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Database error: ${error.message}")
            }
        })
    }

    private fun processMoves(moves: List<Move>?) {
        moves?.let {
            for (move in moves) {
                chessEngine.makeMove(move.from, move.to)
                gameMoves.add(move)
            }
        }
        refreshBoard()
    }
}

enum class GameStatus {
    WAITING_FOR_OPPONENT,
    ONGOING,
    WHITE_WON,
    BLACK_WON,
    STALEMATE,
    DRAW
}

data class GameUiState(
    val gameId: String = "",
    val myColor: PlayerColor? = null,
    val boardState: Map<String, CellState> = mapOf(),
    val gameStatus: String = GameStatus.WAITING_FOR_OPPONENT.name,
    val selectedPosition: String? = null,
    val winner: Player? = null,
    val currentPlayer: Player = Player.WHITE
)

data class CellState(
    val showDotIndicator: Boolean,
    val piece: String?,
    val showPawnPromotionDialog: Boolean
)

sealed interface GameEvent {
    data class CellTap(val position: String) : GameEvent
    data class JoinGameAtStart(val gameId: String) : GameEvent
    data class PawnPromotion(val position: String) : GameEvent
    data object Undo : GameEvent
    data object Reset : GameEvent
    data object ResetConfirm : GameEvent
}

sealed interface GameViewEffect {
    data class OnPawnPromotion(val position: String) : GameViewEffect
    data object OnReset : GameViewEffect
}