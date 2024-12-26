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
            // drawMoves3, TODO: Pawn Promotion: test will fail
            drawMoves4,
            drawMoves5
        )

        for (game in drawGames) {
            validateGame(game, expectedResult = Color.DRAW)
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
    }

    @Test
    fun testWhiteWinGames() {
        val whiteWinGames = listOf(
            whiteMoves1,
            whiteMoves2
        )

        for (game in whiteWinGames) {
            validateGame(game, expectedResult = Color.WHITE)
        }
    }

    @Test
    fun testBlackWinGames() {
        val blackWinGames = listOf(
            blackMoves1,
            blackMoves2,
            blackMoves3,
            blackMoves4
        )

        for (game in blackWinGames) {
            validateGame(game, expectedResult = Color.BLACK)
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
            games.add(chessGame.getBoardAsString())
            val (start, end) = moves[index]
            chessGame.makeMove(start, end)
            index++
        }
        games.add(chessGame.getBoardAsString())

        for (i in 1 until games.size) {
            assertNotEquals(
                "Board state should change after move ${moves[i - 1]}",
                games[i - 1],
                games[i]
            )
        }

        assertEquals(
            "Game winner should match expected result",
            expectedResult,
            chessGame.getWinner()
        )
    }
}