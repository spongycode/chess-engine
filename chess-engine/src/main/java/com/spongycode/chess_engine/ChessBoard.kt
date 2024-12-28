package com.spongycode.chess_engine

import com.spongycode.chess_engine.Constants.BOARD_SIZE

class ChessBoard {
    private val board: MutableList<MutableList<Cell>> = MutableList(BOARD_SIZE) { MutableList(BOARD_SIZE) { Cell() } }

    init {
        fillBoard()
    }

    fun getBoard(): MutableList<MutableList<Cell>> {
        return board
    }

    private fun fillBoard() {
        for (ch in 'A'..'H') {
            val position = "${ch}2"
            addPiece(position, ChessPiece.WhiteChessPiece(ChessPiece.Type.PAWN))
        }
        addPiece("A1", ChessPiece.WhiteChessPiece(ChessPiece.Type.ROOK))
        addPiece("B1", ChessPiece.WhiteChessPiece(ChessPiece.Type.KNIGHT))
        addPiece("C1", ChessPiece.WhiteChessPiece(ChessPiece.Type.BISHOP))
        addPiece("D1", ChessPiece.WhiteChessPiece(ChessPiece.Type.QUEEN))
        addPiece("E1", ChessPiece.WhiteChessPiece(ChessPiece.Type.KING))
        addPiece("F1", ChessPiece.WhiteChessPiece(ChessPiece.Type.BISHOP))
        addPiece("G1", ChessPiece.WhiteChessPiece(ChessPiece.Type.KNIGHT))
        addPiece("H1", ChessPiece.WhiteChessPiece(ChessPiece.Type.ROOK))


        for (ch in 'A'..'H') {
            val position = "${ch}7"
            addPiece(position, ChessPiece.BlackChessPiece(ChessPiece.Type.PAWN))
        }
        addPiece("A8", ChessPiece.BlackChessPiece(ChessPiece.Type.ROOK))
        addPiece("B8", ChessPiece.BlackChessPiece(ChessPiece.Type.KNIGHT))
        addPiece("C8", ChessPiece.BlackChessPiece(ChessPiece.Type.BISHOP))
        addPiece("D8", ChessPiece.BlackChessPiece(ChessPiece.Type.QUEEN))
        addPiece("E8", ChessPiece.BlackChessPiece(ChessPiece.Type.KING))
        addPiece("F8", ChessPiece.BlackChessPiece(ChessPiece.Type.BISHOP))
        addPiece("G8", ChessPiece.BlackChessPiece(ChessPiece.Type.KNIGHT))
        addPiece("H8", ChessPiece.BlackChessPiece(ChessPiece.Type.ROOK))
    }

    private fun addPiece(position: String, piece: ChessPiece) {
        val (row, col) = position.transformToPair()
        board[row][col].piece = piece
    }
}