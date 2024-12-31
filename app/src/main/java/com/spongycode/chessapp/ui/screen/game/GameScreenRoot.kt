package com.spongycode.chessapp.ui.screen.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.spongycode.chess_engine.Player.BLACK
import com.spongycode.chess_engine.Player.WHITE
import com.spongycode.chessapp.R
import com.spongycode.chessapp.model.PlayerColor
import com.spongycode.chessapp.util.getResource
import com.spongycode.chessapp.util.toPlayerColor
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@Composable
fun GameScreenRoot(
    modifier: Modifier = Modifier,
    gameId: String,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.gameState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showPawnPromotionDialog by remember { mutableStateOf(false) }
    var pawnPromotionPosition by remember { mutableStateOf("") }
    var showGameWelcomeDialog by remember { mutableStateOf(true) }
    var showGameEndDialog by remember { mutableStateOf(false) }

    GameScreen(
        modifier = modifier,
        gameId = gameId,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )

    LaunchedEffect(null) {
        viewModel.viewEffect.collectLatest {
            when (it) {
                is GameViewEffect.OnPawnPromotion -> {
                    pawnPromotionPosition = it.position
                    showPawnPromotionDialog = true
                }

                GameViewEffect.OnReset -> {
                    showResetDialog = true
                }

                GameViewEffect.OnGameEnd -> {
                    showGameWelcomeDialog = false
                    showGameEndDialog = true
                }
            }
        }
    }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                viewModel.onEvent(GameEvent.ResetConfirm)
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showPawnPromotionDialog) {
        PawnPromotionDialog(
            currentPlayer = uiState.currentPlayer,
            onConfirm = { promotedPieceChar ->
                viewModel.onEvent(GameEvent.CellTap("$pawnPromotionPosition$promotedPieceChar"))
                showPawnPromotionDialog = false
            },
            onDismiss = { showPawnPromotionDialog = false }
        )
    }

    if (showGameWelcomeDialog) {
        GameWelcomeDialog(
            playerColor = uiState.myColor,
            gameId = gameId,
            onConfirm = {
                showGameWelcomeDialog = false
            }
        )
    }

    if (showGameEndDialog) {
        GameEndDialog(
            winner = uiState.winner,
            playerColor = uiState.myColor,
            onConfirm = {
                showGameEndDialog = false
            }
        )
    }
}


@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameId: String,
    uiState: GameUiState = GameUiState(),
    onEvent: (GameEvent) -> Unit = {}
) {
    LaunchedEffect(null) {
        onEvent(GameEvent.JoinGameAtStart(gameId))
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = "Invite others to \nwatch the game.", fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
            CopyToClipboardButton(
                bgColor = Color(0xFFF3D6BB),
                gameId = gameId
            )
        }
        ChessBoardCompose(
            uiState = uiState,
            onEvent = onEvent
        )
        GameWalkthroughControls(
            onDoubleLeftClick = { onEvent(GameEvent.OnGameWalkthroughClick(GameWalkthroughOption.DOUBLE_LEFT)) },
            onLeftClick = { onEvent(GameEvent.OnGameWalkthroughClick(GameWalkthroughOption.LEFT)) },
            onRightClick = { onEvent(GameEvent.OnGameWalkthroughClick(GameWalkthroughOption.RIGHT)) },
            onDoubleRightClick = { onEvent(GameEvent.OnGameWalkthroughClick(GameWalkthroughOption.DOUBLE_RIGHT)) }
        )
        if (false && uiState.gameStatus == GameStatus.ONGOING.name) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onEvent(GameEvent.Reset) },
                    modifier = Modifier
                        .height(50.dp)
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9284A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Reset",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { onEvent(GameEvent.Undo) },
                    modifier = Modifier
                        .height(50.dp)
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3538EF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Undo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        if (uiState.gameStatus == GameStatus.WAITING_FOR_OPPONENT.name) {
            GameInfo(bgColor = Color(0xFFC9C7C7), text = "Waiting for opponent to join..")
        }
        if (uiState.winner != null) {
            GameInfo(
                bgColor = Color(
                    if (uiState.winner == PlayerColor.BOTH) 0xFF383838
                    else if (uiState.winner == uiState.myColor) 0xFF3F8526
                    else if (uiState.myColor == PlayerColor.BOTH) 0xFF383838
                    else 0xFFD9284A
                ),
                text = when (uiState.winner) {
                    PlayerColor.BOTH -> "Draw"
                    uiState.myColor -> "You Won!"
                    PlayerColor.WHITE -> "White Won!"
                    else -> "Black Won!"
                }
            )
        } else {
            if (uiState.myColor == PlayerColor.BOTH) {
                GameInfo(bgColor = Color(0xFF949393), text = "Spectating...")
            }
        }
        if (uiState.winner != null) {
            GameInfo(bgColor = Color(0xFF949393), text = "Game Ended")
        }
    }
}

@Composable
fun GameInfo(
    bgColor: Color, text: String = "Game Info"
) {
    Text(
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(15.dp),
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Composable
fun ChessBoardCompose(
    uiState: GameUiState = GameUiState(),
    onEvent: (GameEvent) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    Column {
        uiState.myColor?.let {
            if (it == PlayerColor.BOTH) PlayerTurnIndicator(
                isMine = false,
                isMyTurn = uiState.currentPlayer.toPlayerColor() == PlayerColor.BLACK,
                myColor = PlayerColor.BLACK,
                viewerMode = true,
                timeLeft = uiState.blackPlayerTimeLeft
            ) else {
                PlayerTurnIndicator(
                    isMine = false,
                    isMyTurn = uiState.currentPlayer.toPlayerColor() == uiState.myColor,
                    myColor = uiState.myColor,
                    timeLeft = if (uiState.myColor == PlayerColor.WHITE) uiState.blackPlayerTimeLeft else uiState.whitePlayerTimeLeft,
                    viewerMode = false
                )
            }
        }
        Box(modifier = Modifier.clipToBounds()) {
            LazyColumn {
                for (row in if (uiState.myColor == PlayerColor.WHITE || uiState.myColor == PlayerColor.BOTH)
                    8 downTo 1 else
                    1..8) {
                    item {
                        LazyRow {
                            for (col in if (uiState.myColor == PlayerColor.WHITE || uiState.myColor == PlayerColor.BOTH)
                                'A'..'H' else
                                'H' downTo 'A') {
                                val position = "${col}${row}".lowercase(Locale.ROOT)
                                item {
                                    ChessCell(
                                        modifier = Modifier
                                            .size((screenWidthDp / 8).dp, (screenWidthDp / 8).dp)
                                            .background(
                                                if (((row - 1) + (col - 'A')) % 2 == 0) Color(
                                                    0xFF769656
                                                ) else
                                                    Color(0xFFEEEED2)
                                            )
                                            .clickable {
                                                if (uiState.boardState[position]?.showPawnPromotionDialog == true) {
                                                    onEvent(GameEvent.PawnPromotion(position))
                                                } else {
                                                    onEvent(GameEvent.CellTap(position))
                                                }
                                            },
                                        piece = if (uiState.isGameWalkthroughMode)
                                            uiState.gameWalkthroughBoardState[position]?.piece
                                                ?: "" else
                                            uiState.boardState[position]?.piece ?: "",
                                        showDotIndicator = if (uiState.isGameWalkthroughMode)
                                            uiState.gameWalkthroughBoardState[position]?.showDotIndicator
                                                ?: false else
                                            uiState.boardState[position]?.showDotIndicator ?: false,
                                        isCellSelected = uiState.selectedPosition == position
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(if (uiState.isGameWalkthroughMode) Color(0x3BFFFFFF) else Color.Transparent)
            )
        }
        uiState.myColor?.let {
            if (it == PlayerColor.BOTH) PlayerTurnIndicator(
                isMine = false,
                isMyTurn = uiState.currentPlayer.toPlayerColor() == PlayerColor.WHITE,
                myColor = PlayerColor.WHITE,
                viewerMode = true,
                timeLeft = uiState.whitePlayerTimeLeft
            ) else {
                PlayerTurnIndicator(
                    isMine = true,
                    isMyTurn = uiState.currentPlayer.toPlayerColor() == uiState.myColor,
                    myColor = uiState.myColor,
                    timeLeft = if (uiState.myColor == PlayerColor.WHITE) uiState.whitePlayerTimeLeft else uiState.blackPlayerTimeLeft,
                    viewerMode = false
                )
            }
        }
    }
}

@Composable
fun ChessCell(
    modifier: Modifier = Modifier,
    piece: String = "",
    showDotIndicator: Boolean = false,
    isCellSelected: Boolean = false
) {
    Box(
        modifier = modifier.background(
            if (isCellSelected) Color(0xA3F3E164) else Color.Transparent
        ),
        contentAlignment = Alignment.Center
    ) {
        if (showDotIndicator) {
            if (piece.isNotBlank()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                ) {
                    val strokeWidth = 5.dp.toPx()
                    drawCircle(
                        color = Color(0x574F4E4E),
                        style = Stroke(width = strokeWidth),
                        radius = size.minDimension / 2 - strokeWidth / 2
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.3f)
                        .clip(CircleShape)
                        .background(Color(0x574F4E4E))
                )
            }
        }

        if (piece.isNotBlank()) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                painter = painterResource(piece.getResource()),
                contentDescription = null
            )
        }
    }
}


@Composable
fun PawnPromotionDialog(
    currentPlayer: com.spongycode.chess_engine.Player,
    onConfirm: (Char) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val pieces = listOf('Q', 'R', 'B', 'N')
    val colors = listOf(Color(0xFF769656), Color(0xFFEEEED2))

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
                    text = "Promote pawn to:",
                    fontWeight = FontWeight.W800,
                    fontSize = 22.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(25.dp))

                pieces.chunked(2).forEachIndexed { rowIndex, rowPieces ->
                    Row {
                        rowPieces.forEachIndexed { colIndex, pieceChar ->
                            ChessCell(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors[(rowIndex + colIndex) % 2])
                                    .clickable { onConfirm(pieceChar) },
                                piece = if (currentPlayer == WHITE) "W$pieceChar" else "B$pieceChar"
                            )
                            if (colIndex == 0) Spacer(modifier = Modifier.width(30.dp))
                        }
                    }
                    if (rowIndex == 0) Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(25.dp))
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9284A))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun GameWelcomeDialog(
    playerColor: PlayerColor? = PlayerColor.BOTH,
    gameId: String,
    onConfirm: () -> Unit = {}
) {
    val initialText = when (playerColor) {
        PlayerColor.WHITE -> buildAnnotatedString {
            append("You are playing as ")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("White")
            }
            append(".")
        }

        PlayerColor.BLACK -> buildAnnotatedString {
            append("You are playing as ")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("Black")
            }
            append(".")
        }

        else -> buildAnnotatedString {
            append("You are ")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("spectating")
            }
            append(" the game.")
        }
    }


    Dialog(onDismissRequest = onConfirm) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 30.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to the chess board",
                    fontWeight = FontWeight.W800,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(25.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Center,
                    text = initialText,
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Center,
                    text = if (playerColor != PlayerColor.BOTH) "Your game will begin as soon as your opponent joins."
                    else "The game will begin as soon as both players join",
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(20.dp))
                CopyToClipboardButton(
                    bgColor = Color(0xFFF3F1F1),
                    gameId = gameId
                )
                Spacer(modifier = Modifier.height(25.dp))
                Button(
                    onClick = { onConfirm() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F8526))
                ) {
                    Text("Join Game", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun GameEndDialog(
    winner: PlayerColor? = PlayerColor.BOTH,
    playerColor: PlayerColor? = PlayerColor.BOTH,
    onConfirm: () -> Unit = {}
) {
    val winnerText = if (winner == PlayerColor.BOTH) {
        "Draw"
    } else if (winner == playerColor) {
        "You Won!"
    } else {
        if (winner == PlayerColor.WHITE) "White Won!" else "Black Won!"
    }

    Dialog(onDismissRequest = onConfirm) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp, bottom = 50.dp, top = 10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onConfirm()
                        }
                        .padding(5.dp)
                        .size(25.dp)
                        .align(Alignment.End),
                    contentDescription = "Close",
                    tint = Color.Black
                )

                Text(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Center,
                    text = winnerText,
                    fontWeight = FontWeight.W800,
                    fontSize = 30.sp,
                    color = Color(
                        if (winner == PlayerColor.BOTH) 0xFF000000
                        else if (winner == playerColor) 0xFF34A90A
                        else if (playerColor != PlayerColor.BOTH) 0xFFD9284A
                        else 0xFF000000
                    )
                )
                Spacer(modifier = Modifier.height(30.dp))

                if (winner != PlayerColor.BOTH) {
                    ChessCell(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (winner == PlayerColor.WHITE) Color(0xFF769656) else
                                    Color(0xFFEEEED2)
                            ),
                        piece = if (winner == PlayerColor.WHITE) "WK" else "BK",
                    )
                } else {
                    Row {
                        ChessCell(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color(0xFF769656)),
                            piece = "WK",
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        ChessCell(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color(0xFFEEEED2)),
                            piece = "BK",
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CopyToClipboardButton(
    bgColor: Color = Color.White,
    gameId: String
) {
    val clipboardManager = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                clipboardManager.setText(AnnotatedString(gameId))
            }
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = gameId,
                fontWeight = FontWeight.W500,
                fontSize = 25.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_copy),
                contentDescription = "Copy Game ID",
                tint = Color(0xFF3538EF)
            )
        }
        Text(
            text = "Game ID", fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reset Game",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Are you sure you want to reset the game? This action cannot be undone.",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9284A))
                    ) {
                        Text("Reset", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerTurnIndicator(
    isMine: Boolean,
    isMyTurn: Boolean,
    timeLeft: Int = 300,
    myColor: PlayerColor?,
    viewerMode: Boolean? = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF6F6))
            .padding(10.dp),
        horizontalArrangement = if (isMine || (viewerMode == true && myColor == PlayerColor.WHITE)) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val topDisplay = (isMyTurn && isMine && viewerMode == false) ||
                (isMyTurn && viewerMode == true && myColor == PlayerColor.WHITE)
        val bottomDisplay = (!isMyTurn && !isMine && viewerMode == false) ||
                (isMyTurn && viewerMode == true && myColor == PlayerColor.BLACK)

        val isTimerActive = topDisplay || bottomDisplay

        if (topDisplay) {
            Text(
                text = if (viewerMode == true) "White's turn.." else "Your move..",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(22.dp))
        }
        val piece = if (viewerMode == true) {
            if (myColor == PlayerColor.WHITE) "WK" else "BK"
        } else if (isMine) {
            if (myColor == PlayerColor.WHITE) "WK" else "BK"
        } else {
            if (myColor == PlayerColor.WHITE) "BK" else "WK"
        }
        val timerOnTop =
            (viewerMode == true && myColor == PlayerColor.BLACK) ||
                    (viewerMode == false && myColor == PlayerColor.WHITE && !isMine) ||
                    (viewerMode == false && myColor == PlayerColor.BLACK && !isMine)

        if (!timerOnTop) {
            Timer(timeLeft, isTimerActive)
            Spacer(modifier = Modifier.width(15.dp))
        }
        ChessCell(
            modifier = Modifier
                .size(50.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color(if (piece == "WK") 0xFF769656 else 0xFFEEEED2)),
            piece = piece
        )

        if (timerOnTop) {
            Spacer(modifier = Modifier.width(15.dp))
            Timer(timeLeft, isTimerActive)
        }

        if (bottomDisplay) {
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = if (viewerMode == true) "Black's turn.." else "Opponent's turn..",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        }
    }
}

@Composable
fun Timer(
    timeLeft: Int,
    isTimerActive: Boolean = true
) {
    var minutes = (timeLeft / 60).toString()
    var seconds = (timeLeft % 60).toString()
    if (minutes.length == 1) {
        minutes = "0$minutes"
    }
    if (seconds.length == 1) {
        seconds = "0$seconds"
    }
    Text(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(
                Color(
                    if (isTimerActive) {
                        if (timeLeft <= 20) 0xFFD9284A
                        else 0xFF3F8526
                    } else 0xFFC9C7C7
                )
            )
            .padding(8.dp),
        text = "$minutes:$seconds",
        fontSize = 20.sp,
        fontWeight = FontWeight.W800,
        color = Color.White
    )
}

@Composable
fun GameWalkthroughControls(
    modifier: Modifier = Modifier,
    onDoubleLeftClick: () -> Unit = {},
    onLeftClick: () -> Unit = {},
    onRightClick: () -> Unit = {},
    onDoubleRightClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(5.dp)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Absolute.SpaceAround
    ) {
        Icon(
            painter = painterResource(R.drawable.keyboard_double_arrow_left),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    onDoubleLeftClick()
                }
                .background(Color(0xFF1287AB))
                .padding(vertical = 5.dp, horizontal = 12.dp)
                .size(30.dp),
            contentDescription = "double left",
            tint = Color.White
        )
        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_left),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    onLeftClick()
                }
                .background(Color(0xFF0F7896))
                .padding(vertical = 5.dp, horizontal = 12.dp)
                .size(30.dp),
            contentDescription = "left",
            tint = Color.White
        )

        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_right),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    onRightClick()
                }
                .background(Color(0xFF0F7896))
                .padding(vertical = 5.dp, horizontal = 12.dp)
                .size(30.dp),
            contentDescription = "right",
            tint = Color.White
        )
        Icon(
            painter = painterResource(R.drawable.keyboard_double_arrow_right),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    onDoubleRightClick()
                }
                .background(Color(0xFF1287AB))
                .padding(vertical = 5.dp, horizontal = 12.dp)
                .size(30.dp),
            contentDescription = "double right",
            tint = Color.White
        )
    }
}

@Preview
@Composable
private fun GameWalkthroughControlsPreview() {
    GameWalkthroughControls()
}

@Preview
@Composable
private fun TimerPreview() {
    Timer(10)
}

@Preview
@Composable
private fun PlayerTurnIndicatorPreview() {
    PlayerTurnIndicator(
        isMine = true,
        isMyTurn = true,
        myColor = PlayerColor.WHITE
    )
}

@Preview
@Composable
private fun PlayerTurnIndicatorOpponentPreview() {
    PlayerTurnIndicator(
        isMine = false,
        isMyTurn = false,
        myColor = PlayerColor.BLACK
    )
}

@Preview
@Composable
private fun PlayerTurnIndicatorOpponentBlackPreview() {
    PlayerTurnIndicator(
        isMine = false,
        isMyTurn = false,
        myColor = PlayerColor.WHITE
    )
}

@Preview
@Composable
private fun PlayerTurnIndicatorSpectatorWhitePreview() {
    PlayerTurnIndicator(
        isMine = false,
        isMyTurn = false,
        myColor = PlayerColor.WHITE,
        viewerMode = true
    )
}

@Preview
@Composable
private fun PlayerTurnIndicatorSpectatorBlackPreview() {
    PlayerTurnIndicator(
        isMine = false,
        isMyTurn = true,
        myColor = PlayerColor.BLACK,
        viewerMode = true
    )
}

@Preview
@Composable
private fun ChessCellBlackSelectedPreview() {
    ChessCell(
        modifier = Modifier
            .size(100.dp)
            .background(Color(0xFF769656)),
        showDotIndicator = false,
        piece = "BK",
        isCellSelected = true
    )
}

@Preview
@Composable
private fun ChessCellBlackPreview() {
    ChessCell(
        modifier = Modifier
            .size(100.dp)
            .background(Color(0xFF769656)),
        showDotIndicator = false,
        piece = "BK"
    )
}

@Preview
@Composable
private fun ChessCellWhitePreview() {
    ChessCell(
        modifier = Modifier
            .size(100.dp)
            .background(Color(0xFFEEEED2)),
        showDotIndicator = true
    )
}

@Preview
@Composable
private fun ChessCellWhiteWithIndicatorPreview() {
    ChessCell(
        modifier = Modifier
            .size(100.dp)
            .background(Color(0xFFEEEED2)),
        showDotIndicator = true,
        piece = "BK"
    )
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    GameScreen(gameId = "12345")
}

@Preview
@Composable
private fun PawnPromotionDialogPreview() {
    PawnPromotionDialog(
        currentPlayer = BLACK
    )
}

@Preview
@Composable
private fun GameWelcomeDialogPreview() {
    GameWelcomeDialog(gameId = "a2WI")
}

@Preview
@Composable
private fun GameEndDialogPreview() {
    GameEndDialog(
        playerColor = PlayerColor.WHITE,
        winner = PlayerColor.WHITE
    )
}

@Preview
@Composable
private fun GameEndDialogLosePreview() {
    GameEndDialog(
        playerColor = PlayerColor.WHITE,
        winner = PlayerColor.BLACK
    )
}

@Preview
@Composable
private fun GameEndDialogSpectatorPreview() {
    GameEndDialog(
        playerColor = PlayerColor.BOTH,
        winner = PlayerColor.WHITE
    )
}

@Preview
@Composable
private fun GameEndDialogBothPreview() {
    GameEndDialog(
        playerColor = PlayerColor.BOTH,
        winner = PlayerColor.BOTH
    )
}