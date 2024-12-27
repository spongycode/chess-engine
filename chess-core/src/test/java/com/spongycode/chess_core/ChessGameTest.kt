package com.spongycode.chess_core

import com.spongycode.chess_core.sample_games.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ChessGameTest {

    private lateinit var chessBoard: ChessBoard
    private lateinit var board: MutableList<MutableList<Cell>>
    private lateinit var chessGame: ChessGame

    @Before
    fun setup() {
        chessBoard = ChessBoard()
        board = chessBoard.getBoard()
        chessGame = ChessGame(chessBoard = board)
    }

    @Test
    fun testDrawGames() {
        val drawGames = listOf(
            drawMoves1,
            drawMoves2,
            drawMoves3,
            drawMoves4,
            drawMoves5,
            drawMoves6
        )

        for (game in drawGames) {
            validateGame(game, expectedResult = Color.DRAW)
        }

        for (game in drawGames) {
            validateUndoMovements(game, expectedResult = Color.DRAW)
        }
    }

    @Test
    fun testNullGames() {
        val nullGames = listOf(
            nullMoves1,
            nullMoves2
        )

        for (game in nullGames) {
            validateGame(game, expectedResult = null)
        }

        for (game in nullGames) {
            validateUndoMovements(game, expectedResult = null)
        }
    }

    @Test
    fun testWhiteWinGames() {
        val whiteWinGames = listOf(
            whiteMoves1,
            whiteMoves2,
            whiteMoves3
        )

        for (game in whiteWinGames) {
            validateGame(game, expectedResult = Color.WHITE)
        }

        for (game in whiteWinGames) {
            validateUndoMovements(game, expectedResult = Color.WHITE)
        }
    }

    @Test
    fun testBlackWinGames() {
        val blackWinGames = listOf(
            blackMoves1,
            blackMoves2,
            blackMoves3,
            blackMoves4,
            blackMoves5
        )

        for (game in blackWinGames) {
            validateGame(game, expectedResult = Color.BLACK)
        }

        for (game in blackWinGames) {
            validateUndoMovements(game, expectedResult = Color.BLACK)
        }
    }

    private fun validateGame(
        game: Pair<List<Pair<String, String>>, Color?>,
        expectedResult: Color?
    ) {
        setup()

        val moves = game.first
        val games = mutableListOf<String>()

        var index = 0
        while (index < moves.size && chessGame.getWinner() == null) {
            val (start, end) = moves[index]
            chessGame.makeMove(start, end)
            index++
            games.add(chessGame.getBoardAsString())
        }

        for (i in 1 until games.size) {
            assertNotEquals(
                "Board state should change after move ${moves[i - 1]}",
                "\n${games[i - 1]}",
                "\n${games[i]}"
            )
        }

        assertEquals(
            "Game winner should match expected result",
            expectedResult,
            chessGame.getWinner()
        )
    }

    private fun validateUndoMovements(
        game: Pair<List<Pair<String, String>>, Color?>,
        expectedResult: Color?
    ) {
        setup()

        val move = game.first
        val games = mutableListOf<String>()

        var index = 0
        while (index < move.size && chessGame.getWinner() == null) {
            val (start, end) = move[index]
            chessGame.makeMove(start, end)
            index++
            games.add(chessGame.getBoardAsString())
        }

        val newChessBoard = ChessBoard()
        val newBoard = newChessBoard.getBoard()
        chessGame.reset(newBoard)

        for (frontIndex in move.indices) {
            var (start, end) = move[frontIndex]
            chessGame.makeMove(start, end)

            var backIndex = frontIndex
            while (backIndex > 0) {
                chessGame.undo()
                backIndex--
                val undoGame = chessGame.getBoardAsString()
                assertEquals(
                    "Undo failed at index $backIndex when coming back from $frontIndex",
                    "\n$undoGame",
                    "\n${games[backIndex]}"
                )
            }
            while (backIndex < frontIndex) {
                backIndex++
                start = move[backIndex].first
                end = move[backIndex].second
                chessGame.makeMove(start, end)
            }
        }

        assertEquals(
            "Game winner should match expected result",
            expectedResult,
            chessGame.getWinner()
        )
    }
}