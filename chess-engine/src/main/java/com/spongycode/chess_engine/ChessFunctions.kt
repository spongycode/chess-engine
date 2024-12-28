package com.spongycode.chess_engine

interface ChessFunctions {
    fun getCurrentPlayer(): Player
    fun makeMove(start: String, end: String)
    fun getBoard(): MutableList<MutableList<Cell>>
    fun getMoves(start: String): List<String>
    fun undo()
    fun reset()
    fun getWinner(): Player?
}