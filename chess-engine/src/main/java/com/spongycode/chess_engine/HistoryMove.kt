package com.spongycode.chess_engine

data class HistoryMove(
    val move: Pair<String, String>,
    val removedPiece: ChessPiece?,
    val pawnPromotedPiece: ChessPiece?
)