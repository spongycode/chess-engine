package com.spongycode.chess_core

import com.spongycode.chess_core.Constants.BOARD_SIZE
import com.spongycode.chess_core.Constants.KNIGHT_MOVES
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ChessGame(
    var chessBoard: MutableList<MutableList<Cell>>
) {
    private val historyMoves: MutableList<Pair<Pair<String, String>, ChessPiece?>> = mutableListOf()
    private var currentPlayer: Color = Color.WHITE
    private var blackKingPosition: String = "E8"
    private var whiteKingPosition: String = "E1"
    var winner: Color? = null

    fun getBoard(): MutableList<MutableList<Cell>> {
        return chessBoard
    }

    fun makeMove(start: String, end: String) {
        val canMove = validate(start, end)
        if (canMove) {
            val (startRow, startCol) = start.transformToPair()
            val startPiece = chessBoard[startRow][startCol].piece
            removePiece(start)
            val (endRow, endCol) = end.transformToPair()
            val endPiece = chessBoard[endRow][endCol].piece
            startPiece?.let {
                addPiece(end, startPiece)
            }
            currentPlayer = if (currentPlayer == Color.WHITE) Color.BLACK else Color.WHITE
            historyMoves.add(
                Pair(
                    Pair(
                        start.uppercase(Locale.getDefault()),
                        end.uppercase(Locale.getDefault())
                    ), endPiece
                )
            )
            if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING) {
                blackKingPosition = end
            }
            if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING) {
                whiteKingPosition = end
            }
        } else {
            println("Invalid Move!")
        }
    }

    fun getMoves(start: String): List<String> {
        val moves = mutableListOf<String>()
        val (startRow, startCol) = start.transformToPair()
        val piece = chessBoard[startRow][startCol].piece ?: return moves
        val isPieceKing =
            piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING ||
                    piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING
        val kingPosition =
            if (currentPlayer == Color.WHITE) whiteKingPosition else blackKingPosition
        for (row in 8 downTo 1) {
            for (col in 'A'..'H') {
                val end = "${col}${row}"
                if (validate(start, end, false)) {
                    makeMove(start, end)
                    if (!isCellUnderAttack(if (isPieceKing) end else kingPosition)) {
                        moves.add(end.lowercase(Locale.getDefault()))
                    }
                    undo()
                }
            }
        }
        return moves
    }

    fun printBoard() {
        for (row in 8 downTo 1) {
            print("$row  ")
            for (col in 'A'..'H') {
                val position = "${col}${row}"
                val cell = position.transformToPair()
                print(chessBoard[cell.first][cell.second].piece.toShortFormat())
                print(" ")
            }
            println()
        }
        print("   ")
        for (col in 'A'..'H') {
            print(" $col ")
        }
        println()
    }

    fun undo() {
        if (historyMoves.isNotEmpty()) {
            val (start, end) = historyMoves.last().first
            val (startPositionRow, startPositionCol) = start.transformToPair()
            val (endPositionRow, endPositionCol) = end.transformToPair()
            val removedPiece = historyMoves.last().second
            val startPiece = chessBoard[endPositionRow][endPositionCol].piece
            // check for en passant
            if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.PAWN &&
                endPositionCol != startPositionCol && removedPiece == null
            ) {
                chessBoard[endPositionRow - 1][endPositionCol].piece =
                    ChessPiece.WhiteChessPiece(ChessPiece.Type.PAWN)
                chessBoard[endPositionRow][endPositionCol].piece = null
                chessBoard[startPositionRow][startPositionCol].piece = startPiece
            } else if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.PAWN &&
                endPositionCol != startPositionCol && removedPiece == null
            ) {
                chessBoard[endPositionRow + 1][endPositionCol].piece =
                    ChessPiece.BlackChessPiece(ChessPiece.Type.PAWN)
                chessBoard[endPositionRow][endPositionCol].piece = null
                chessBoard[startPositionRow][startPositionCol].piece = startPiece
            } else {
                chessBoard[endPositionRow][endPositionCol].piece = removedPiece
                chessBoard[startPositionRow][startPositionCol].piece = startPiece
                if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING) {
                    blackKingPosition = start
                }
                if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING) {
                    whiteKingPosition = start
                }
            }
            historyMoves.removeLast()
            currentPlayer = if (currentPlayer == Color.WHITE) Color.BLACK else Color.WHITE
        }
    }

    fun reset(board: MutableList<MutableList<Cell>>) {
        chessBoard = board
        currentPlayer = Color.WHITE
        historyMoves.clear()
        blackKingPosition = "E8"
        whiteKingPosition = "E1"
        winner = null
    }

    private fun removePiece(position: String) {
        val (row, col) = position.transformToPair()
        val piece = chessBoard[row][col].piece
        if (piece is ChessPiece.BlackChessPiece && currentPlayer != Color.BLACK && piece.type == ChessPiece.Type.KING) {
            winner = Color.WHITE
        }
        if (piece is ChessPiece.WhiteChessPiece && currentPlayer != Color.WHITE && piece.type == ChessPiece.Type.KING) {
            winner = Color.BLACK
        }
        chessBoard[row][col].piece = null
    }

    private fun addPiece(position: String, piece: ChessPiece) {
        removePiece(position)
        val (row, col) = position.transformToPair()
        chessBoard[row][col].piece = piece
    }

    private fun validate(start: String, end: String, shouldMove: Boolean = true): Boolean {
        if (start == end) return false
        val (startRow, startCol) = start.transformToPair()
        val (endRow, endCol) = end.transformToPair()
        if (!(startRow in 0 until BOARD_SIZE && startCol in 0 until BOARD_SIZE &&
                    endRow in 0 until BOARD_SIZE && endCol in 0 until BOARD_SIZE)
        ) return false
        val startPiece = chessBoard[startRow][startCol].piece ?: return false
        if (startPiece.getColor() != currentPlayer) return false
        val endPiece = chessBoard[endRow][endCol].piece
        if (endPiece != null && (endPiece.getColor() == startPiece.getColor())) return false
        val dRow = endRow - startRow
        val dCol = endCol - startCol

        return when (startPiece) {
            is ChessPiece.BlackChessPiece -> {
                when (startPiece.type) {
                    ChessPiece.Type.KING -> abs(dRow) <= 1 && abs(dCol) <= 1
                    ChessPiece.Type.QUEEN -> ((dRow == 0 || dCol == 0) || (abs(dRow) == abs(dCol))) &&
                            !piecesInBetween(start, end)

                    ChessPiece.Type.ROOK -> (dRow == 0 || dCol == 0) && !piecesInBetween(start, end)
                    ChessPiece.Type.KNIGHT -> KNIGHT_MOVES.contains(Pair(dRow, dCol))
                    ChessPiece.Type.BISHOP -> abs(dRow) == abs(dCol) && !piecesInBetween(start, end)
                    ChessPiece.Type.PAWN -> {
                        val lastPiece = chessBoard[4][startCol + dCol].piece
                        (dCol == 0 && (dRow == 1 || (startRow == 1 && dRow == 2 && !piecesInBetween(
                            start,
                            end
                        ))) && endPiece == null) ||
                                (abs(dCol) == 1 && dRow == 1 && endPiece != null) ||
                                (startRow == 4 && abs(dCol) == 1 && dRow == 1 && endPiece == null &&
                                        historyMoves.size > 0 && historyMoves.last().first == Pair(
                                    "${'A' + startCol + dCol}2",
                                    "${'A' + startCol + dCol}4"
                                ) && (lastPiece is ChessPiece.WhiteChessPiece && lastPiece.type == ChessPiece.Type.PAWN)
                                        && (!shouldMove || removePiece("${'A' + startCol + dCol}4") == Unit))
                    }
                }
            }

            is ChessPiece.WhiteChessPiece -> {
                when (startPiece.type) {
                    ChessPiece.Type.KING -> abs(dRow) <= 1 && abs(dCol) <= 1
                    ChessPiece.Type.QUEEN -> ((dRow == 0 || dCol == 0) || (abs(dRow) == abs(dCol))) &&
                            !piecesInBetween(start, end)

                    ChessPiece.Type.ROOK -> (dRow == 0 || dCol == 0) && !piecesInBetween(start, end)
                    ChessPiece.Type.KNIGHT -> KNIGHT_MOVES.contains(Pair(dRow, dCol))
                    ChessPiece.Type.BISHOP -> abs(dRow) == abs(dCol) && !piecesInBetween(start, end)
                    ChessPiece.Type.PAWN -> {
                        val lastPiece = chessBoard[3][startCol + dCol].piece
                        (dCol == 0 && (dRow == -1 || (startRow == 6 && dRow == -2 && !piecesInBetween(
                            start,
                            end
                        ))) && endPiece == null) ||
                                (abs(dCol) == 1 && dRow == -1 && endPiece != null) ||
                                (startRow == 3 && abs(dCol) == 1 && dRow == -1 && endPiece == null &&
                                        historyMoves.size > 0 && historyMoves.last().first == Pair(
                                    "${'A' + startCol + dCol}7",
                                    "${'A' + startCol + dCol}5"
                                ) && (lastPiece is ChessPiece.BlackChessPiece && lastPiece.type == ChessPiece.Type.PAWN)
                                        && (!shouldMove || removePiece("${'A' + startCol + dCol}5") == Unit))
                    }
                }
            }
        }
    }

    private fun piecesInBetween(start: String, end: String): Boolean {
        val positionStart = start.transformToPair()
        val positionEnd = end.transformToPair()
        val dRow = positionEnd.first - positionStart.first
        val dCol = positionEnd.second - positionStart.second
        if (dRow == 0) {
            val startCol = min(positionStart.second, positionEnd.second) + 1
            val endCol = max(positionStart.second, positionEnd.second) - 1
            val row = positionStart.first
            for (col in startCol..endCol) {
                if (chessBoard[row][col].piece != null) {
                    return true
                }
            }
        } else if (dCol == 0) {
            val startRow = min(positionStart.first, positionEnd.first) + 1
            val endRow = max(positionStart.first, positionEnd.first) - 1
            val col = positionStart.second
            for (row in startRow..endRow) {
                if (chessBoard[row][col].piece != null) {
                    return true
                }
            }
        } else {
            val (startRow, startCol) = positionStart
            val (endRow, endCol) = positionEnd
            val rowUnitDiff = (endRow - startRow) / abs(endRow - startRow)
            val colUnitDiff = (endCol - startCol) / abs(endCol - startCol)
            var (row, col) = positionStart
            row += rowUnitDiff
            col += colUnitDiff
            while (row != endRow && col != endCol) {
                if (chessBoard[row][col].piece != null) {
                    return true
                }
                row += rowUnitDiff
                col += colUnitDiff
            }
        }
        return false
    }

    private fun isCellUnderAttack(position: String): Boolean {
        val (row, col) = position.transformToPair()
        val piece = chessBoard[row][col].piece ?: return false
        val pieceColor = piece.getColor()
        for (checkRow in 0 until BOARD_SIZE) {
            for (checkCol in 0 until BOARD_SIZE) {
                val checkPiece = chessBoard[checkRow][checkCol].piece ?: continue
                if (checkPiece.getColor() == pieceColor) continue
                val start = "${'A' + checkCol}${8 - checkRow}"
                if (validate(start, position, false)) {
                    return true
                }
            }
        }
        return false
    }
}