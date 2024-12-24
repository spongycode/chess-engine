package com.spongycode.chess_core


sealed class ChessPiece {
    data class BlackChessPiece(val type: Type) : ChessPiece()
    data class WhiteChessPiece(val type: Type) : ChessPiece()

    enum class Type {
        KING, QUEEN, ROOK, KNIGHT, BISHOP, PAWN
    }
}
