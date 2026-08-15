package com.ghost.folio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ghost.folio.data.local.FolioDatabase
import com.ghost.folio.data.repository.ArticleRepository
import com.ghost.folio.data.seed.SeedLoader
import com.ghost.folio.ui.components.FloatingNav
import com.ghost.folio.ui.screens.article.ArticleScreen
import com.ghost.folio.ui.screens.article.ArticleViewModel
import com.ghost.folio.ui.screens.explore.ExploreScreen
import com.ghost.folio.ui.screens.explore.ExploreViewModel
import com.ghost.folio.ui.screens.home.HomeScreen
import com.ghost.folio.ui.screens.home.HomeViewModel
import com.ghost.folio.ui.screens.saved.SavedScreen
import com.ghost.folio.ui.screens.saved.SavedViewModel
import com.ghost.folio.ui.screens.search.SearchScreen
import com.ghost.folio.ui.screens.search.SearchViewModel
import com.ghost.folio.ui.screens.splash.SplashScreen
import com.ghost.folio.ui.theme.ChampionBlue
import com.ghost.folio.ui.theme.FolioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val database = FolioDatabase.getDatabase(applicationContext)
        val repository = ArticleRepository(database.articleDao(), database.categoryDao())

        setContent {
            FolioTheme {
                var isSplashFinished by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    SeedLoader.seedIfNeeded(applicationContext, database)
                }

                if (!isSplashFinished) {
                    SplashScreen(
                        onSplashFinished = {
                            isSplashFinished = true
                        }
                    )
                } else {
                    FolioApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun FolioApp(repository: ArticleRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val mainTabs = listOf("home", "explore", "search", "saved")
    val showFloatingNav = currentRoute in mainTabs

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(repository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onArticleClick = { articleId ->
                        navController.navigate("article/$articleId")
                    }
                )
            }

            composable("explore") {
                val exploreViewModel: ExploreViewModel = viewModel(
                    factory = ExploreViewModel.Factory(repository)
                )
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onArticleClick = { articleId ->
                        navController.navigate("article/$articleId")
                    }
                )
            }

            composable("search") {
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModel.Factory(repository)
                )
                SearchScreen(
                    viewModel = searchViewModel,
                    onArticleClick = { articleId ->
                        navController.navigate("article/$articleId")
                    }
                )
            }

            composable("saved") {
                val savedViewModel: SavedViewModel = viewModel(
                    factory = SavedViewModel.Factory(repository)
                )
                SavedScreen(
                    viewModel = savedViewModel,
                    onArticleClick = { articleId ->
                        navController.navigate("article/$articleId")
                    }
                )
            }

            composable(
                route = "article/{articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                val articleViewModel: ArticleViewModel = viewModel(
                    key = articleId,
                    factory = ArticleViewModel.Factory(repository, articleId)
                )
                ArticleScreen(
                    viewModel = articleViewModel,
                    onBack = { navController.popBackStack() },
                    onRelatedArticleClick = { nextId ->
                        navController.navigate("article/$nextId") {
                            popUpTo("article/$articleId") { inclusive = true }
                        }
                    }
                )
            }
        }

        if (showFloatingNav) {
            FloatingNav(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
