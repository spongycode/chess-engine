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
import com.spongycode.chessapp.util.getResource
import java.util.Locale

@Composable
fun MainScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val uiState = viewModel.gameState.collectAsState().value
    MainScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: GameUiState = GameUiState(),
    onEvent: (GameEvent) -> Unit = {}
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray),
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
                onClick = { showResetDialog = true },
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

    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                onEvent(GameEvent.Reset)
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
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
                                        onEvent(GameEvent.CellTap(position))
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