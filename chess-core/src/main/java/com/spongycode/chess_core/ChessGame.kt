package com.spongycode.chess_core

import com.spongycode.chess_core.Constants.BOARD_SIZE
import com.spongycode.chess_core.Constants.KING_MOVES
import com.spongycode.chess_core.Constants.KNIGHT_MOVES
import com.spongycode.chess_core.Constants.PAWN_MOVES
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
    private var whiteKingMoveCount: Int = 0
    private var blackKingMoveCount: Int = 0
    private var whiteRooksMoved: Pair<Int, Int> = Pair(0, 0)
    private var blackRooksMoved: Pair<Int, Int> = Pair(0, 0)
    private var winner: Color? = null

    fun getBoard(): MutableList<MutableList<Cell>> {
        return chessBoard
    }

    fun makeMove(start: String, end: String) {
        val canMove = validate(start, end)
        if (canMove) {
            makeMoveAfterValidation(start, end) {
                checkForGameOverOrStalemate()
            }
        } else {
            println("Invalid Move!")
        }
    }

    fun getMoves(start: String): List<String> {
        val (startRow, startCol) = start.transformToPair()
        val piece = chessBoard[startRow][startCol].piece ?: return mutableListOf()
        return when (piece.type) {
            ChessPiece.Type.KING -> getKingMoves(start)
            ChessPiece.Type.QUEEN -> getQueenMoves(start)
            ChessPiece.Type.ROOK -> getRookMoves(start)
            ChessPiece.Type.KNIGHT -> getKnightMoves(start)
            ChessPiece.Type.BISHOP -> getBishopMoves(start)
            ChessPiece.Type.PAWN -> getPawnMoves(start)
        }
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

    fun getBoardAsString(): String{
        val game = StringBuilder()
        for (row in 8 downTo 1) {
            game.append("$row  ")
            for (col in 'A'..'H') {
                val position = "${col}${row}"
                val cell = position.transformToPair()
                game.append(chessBoard[cell.first][cell.second].piece.toShortFormat())
                game.append(" ")
            }
            game.append("\n")
        }
        game.append("   ")
        for (col in 'A'..'H') {
            game.append(" $col ")
        }
        game.append("\n")
        return game.toString()
    }

    fun undo() {
        if (historyMoves.isNotEmpty()) {
            val (start, end) = historyMoves.last().first
            val (startPositionRow, startPositionCol) = start.transformToPair()
            val (endPositionRow, endPositionCol) = end.transformToPair()
            val removedPiece = historyMoves.last().second
            val startPiece = chessBoard[endPositionRow][endPositionCol].piece
            // check for castling
            if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING &&
                startPositionRow == endPositionRow && abs(startPositionCol - endPositionCol) > 1
            ) {
                blackKingMoveCount--
                if (endPositionCol == 2) {
                    chessBoard[0][0].piece = ChessPiece.BlackChessPiece(ChessPiece.Type.ROOK)
                    chessBoard[0][4].piece = startPiece
                    chessBoard[0][3].piece = null
                    chessBoard[0][2].piece = null
                    blackRooksMoved = Pair(blackRooksMoved.first - 1, blackRooksMoved.second)
                } else if (endPositionCol == 6) {
                    chessBoard[0][7].piece = ChessPiece.BlackChessPiece(ChessPiece.Type.ROOK)
                    chessBoard[0][4].piece = startPiece
                    chessBoard[0][5].piece = null
                    chessBoard[0][6].piece = null
                    blackRooksMoved = Pair(blackRooksMoved.first, blackRooksMoved.second - 1)
                }
            } else if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING &&
                startPositionRow == endPositionRow && abs(startPositionCol - endPositionCol) > 1
            ) {
                whiteKingMoveCount--
                if (endPositionCol == 2) {
                    chessBoard[7][0].piece = ChessPiece.WhiteChessPiece(ChessPiece.Type.ROOK)
                    chessBoard[7][4].piece = startPiece
                    chessBoard[7][3].piece = null
                    chessBoard[7][2].piece = null
                    whiteRooksMoved = Pair(whiteRooksMoved.first - 1, whiteRooksMoved.second)
                } else if (endPositionCol == 6) {
                    chessBoard[7][7].piece = ChessPiece.WhiteChessPiece(ChessPiece.Type.ROOK)
                    chessBoard[7][4].piece = startPiece
                    chessBoard[7][5].piece = null
                    chessBoard[7][6].piece = null
                    whiteRooksMoved = Pair(whiteRooksMoved.first, whiteRooksMoved.second - 1)
                }
            }
            // check for en passant
            else if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.PAWN &&
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
                if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING) {
                    blackKingMoveCount--
                }
                if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING) {
                    whiteKingMoveCount--
                }
                if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.ROOK) {
                    if (start.uppercase(Locale.ROOT) == "A8") {
                        blackRooksMoved = Pair(blackRooksMoved.first - 1, blackRooksMoved.second)
                    }
                    if (start.uppercase(Locale.ROOT) == "H8") {
                        blackRooksMoved = Pair(blackRooksMoved.first, blackRooksMoved.second - 1)
                    }
                }
                if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.ROOK) {
                    if (start.uppercase(Locale.ROOT) == "A1") {
                        whiteRooksMoved = Pair(whiteRooksMoved.first - 1, whiteRooksMoved.second)
                    }
                    if (start.uppercase(Locale.ROOT) == "H1") {
                        whiteRooksMoved = Pair(whiteRooksMoved.first, whiteRooksMoved.second - 1)
                    }
                }
                if (removedPiece is ChessPiece.WhiteChessPiece && removedPiece.type == ChessPiece.Type.ROOK) {
                    if (end.uppercase(Locale.ROOT) == "A1") {
                        whiteRooksMoved = Pair(whiteRooksMoved.first - 1, whiteRooksMoved.second)
                    }
                    if (end.uppercase(Locale.ROOT) == "H1") {
                        whiteRooksMoved = Pair(whiteRooksMoved.first, whiteRooksMoved.second - 1)
                    }
                }
                if (removedPiece is ChessPiece.BlackChessPiece && removedPiece.type == ChessPiece.Type.ROOK) {
                    if (end.uppercase(Locale.ROOT) == "A8") {
                        blackRooksMoved = Pair(blackRooksMoved.first - 1, blackRooksMoved.second)
                    }
                    if (end.uppercase(Locale.ROOT) == "H8") {
                        blackRooksMoved = Pair(blackRooksMoved.first, blackRooksMoved.second - 1)
                    }
                }
                chessBoard[endPositionRow][endPositionCol].piece = removedPiece
                chessBoard[startPositionRow][startPositionCol].piece = startPiece
            }
            if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING) {
                blackKingPosition = start
            }
            if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING) {
                whiteKingPosition = start
            }
            historyMoves.removeLast()
            currentPlayer = if (currentPlayer == Color.WHITE) Color.BLACK else Color.WHITE
            winner = null
        }
    }

    fun reset(board: MutableList<MutableList<Cell>>) {
        chessBoard = board
        currentPlayer = Color.WHITE
        historyMoves.clear()
        blackKingPosition = "E8"
        whiteKingPosition = "E1"
        winner = null
        whiteKingMoveCount = 0
        blackKingMoveCount = 0
        whiteRooksMoved = Pair(0, 0)
        blackRooksMoved = Pair(0, 0)
    }

    fun getWinner(): Color? {
        return winner
    }

    private fun getKingMoves(start: String): List<String> {
        val moves = mutableListOf<String>()
        for (move in KING_MOVES) {
            val end = start.offset(move.first, move.second)
            if (validate(start, end)) {
                makeMoveAfterValidation(start, end)
                if (!isCellUnderAttack(if (currentPlayer == Color.WHITE) blackKingPosition else whiteKingPosition)) {
                    moves.add(end.lowercase(Locale.getDefault()))
                }
                undo()
            }
        }
        return moves
    }

    private fun getQueenMoves(start: String): List<String> {
        val moves = mutableListOf<String>()
        var offset = 1
        while (offset <= 8) {
            val endDown = start.offset(offset, 0)
            val endUp = start.offset(-offset, 0)
            val endLeft = start.offset(0, -offset)
            val endRight = start.offset(0, offset)
            val endDownLeft = start.offset(offset, -offset)
            val endDownRight = start.offset(offset, offset)
            val endUpLeft = start.offset(-offset, -offset)
            val endUpRight = start.offset(-offset, offset)
            val movesList = listOf(
                endDown,
                endUp,
                endLeft,
                endRight,
                endDownLeft,
                endDownRight,
                endUpLeft,
                endUpRight
            )
            for (move in movesList) {
                if (validate(start, move)) {
                    makeMoveAfterValidation(start, move)
                    if (!isCellUnderAttack(if (currentPlayer == Color.WHITE) blackKingPosition else whiteKingPosition)) {
                        moves.add(move.lowercase(Locale.getDefault()))
                    }
                    undo()
                }
            }
            offset++
        }
        return moves
    }

    private fun getRookMoves(start: String): List<String> {
        val moves = mutableListOf<String>()
        var offset = 1
        while (offset <= 8) {
            val endDown = start.offset(offset, 0)
            val endUp = start.offset(-offset, 0)
            val endLeft = start.offset(0, -offset)
            val endRight = start.offset(0, offset)
            val movesList = listOf(endDown, endUp, endLeft, endRight)
            for (move in movesList) {
                if (validate(start, move)) {
                    makeMoveAfterValidation(start, move)
                    if (!isCellUnderAttack(if (currentPlayer == Color.WHITE) blackKingPosition else whiteKingPosition)) {
                        moves.add(move.lowercase(Locale.getDefault()))
                    }
                    undo()
                }
            }
            offset++
        }
        return moves
    }

    private fun getBishopMoves(start: String): List<String> {
        val moves = mutableListOf<String>()
        var offset = 1
        while (offset <= 8) {
            val endDownLeft = start.offset(offset, -offset)
            val endDownRight = start.offset(offset, offset)
            val endUpLeft = start.offset(-offset, -offset)
            val endUpRight = start.offset(-offset, offset)
            val movesList = listOf(endDownLeft, endDownRight, endUpLeft, endUpRight)
            for (move in movesList) {
                if (validate(start, move)) {
                    makeMoveAfterValidation(start, move)
                    if (!isCellUnderAttack(if (currentPlayer == Color.WHITE) blackKingPosition else whiteKingPosition)) {
                        moves.add(move.lowercase(Locale.getDefault()))
                    }
                    undo()
                }
            }
            offset++
        }
        return moves
    }

    private fun getKnightMoves(start: String): List<String> {
        val moves = mutableListOf<String>()
        for (move in KNIGHT_MOVES) {
            val end = start.offset(move.first, move.second)
            if (validate(start, end)) {
                makeMoveAfterValidation(start, end)
                if (!isCellUnderAttack(if (currentPlayer == Color.WHITE) blackKingPosition else whiteKingPosition)) {
                    moves.add(end.lowercase(Locale.getDefault()))
                }
                undo()
            }
        }
        return moves
    }

    private fun getPawnMoves(start: String): List<String> {
        val moves = mutableListOf<String>()
        val (startRow, startCol) = start.transformToPair()
        val piece = chessBoard[startRow][startCol].piece ?: return moves
        for (move in PAWN_MOVES) {
            val end = start.offset(
                if (piece.getColor() == Color.WHITE) move.first else -move.first,
                move.second
            )
            if (validate(start, end, false)) {
                makeMoveAfterValidation(start, end)
                if (!isCellUnderAttack(if (currentPlayer == Color.WHITE) blackKingPosition else whiteKingPosition)) {
                    moves.add(end.lowercase(Locale.getDefault()))
                }
                undo()
            }
        }
        return moves
    }

    private fun removePiece(position: String) {
        val (row, col) = position.transformToPair()
        val piece = chessBoard[row][col].piece
        // Todo: remove the static winner logic
        if (piece is ChessPiece.BlackChessPiece && currentPlayer != Color.BLACK && piece.type == ChessPiece.Type.KING) {
            winner = Color.WHITE
        }
        if (piece is ChessPiece.WhiteChessPiece && currentPlayer != Color.WHITE && piece.type == ChessPiece.Type.KING) {
            winner = Color.BLACK
        }
        chessBoard[row][col].piece = null
    }

    private fun makeMoveAfterValidation(start: String, end: String, afterMove: () -> Unit = {}) {
        val (startRow, startCol) = start.transformToPair()
        val startPiece = chessBoard[startRow][startCol].piece
        removePiece(start)
        val (endRow, endCol) = end.transformToPair()
        val endPiece = chessBoard[endRow][endCol].piece
        startPiece?.let {
            addPiece(end, startPiece)
        }
        if (endPiece is ChessPiece.WhiteChessPiece && endPiece.type == ChessPiece.Type.ROOK) {
            if (end.uppercase(Locale.ROOT) == "A1") {
                whiteRooksMoved = Pair(whiteRooksMoved.first + 1, whiteRooksMoved.second)
            }
            if (end.uppercase(Locale.ROOT) == "H1") {
                whiteRooksMoved = Pair(whiteRooksMoved.first, whiteRooksMoved.second + 1)
            }
        }
        if (endPiece is ChessPiece.BlackChessPiece && endPiece.type == ChessPiece.Type.ROOK) {
            if (end.uppercase(Locale.ROOT) == "A8") {
                blackRooksMoved = Pair(blackRooksMoved.first + 1, blackRooksMoved.second)
            }
            if (end.uppercase(Locale.ROOT) == "H8") {
                blackRooksMoved = Pair(blackRooksMoved.first, blackRooksMoved.second + 1)
            }
        }
        if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING &&
            abs(startCol - endCol) > 1
        ) {
            addPiece(
                start.offset(0, if (endCol - startCol > 0) 1 else -1),
                ChessPiece.BlackChessPiece(ChessPiece.Type.ROOK)
            )
            removePiece(end.offset(0, if (endCol - startCol > 0) 1 else -2))
        }
        if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING &&
            abs(startCol - endCol) > 1
        ) {
            addPiece(
                start.offset(0, if (endCol - startCol > 0) 1 else -1),
                ChessPiece.WhiteChessPiece(ChessPiece.Type.ROOK)
            )
            removePiece(end.offset(0, if (endCol - startCol > 0) 1 else -2))
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
        // marking castling states
        if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING) {
            blackKingMoveCount++
            if (endCol - startCol == 2) {
                blackRooksMoved = Pair(blackRooksMoved.first, blackRooksMoved.second + 1)
            } else if (endCol - startCol == -2) {
                blackRooksMoved = Pair(blackRooksMoved.first + 1, blackRooksMoved.second)
            }
        } else if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING) {
            whiteKingMoveCount++
            if (endCol - startCol == 2) {
                whiteRooksMoved = Pair(whiteRooksMoved.first, whiteRooksMoved.second + 1)
            } else if (endCol - startCol == -2) {
                whiteRooksMoved = Pair(whiteRooksMoved.first + 1, whiteRooksMoved.second)
            }
        } else if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.ROOK) {
            if (start.lowercase(Locale.ROOT) == "a8") {
                blackRooksMoved = Pair(blackRooksMoved.first + 1, blackRooksMoved.second)
            }
            if (start.lowercase(Locale.ROOT) == "h8") {
                blackRooksMoved = Pair(blackRooksMoved.first, blackRooksMoved.second + 1)
            }
        } else if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.ROOK) {
            if (start.lowercase(Locale.ROOT) == "a1") {
                whiteRooksMoved = Pair(whiteRooksMoved.first + 1, whiteRooksMoved.second)
            }
            if (start.lowercase(Locale.ROOT) == "h1") {
                whiteRooksMoved = Pair(whiteRooksMoved.first, whiteRooksMoved.second + 1)
            }
        }

        if (startPiece is ChessPiece.BlackChessPiece && startPiece.type == ChessPiece.Type.KING) {
            blackKingPosition = end
        }
        if (startPiece is ChessPiece.WhiteChessPiece && startPiece.type == ChessPiece.Type.KING) {
            whiteKingPosition = end
        }
        afterMove()
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

        return when (startPiece.type) {
            ChessPiece.Type.KING -> {
                when (startPiece) {
                    is ChessPiece.BlackChessPiece -> {
                        (abs(dRow) <= 1 && abs(dCol) <= 1) || (blackKingMoveCount == 0 && dRow == 0 &&
                                ((dCol == 2 && blackRooksMoved.second == 0) || (dCol == -2 && blackRooksMoved.first == 0)) &&
                                !isCellUnderAttack(
                                    blackKingPosition.offset(
                                        0,
                                        if (dCol > 0) 1 else -1
                                    ), attackingPlayer = Color.WHITE
                                ) &&
                                !isCellUnderAttack(
                                    blackKingPosition.offset(
                                        0,
                                        if (dCol > 0) 2 else -2
                                    ), attackingPlayer = Color.WHITE
                                ) &&
                                !isCellUnderAttack(
                                    blackKingPosition,
                                    attackingPlayer = Color.WHITE
                                ))
                    }

                    is ChessPiece.WhiteChessPiece -> {
                        (abs(dRow) <= 1 && abs(dCol) <= 1) || (whiteKingMoveCount == 0 && dRow == 0 &&
                                ((dCol == 2 && whiteRooksMoved.second == 0) || (dCol == -2 && whiteRooksMoved.first == 0)) &&
                                !piecesInBetween(start, end) &&
                                !isCellUnderAttack(
                                    whiteKingPosition.offset(
                                        0,
                                        if (dCol > 0) 1 else -1
                                    ), attackingPlayer = Color.BLACK
                                ) &&
                                !isCellUnderAttack(
                                    whiteKingPosition.offset(
                                        0,
                                        if (dCol > 0) 2 else -2
                                    ), attackingPlayer = Color.BLACK
                                ) &&
                                !isCellUnderAttack(
                                    whiteKingPosition,
                                    attackingPlayer = Color.BLACK
                                ))
                    }
                }
            }

            ChessPiece.Type.QUEEN -> ((dRow == 0 || dCol == 0) || (abs(dRow) == abs(dCol))) &&
                    !piecesInBetween(start, end)

            ChessPiece.Type.ROOK -> (dRow == 0 || dCol == 0) && !piecesInBetween(start, end)

            ChessPiece.Type.KNIGHT -> KNIGHT_MOVES.contains(Pair(dRow, dCol))

            ChessPiece.Type.BISHOP -> abs(dRow) == abs(dCol) && !piecesInBetween(start, end)

            ChessPiece.Type.PAWN -> {
                when (startPiece) {
                    is ChessPiece.BlackChessPiece -> {
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

                    is ChessPiece.WhiteChessPiece -> {
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

    private fun isCellUnderAttack(position: String, attackingPlayer: Color? = null): Boolean {
        val (row, col) = position.transformToPair()
        val opponentPlayer = attackingPlayer ?: currentPlayer

        // checking vertically up
        var dummyRow = row - 1
        var dummyCol = col
        while (dummyRow >= 0) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyRow - row) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyRow - row) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING)))
                ) {
                    return true
                }
                break
            }
            dummyRow--
        }

        // checking vertically down
        dummyRow = row + 1
        dummyCol = col
        while (dummyRow < BOARD_SIZE) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyRow - row) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyRow - row) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING)))
                ) {
                    return true
                }
                break
            }
            dummyRow++
        }

        // checking horizontally left
        dummyRow = row
        dummyCol = col - 1
        while (dummyCol >= 0) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING)))
                ) {
                    return true
                }
                break
            }
            dummyCol--
        }

        // checking horizontally right
        dummyRow = row
        dummyCol = col + 1
        while (dummyCol < BOARD_SIZE) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.ROOK) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING)))
                ) {
                    return true
                }
                break
            }
            dummyCol++
        }

        // checking diagonal towards top-left
        dummyRow = row - 1
        dummyCol = col - 1
        while (dummyCol >= 0 && dummyRow >= 0) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.PAWN))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING)))
                ) {
                    return true
                }
                break
            }
            dummyCol--
            dummyRow--
        }

        // checking diagonal towards top-right
        dummyRow = row - 1
        dummyCol = col + 1
        while (dummyCol < BOARD_SIZE && dummyRow >= 0) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.PAWN))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING)))
                ) {
                    return true
                }
                break
            }
            dummyCol++
            dummyRow--
        }

        // checking diagonal towards bottom-left
        dummyRow = row + 1
        dummyCol = col - 1
        while (dummyCol >= 0 && dummyRow < BOARD_SIZE) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.PAWN)))
                ) {
                    return true
                }
                break
            }
            dummyCol--
            dummyRow++
        }

        // checking diagonal towards bottom-right
        dummyRow = row + 1
        dummyCol = col + 1
        while (dummyCol < BOARD_SIZE && dummyRow < BOARD_SIZE) {
            val piece = chessBoard[dummyRow][dummyCol].piece
            if (piece != null) {
                if (
                    (opponentPlayer == Color.BLACK &&
                            ((piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KING))) ||
                    (opponentPlayer == Color.WHITE &&
                            ((piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.QUEEN) ||
                                    (piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.BISHOP) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KING) ||
                                    (abs(dummyCol - col) == 1 && piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.PAWN)))
                ) {
                    return true
                }
                break
            }
            dummyCol++
            dummyRow++
        }

        // checking knight attacks
        dummyRow = row
        dummyCol = col
        for (move in KNIGHT_MOVES) {
            val (dRow, dCol) = move
            val knightAttackRow = dummyRow + dRow
            val knightAttackCol = dummyCol + dCol
            if (knightAttackRow in 0 until BOARD_SIZE && knightAttackCol in 0 until BOARD_SIZE) {
                val piece = chessBoard[knightAttackRow][knightAttackCol].piece
                if (
                    (opponentPlayer == Color.BLACK &&
                            piece is ChessPiece.BlackChessPiece && piece.type == ChessPiece.Type.KNIGHT) ||
                    (opponentPlayer == Color.WHITE &&
                            piece is ChessPiece.WhiteChessPiece && piece.type == ChessPiece.Type.KNIGHT)
                ) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkForGameOverOrStalemate() {
        for (row in 8 downTo 1) {
            for (col in 'A'..'H') {
                val position = "${col}${row}"
                val (r, c) = position.transformToPair()
                if (chessBoard[r][c].piece?.getColor() == currentPlayer) {
                    val moves = getMoves(position)
                    if (moves.isNotEmpty()) {
                        winner = null
                        return
                    }
                }
            }
        }
        winner =
            if (isCellUnderAttack(
                    if (currentPlayer == Color.WHITE) whiteKingPosition else blackKingPosition,
                    attackingPlayer = if (currentPlayer == Color.WHITE) Color.BLACK else Color.WHITE
                )
            ) {
                if (currentPlayer == Color.WHITE) Color.BLACK else Color.WHITE
            } else {
                Color.DRAW
            }
    }
}