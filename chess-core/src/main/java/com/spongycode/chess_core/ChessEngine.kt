package com.spongycode.chess_core

class ChessEngine : ChessFunctions {
    private val chessBoard = ChessBoard()
    private val board = chessBoard.getBoard()
    private val chessGame = ChessGame(chessBoard = board)
    override fun makeMove(start: String, end: String) {
        chessGame.makeMove(start, end)
    }

    override fun getBoard(): MutableList<MutableList<Cell>> = chessGame.getBoard()
    override fun getMoves(start: String): List<String> = chessGame.getMoves(start)
    override fun undo() {
        chessGame.undo()
    }

    override fun reset() {
        val newBoard = ChessBoard().getBoard()
        chessGame.reset(newBoard)
    }

    override fun getWinner(): Color? = chessGame.getWinner()
}