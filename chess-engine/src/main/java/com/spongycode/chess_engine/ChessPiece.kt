package com.spongycode.chess_engine

sealed class ChessPiece {
    abstract val type: Type
    data class BlackChessPiece(override val type: Type) : ChessPiece()
    data class WhiteChessPiece(override val type: Type) : ChessPiece()

    enum class Type {
        KING, QUEEN, ROOK, KNIGHT, BISHOP, PAWN
    }
}