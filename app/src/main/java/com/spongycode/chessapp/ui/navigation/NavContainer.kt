package com.spongycode.chessapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.spongycode.chessapp.ui.screen.game.GameScreenRoot
import com.spongycode.chessapp.ui.screen.home.HomeScreenRoot
import com.spongycode.chessapp.util.Constants.GAME_ID
import com.spongycode.chessapp.util.Constants.GAME_SCREEN
import com.spongycode.chessapp.util.Constants.HOME_SCREEN

val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController") }

@Composable
fun NavContainer(startDestination: String) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(route = HOME_SCREEN,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
                    )
                },
                popEnterTransition = {
                    EnterTransition.None
                }
            ) {
                HomeScreenRoot()
            }
            composable(
                route = "$GAME_SCREEN/{$GAME_ID}",
                arguments = listOf(navArgument(GAME_ID) { type = NavType.StringType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "chess-app://game/{$GAME_ID}" }
                ),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
                    )
                },
                popEnterTransition = {
                    EnterTransition.None
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End, tween(300)
                    )
                }
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString(GAME_ID)
                if (gameId != null) {
                    GameScreenRoot(gameId = gameId)
                }
            }
        }
    }
}