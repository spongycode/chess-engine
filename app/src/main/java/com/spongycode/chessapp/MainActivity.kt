package com.spongycode.chessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.spongycode.chessapp.screen.MainScreenRoot
import com.spongycode.chessapp.screen.MainViewModel
import com.spongycode.chessapp.ui.theme.ChessAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChessAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreenRoot(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = MainViewModel()
                    )
                }
            }
        }
    }
}