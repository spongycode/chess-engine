package com.spongycode.chess_core

data class HistoryMove(
    val move: Pair<String, String>,
    val removedPiece: ChessPiece?,
    val pawnPromotedPiece: ChessPiece?
)