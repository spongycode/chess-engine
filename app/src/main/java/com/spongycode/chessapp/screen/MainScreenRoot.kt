package com.spongycode.chessapp.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.spongycode.chess_engine.Player.*
import com.spongycode.chessapp.util.getResource
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@Composable
fun MainScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val uiState = viewModel.gameState.collectAsState().value
    var showResetDialog by remember { mutableStateOf(false) }
    var showPawnPromotionDialog by remember { mutableStateOf(false) }
    var pawnPromotionPosition by remember { mutableStateOf("") }

    MainScreen(
        modifier = modifier,
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
}


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: GameUiState = GameUiState(),
    onEvent: (GameEvent) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFADEDE)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChessBoardCompose(
            uiState = uiState,
            onEvent = onEvent
        )

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
        uiState.winner?.let {
            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (uiState.winner == WHITE) Color.Black else Color.White)
                    .padding(vertical = 5.dp, horizontal = 15.dp),
                fontSize = 25.sp,
                color = if (uiState.winner == WHITE) Color.White else Color.Black,
                fontWeight = FontWeight.W800,
                text = when (uiState.winner) {
                    WHITE -> "White won"
                    BOTH -> "Stalemate - Draw"
                    else -> "Black won"
                }
            )
        }
    }
}

@Composable
fun ChessBoardCompose(
    uiState: GameUiState = GameUiState(),
    onEvent: (GameEvent) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    LazyColumn {
        for (row in 8 downTo 1) {
            item {
                LazyRow {
                    for (col in 'A'..'H') {
                        val position = "${col}${row}".lowercase(Locale.ROOT)
                        item {
                            ChessCell(
                                modifier = Modifier
                                    .size((screenWidthDp / 8).dp, (screenWidthDp / 8).dp)
                                    .background(
                                        if (((row - 1) + (col - 'A')) % 2 == 0) Color(0xFF769656) else
                                            Color(0xFFEEEED2)
                                    )
                                    .clickable {
                                        if (uiState.boardState[position]?.showPawnPromotionDialog == true) {
                                            onEvent(GameEvent.PawnPromotion(position))
                                        } else {
                                            onEvent(GameEvent.CellTap(position))
                                        }
                                    },
                                piece = uiState.boardState[position]?.piece ?: "",
                                showDotIndicator = uiState.boardState[position]?.showDotIndicator
                                    ?: false,
                                isCellSelected = uiState.selectedPosition == position
                            )
                        }
                    }
                }
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
                        .size(20.dp)
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
    MainScreen()
}

@Preview
@Composable
private fun PawnPromotionDialogPreview() {
    PawnPromotionDialog(
        currentPlayer = BLACK
    )
}