package com.spongycode.chess_core

object Constants {
    const val BOARD_SIZE = 8
    val KNIGHT_MOVES = setOf(
        Pair(1, 2), Pair(1, -2),
        Pair(-1, 2), Pair(-1, -2),
        Pair(2, 1), Pair(2, -1),
        Pair(-2, 1), Pair(-2, -1)
    )
}