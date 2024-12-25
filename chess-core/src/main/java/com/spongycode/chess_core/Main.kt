package com.spongycode.chess_core

fun main() {
    val chessBoard = ChessBoard()
    val board = chessBoard.getBoard()

    val chessGame = ChessGame(chessBoard = board)

    while (chessGame.getWinner() == null) {
        chessGame.printBoard()
        try {
            val input = readln().split(" ")
            if (input.size == 2) {
                val start = input[0]
                val end = input[1]
                chessGame.makeMove(start, end)
            } else {
                val start = input[0]
                val moves = chessGame.getMoves(start)
                println(moves)
            }
        } catch (e: Exception) {
            println(e)
        }
    }
    println("WINNER: ${chessGame.getWinner()}")
}

/** CHESS BOARD
    8  BR BN BB BQ BK BB BN BR
    7  BP BP BP BP BP BP BP BP
    6
    5
    4
    3
    2  WP WP WP WP WP WP WP WP
    1  WR WN WB WQ WK WB WN WR
        A  B  C  D  E  F  G  H
 */