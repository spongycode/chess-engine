package com.spongycode.chessapp.repository

import android.content.Context
import java.util.UUID

class ChessRepository(private val context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("chess_prefs", Context.MODE_PRIVATE)

    private fun setUserId(): String {
        val sharedPreferences = context.getSharedPreferences("chess_prefs", Context.MODE_PRIVATE)
        var userId = sharedPreferences.getString("user_id", null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            sharedPreferences.edit().putString("user_id", userId).apply()
        }
        return userId
    }

    fun getUserId(): String {
        val userId = sharedPreferences.getString("user_id", null)
        if (userId != null) {
            return userId
        }
        return setUserId()
    }
}
