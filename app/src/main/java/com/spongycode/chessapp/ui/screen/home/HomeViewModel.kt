package com.spongycode.chessapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.spongycode.chessapp.model.Game
import com.spongycode.chessapp.model.PlayerColor
import com.spongycode.chessapp.repository.ChessRepository
import com.spongycode.chessapp.ui.screen.game.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ChessRepository,
    private val firebaseDatabase: FirebaseDatabase
) : ViewModel() {

    private val _viewEffect = MutableSharedFlow<HomeViewEffect>()
    val viewEffect = _viewEffect.asSharedFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.CreateGame -> {
                viewModelScope.launch {
                    _viewEffect.emit(HomeViewEffect.OnCreateGame)
                }
            }

            is HomeEvent.CreateGameConfirm -> {
                createGame(event.playerColor)
            }

            HomeEvent.JoinGame -> {
                viewModelScope.launch {
                    _viewEffect.emit(HomeViewEffect.OnJoinGame)
                }
            }

            is HomeEvent.JoinGameConfirm -> {
                viewModelScope.launch {
                    val joinSuccess = joinGameAsPlayer(event.gameId, repository.getUserId())
                    if (joinSuccess) {
                        _viewEffect.emit(HomeViewEffect.OnJoinGameConfirm(event.gameId))
                    }
                }
            }

            HomeEvent.PracticeGame -> {
                viewModelScope.launch {
                    _viewEffect.emit(HomeViewEffect.OnPracticeGame)
                }
            }
        }
    }

    private suspend fun joinGameAsPlayer(gameId: String, player2Id: String): Boolean {
        return try {
            val gameRef = firebaseDatabase.getReference("games")
            val snapshot = gameRef.child(gameId).get().await()
            if (snapshot.exists()) {
                val game = snapshot.getValue(Game::class.java)
                if (game?.player2 == null) {
                    val updates = mapOf(
                        "player2" to player2Id,
                        "status" to GameStatus.ONGOING.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    gameRef.child(gameId).updateChildren(updates).await()
                }
                println("Game joined successfully")
                true
            } else {
                println("Game does not exist!")
                false
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
            false
        }
    }

    private fun createGame(playerColor: PlayerColor) {
        attemptCreateGame(playerColor = playerColor)
    }

    private fun attemptCreateGame(
        playerColor: PlayerColor,
        maxRetries: Int = 5,
        currentRetry: Int = 0
    ) {
        val playerId = repository.getUserId()
        if (currentRetry >= maxRetries) {
            println("Failed to create game after $maxRetries retries")
            return
        }

        val gameId = getRandomGameId()
        val gameRef: DatabaseReference = firebaseDatabase.getReference("games").child(gameId)

        gameRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                attemptCreateGame(playerColor = playerColor, currentRetry = currentRetry + 1)
            } else {
                val game = Game(
                    player1 = playerId,
                    player1Color = playerColor.name,
                    status = GameStatus.WAITING_FOR_OPPONENT.name,
                    moves = listOf(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    whitePlayerTimeLeft = 600,
                    blackPlayerTimeLeft = 600
                )

                gameRef.setValue(game).addOnSuccessListener {
                    viewModelScope.launch {
                        _viewEffect.emit(HomeViewEffect.OnCreateGameConfirm(gameId))
                    }
                    println("Game created successfully with ID: $gameId")
                }.addOnFailureListener {
                    println("Failed to create game: ${it.message}")
                }
            }
        }.addOnFailureListener {
            println("Failed to check game ID: ${it.message}")
        }
    }

    private fun getRandomGameId(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..4)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

sealed interface HomeEvent {
    data object CreateGame : HomeEvent
    data object PracticeGame : HomeEvent
    data class CreateGameConfirm(val playerColor: PlayerColor) : HomeEvent
    data class JoinGameConfirm(val gameId: String) : HomeEvent
    data object JoinGame : HomeEvent
}

sealed interface HomeViewEffect {
    data object OnJoinGame : HomeViewEffect
    data class OnJoinGameConfirm(val gameId: String) : HomeViewEffect
    data object OnCreateGame : HomeViewEffect
    data class OnCreateGameConfirm(val gameId: String) : HomeViewEffect
    data object OnPracticeGame : HomeViewEffect
}