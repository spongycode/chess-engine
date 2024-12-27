package com.spongycode.chess_core

interface ChessFunctions {
    fun getCurrentPlayer(): Color
    fun makeMove(start: String, end: String)
    fun getBoard(): MutableList<MutableList<Cell>>
    fun getMoves(start: String): List<String>
    fun undo()
    fun reset()
    fun getWinner(): Color?
}