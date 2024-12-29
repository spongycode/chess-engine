package com.spongycode.chessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.spongycode.chessapp.ui.navigation.NavContainer
import com.spongycode.chessapp.ui.theme.ChessAppTheme
import com.spongycode.chessapp.util.Constants.HOME_SCREEN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChessAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavContainer(startDestination = HOME_SCREEN)
                }
            }
        }
    }
}