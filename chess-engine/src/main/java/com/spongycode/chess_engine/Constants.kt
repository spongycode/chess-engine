package com.spongycode.chess_engine

object Constants {
    const val BOARD_SIZE = 8
    const val CACHE_SIZE = 8
    val KNIGHT_MOVES = setOf(
        Pair(1, 2), Pair(1, -2),
        Pair(-1, 2), Pair(-1, -2),
        Pair(2, 1), Pair(2, -1),
        Pair(-2, 1), Pair(-2, -1)
    )
    val KING_MOVES = setOf(
        Pair(-1, 0), Pair(-1, -1), Pair(-1, 1),
        Pair(1, 0), Pair(1, -1), Pair(1, 1),
        Pair(0, -1), Pair(0, 1),
        Pair(0, 2), Pair(0, -2)
    )
    val PAWN_MOVES = setOf(
        Pair(-2, 0), Pair(-1, 0),
        Pair(-1, -1), Pair(-1, 1)
    )
}