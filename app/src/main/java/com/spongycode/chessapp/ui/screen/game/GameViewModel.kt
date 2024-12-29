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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

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
    private var timerJob: Job? = null

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.CellTap -> {
                if ((repository.getUserId() !in listOf(player1, player2)) ||
                    (repository.getUserId() == player1 && _gameState.value.currentPlayer.toPlayerColor().name != player1Color) ||
                    (repository.getUserId() == player2 && _gameState.value.currentPlayer.toPlayerColor().name == player1Color) ||
                    (_gameState.value.winner != null) ||
                    (_gameState.value.gameStatus != GameStatus.ONGOING.name)
                ) {
                    return
                }

                val updatedBoardState = _gameState.value.boardState.mapValues { (_, state) ->
                    state.copy(showDotIndicator = false)
                }.toMutableMap()

                if (_gameState.value.boardState[event.position.take(2)]?.showDotIndicator == true &&
                    _gameState.value.selectedPosition != null
                ) {
                    cancelTimer()
                    val timeLeft = Pair(
                        if (chessEngine.getCurrentPlayer()
                                .toPlayerColor() == PlayerColor.WHITE
                        ) "whitePlayerTimeLeft"
                        else "blackPlayerTimeLeft",
                        if (chessEngine.getCurrentPlayer()
                                .toPlayerColor() == PlayerColor.WHITE
                        ) _gameState.value.whitePlayerTimeLeft
                        else gameState.value.blackPlayerTimeLeft
                    )
                    chessEngine.makeMove(_gameState.value.selectedPosition!!, event.position)
                    makeMoveToDatabase(
                        gameId = gameState.value.gameId,
                        move = Move(_gameState.value.selectedPosition!!, event.position),
                        timeLeft = timeLeft
                    )
                    refreshBoard()
                    return
                }

                if (_gameState.value.selectedPosition == event.position) {
                    _gameState.value = _gameState.value.copy(
                        boardState = updatedBoardState,
                        selectedPosition = null,
                        winner = chessEngine.getWinner()?.toPlayerColor()
                    )
                    emitWinner()
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
                    winner = chessEngine.getWinner()?.toPlayerColor()
                )
                emitWinner()
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

    private fun emitWinner() {
        if (_gameState.value.winner != null) {
            viewModelScope.launch {
                _viewEffect.emit(GameViewEffect.OnGameEnd)
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
            winner = chessEngine.getWinner()?.toPlayerColor(),
            currentPlayer = chessEngine.getCurrentPlayer()
        )
        emitWinner()
    }

    private fun makeMoveToDatabase(gameId: String, move: Move, timeLeft: Pair<String, Int>) {
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
                            "updatedAt" to System.currentTimeMillis(),
                            timeLeft
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
                        cancelTimer()
                        _gameState.value = _gameState.value.copy(
                            gameStatus = game.status.toString(),
                            whitePlayerTimeLeft = game.whitePlayerTimeLeft,
                            blackPlayerTimeLeft = game.blackPlayerTimeLeft
                        )
                        when (it.status) {
                            GameStatus.WAITING_FOR_OPPONENT.name -> {
                                val moves = it.moves
                                processMoves(moves?.takeLast(moves.size - gameMoves.size))
                            }

                            GameStatus.ONGOING.name -> {
                                resumeTimer()
                                val moves = it.moves
                                processMoves(moves?.takeLast(moves.size - gameMoves.size))
                            }

                            GameStatus.WHITE_WON.name -> {
                                resumeTimer()
                                val moves = it.moves
                                processMoves(moves?.takeLast(moves.size - gameMoves.size))
                                _gameState.value = _gameState.value.copy(winner = PlayerColor.WHITE)
                                emitWinner()
                            }

                            GameStatus.BLACK_WON.name -> {
                                resumeTimer()
                                val moves = it.moves
                                processMoves(moves?.takeLast(moves.size - gameMoves.size))
                                _gameState.value = _gameState.value.copy(winner = PlayerColor.BLACK)
                                emitWinner()
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

    private fun cancelTimer() {
        timerJob?.cancel()
    }

    private suspend fun updateGameOverState(
        gameId: String,
        param1: Pair<String, String>?,
        param2: Pair<String, Int>
    ) {
        try {
            val gameRef = firebaseDatabase.getReference("games")
            val snapshot = gameRef.child(gameId).get().await()
            if (snapshot.exists()) {
                if (param1 == null) {
                    val updates = mapOf(param2)
                    gameRef.child(gameId).updateChildren(updates).await()
                    println("Timer updated successfully")
                } else {
                    val updates = mapOf(param1, param2)
                    gameRef.child(gameId).updateChildren(updates).await()
                    println("Game Over updated successfully")
                }
            } else {
                println("Game does not exist!")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    private fun resumeTimer() {
        if (_gameState.value.winner == null) {
            timerJob = viewModelScope.launch {
                var elapsedSeconds = 0
                while (true) {
                    delay(1000)
                    elapsedSeconds += 1
                    if (_gameState.value.currentPlayer == Player.WHITE) {
                        _gameState.value = _gameState.value.copy(
                            whitePlayerTimeLeft = max(0, _gameState.value.whitePlayerTimeLeft - 1)
                        )
                    } else {
                        _gameState.value = _gameState.value.copy(
                            blackPlayerTimeLeft = max(0, _gameState.value.blackPlayerTimeLeft - 1)
                        )
                    }

                    if (elapsedSeconds % 5 == 0) {
                        updateTimeOnServer()
                    }

                    if (_gameState.value.whitePlayerTimeLeft == 0 || _gameState.value.blackPlayerTimeLeft == 0) {
                        updateTimeOnServer()
                        cancel()
                    }
                }
            }
        }
    }

    private fun updateTimeOnServer() {
        when (_gameState.value.currentPlayer) {
            Player.BLACK -> {
                if (_gameState.value.blackPlayerTimeLeft == 0) {
                    _gameState.value = _gameState.value.copy(winner = PlayerColor.WHITE)
                    viewModelScope.launch {
                        _viewEffect.emit(GameViewEffect.OnGameEnd)
                        updateGameOverState(
                            _gameState.value.gameId,
                            "status" to GameStatus.WHITE_WON.name,
                            "blackPlayerTimeLeft" to 0
                        )
                    }
                } else {
                    viewModelScope.launch {
                        updateGameOverState(
                            _gameState.value.gameId,
                            param1 = null,
                            param2 = "blackPlayerTimeLeft" to _gameState.value.blackPlayerTimeLeft
                        )
                    }
                }
            }

            Player.WHITE -> {
                if (_gameState.value.whitePlayerTimeLeft == 0) {
                    _gameState.value = _gameState.value.copy(winner = PlayerColor.BLACK)
                    viewModelScope.launch {
                        _viewEffect.emit(GameViewEffect.OnGameEnd)
                        updateGameOverState(
                            _gameState.value.gameId,
                            "status" to GameStatus.BLACK_WON.name,
                            "whitePlayerTimeLeft" to 0
                        )
                    }
                } else {
                    viewModelScope.launch {
                        updateGameOverState(
                            _gameState.value.gameId,
                            param1 = null,
                            param2 = "whitePlayerTimeLeft" to _gameState.value.whitePlayerTimeLeft
                        )
                    }
                }
            }

            else -> {
                return
            }
        }

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
    BLACK_WON,
    WHITE_WON
}

data class GameUiState(
    val gameId: String = "",
    val myColor: PlayerColor? = null,
    val boardState: Map<String, CellState> = mapOf(),
    val gameStatus: String = GameStatus.WAITING_FOR_OPPONENT.name,
    val selectedPosition: String? = null,
    val winner: PlayerColor? = null,
    val currentPlayer: Player = Player.WHITE,
    val whitePlayerTimeLeft: Int = 300,
    val blackPlayerTimeLeft: Int = 300
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
    data object OnGameEnd : GameViewEffect
}