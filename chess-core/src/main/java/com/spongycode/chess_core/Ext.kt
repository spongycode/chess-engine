package com.spongycode.chess_core


/**
 * Returns in [row, col]
 */
fun String.transformToPair(): Pair<Int, Int> {
    val col = this[0].lowercaseChar() - 'a'
    val row = 8 - (this[1] - '0')
    return Pair(row, col)
}

fun ChessPiece.getColor(): Color {
    return when (this) {
        is ChessPiece.BlackChessPiece -> Color.BLACK
        is ChessPiece.WhiteChessPiece -> Color.WHITE
    }
}

fun ChessPiece?.toShortFormat(): String {
    if (this == null) {
        return "  "
    }
    return when (this) {
        is ChessPiece.BlackChessPiece -> {
            when (this.type) {
                ChessPiece.Type.KING -> "BK"
                ChessPiece.Type.QUEEN -> "BQ"
                ChessPiece.Type.ROOK -> "BR"
                ChessPiece.Type.KNIGHT -> "BN"
                ChessPiece.Type.BISHOP -> "BB"
                ChessPiece.Type.PAWN -> "BP"
            }
        }

        is ChessPiece.WhiteChessPiece -> {
            when (this.type) {
                ChessPiece.Type.KING -> "WK"
                ChessPiece.Type.QUEEN -> "WQ"
                ChessPiece.Type.ROOK -> "WR"
                ChessPiece.Type.KNIGHT -> "WN"
                ChessPiece.Type.BISHOP -> "WB"
                ChessPiece.Type.PAWN -> "WP"
            }
        }
    }
}