package com.example.osurework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.osurework.ui.detail.ScoreDetailScreen
import com.example.osurework.ui.search.SearchScreen
import com.example.osurework.ui.search.SearchViewModel
import com.example.osurework.ui.theme.OsuReworkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OsuReworkTheme {
                val navController = rememberNavController()
                val searchViewModel: SearchViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "search",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(280)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(280)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = tween(280)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(280)
                        )
                    }
                ) {
                    composable("search") {
                        SearchScreen(
                            navController = navController,
                            viewModel = searchViewModel
                        )
                    }
                    composable("detail") {
                        val score = searchViewModel.selectedScore.collectAsState().value
                        score?.let {
                            ScoreDetailScreen(
                                score = it,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}