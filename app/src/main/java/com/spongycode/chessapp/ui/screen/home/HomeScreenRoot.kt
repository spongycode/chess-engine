package com.spongycode.chessapp.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.spongycode.chessapp.model.PlayerColor
import com.spongycode.chessapp.ui.navigation.LocalNavController
import com.spongycode.chessapp.ui.screen.game.ChessCell
import com.spongycode.chessapp.util.Constants.GAME_SCREEN
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    var showJoinGameDialog by remember { mutableStateOf(false) }
    var showCreateGameDialog by remember { mutableStateOf(false) }

    HomeScreen(
        modifier = modifier,
        onEvent = viewModel::onEvent
    )

    LaunchedEffect(null) {
        viewModel.viewEffect.collectLatest {
            when (it) {
                HomeViewEffect.OnJoinGame -> {
                    showJoinGameDialog = true
                }

                is HomeViewEffect.OnJoinGameConfirm -> {
                    showJoinGameDialog = false
                    navController.navigate("$GAME_SCREEN/${it.gameId}")
                }

                HomeViewEffect.OnCreateGame -> {
                    showCreateGameDialog = true
                }

                is HomeViewEffect.OnCreateGameConfirm -> {
                    showCreateGameDialog = false
                    navController.navigate("$GAME_SCREEN/${it.gameId}")
                }
            }
        }
    }

    if (showJoinGameDialog) {
        JoinGameDialog(
            onConfirm = { viewModel.onEvent(HomeEvent.JoinGameConfirm(it)) },
            onDismiss = { showJoinGameDialog = false }
        )
    }

    if (showCreateGameDialog) {
        CreateGameDialog(
            onConfirm = { playerColor -> viewModel.onEvent(HomeEvent.CreateGameConfirm(playerColor)) },
            onDismiss = { showCreateGameDialog = false }
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onEvent: (HomeEvent) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                onEvent(HomeEvent.CreateGame)
            },
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.6f)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F8526)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Create Game",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(25.dp))
        Button(
            onClick = {
                onEvent(HomeEvent.JoinGame)
            },
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.6f)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3538EF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Join Game",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun JoinGameDialog(
    onConfirm: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var gameId by rememberSaveable { mutableStateOf("") }
    var showJoinGameLoader by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter game id:",
                    fontWeight = FontWeight.W800,
                    fontSize = 22.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(25.dp))
                OutlinedTextField(
                    value = gameId,
                    onValueChange = { gameId = it },
                    placeholder = {
                        Text(text = "Game id", color = Color.Gray)
                    },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W800
                    ),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (gameId.isNotEmpty()) {
                                showJoinGameLoader = true
                                onConfirm(gameId)
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.height(25.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9284A))
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        onClick = {
                            if (gameId.isNotEmpty()) {
                                showJoinGameLoader = true
                                onConfirm(gameId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3538EF))
                    ) {
                        Text("Join", color = Color.White)
                    }
                }
                if (showJoinGameLoader) {
                    Spacer(modifier = Modifier.height(25.dp))
                    Text("Joining game...", color = Color.Gray)
                }
            }
        }
    }
}


@Composable
fun CreateGameDialog(
    onConfirm: (PlayerColor) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var showCreateGameLoader by remember { mutableStateOf(false) }
    var whiteKingSelected by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create new game?",
                    fontWeight = FontWeight.W800,
                    fontSize = 22.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(35.dp))

                Row {
                    ChessCell(
                        modifier = Modifier
                            .border(
                                if (whiteKingSelected) 4.dp
                                else 0.dp, Color.Black
                            )
                            .size(70.dp)
                            .background(Color(0xFF769656))
                            .clickable { whiteKingSelected = true },
                        piece = "WK"
                    )
                    Spacer(modifier = Modifier.width(35.dp))
                    ChessCell(
                        modifier = Modifier
                            .border(
                                if (!whiteKingSelected) 4.dp
                                else 0.dp, Color.Black
                            )
                            .size(70.dp)
                            .background(Color(0xFFEEEED2))
                            .clickable { whiteKingSelected = false },
                        piece = "BK"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Pick your side",
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(25.dp))
                Row {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9284A))
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        onClick = {
                            showCreateGameLoader = true
                            onConfirm(if (whiteKingSelected) PlayerColor.WHITE else PlayerColor.BLACK)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F8526))
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                }
                if (showCreateGameLoader) {
                    Spacer(modifier = Modifier.height(25.dp))
                    Text("Creating game...", color = Color.Gray)
                }
            }
        }
    }
}


@Preview
@Composable
private fun JoinGameDialogPreview() {
    JoinGameDialog()
}

@Preview
@Composable
private fun CreateGameDialogPreview() {
    CreateGameDialog()
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}