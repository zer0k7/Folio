package com.ghost.folio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ghost.folio.data.local.preferences.SettingsPreferences
import com.ghost.folio.data.local.preferences.ThemeSetting
import com.ghost.folio.data.repository.ArticleRepository
import com.ghost.folio.data.seed.SeedLoader
import com.ghost.folio.ui.components.ChangelogBottomSheet
import com.ghost.folio.ui.components.FloatingNav
import com.ghost.folio.ui.screens.article.ArticleScreen
import com.ghost.folio.ui.screens.article.ArticleViewModel
import com.ghost.folio.ui.screens.explore.ExploreScreen
import com.ghost.folio.ui.screens.explore.ExploreViewModel
import com.ghost.folio.ui.screens.home.HomeScreen
import com.ghost.folio.ui.screens.home.HomeViewModel
import com.ghost.folio.ui.screens.more.AppThemeMode
import com.ghost.folio.ui.screens.more.MoreScreen
import com.ghost.folio.ui.screens.more.MoreViewModel
import com.ghost.folio.ui.screens.search.SearchScreen
import com.ghost.folio.ui.screens.search.SearchViewModel
import com.ghost.folio.ui.screens.settings.SettingsScreen
import com.ghost.folio.ui.screens.settings.SettingsViewModel
import com.ghost.folio.ui.screens.splash.SplashScreen
import com.ghost.folio.ui.theme.FolioTheme
import com.ghost.folio.widget.DailyTermWidgetProvider
import com.ghost.folio.widget.DailyTermWidgetWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingArticleId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        intent?.getStringExtra(DailyTermWidgetProvider.EXTRA_ARTICLE_ID)?.let { articleId ->
            pendingArticleId = articleId
        }

        val database = FolioDatabase.getDatabase(applicationContext)
        val repository = ArticleRepository(
            database.articleDao(),
            database.categoryDao(),
            database.historyDao()
        )
        val preferences = SettingsPreferences(applicationContext)

        setContent {
            val themeSetting by preferences.themeSetting.collectAsState(initial = ThemeSetting.SYSTEM)
            val fontScale by preferences.fontScale.collectAsState(initial = 1.0f)
            val lastSeenVersionCode by preferences.lastSeenVersionCode.collectAsState(initial = -1)
            val scope = rememberCoroutineScope()
            var showAutoChangelog by remember { mutableStateOf(false) }

            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeSetting) {
                ThemeSetting.SYSTEM -> systemDark
                ThemeSetting.DARK -> true
                ThemeSetting.LIGHT -> false
            }

            FolioTheme(darkTheme = isDarkTheme, fontScale = fontScale) {
                var isSplashFinished by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    SeedLoader.seedIfNeeded(applicationContext, database)
                    DailyTermWidgetWorker.scheduleDailyUpdate(applicationContext)
                    DailyTermWidgetWorker.updateWidget(applicationContext)
                }

                LaunchedEffect(lastSeenVersionCode) {
                    if (lastSeenVersionCode != -1 && BuildConfig.VERSION_CODE > lastSeenVersionCode) {
                        showAutoChangelog = true
                    }
                }

                if (!isSplashFinished) {
                    SplashScreen(
                        onSplashFinished = {
                            isSplashFinished = true
                        }
                    )
                } else {
                    FolioApp(
                        repository = repository,
                        preferences = preferences,
                        pendingArticleId = pendingArticleId,
                        onArticleIdConsumed = { pendingArticleId = null }
                    )

                    if (showAutoChangelog) {
                        ChangelogBottomSheet(
                            onDismiss = {
                                showAutoChangelog = false
                                scope.launch {
                                    preferences.setLastSeenVersionCode(BuildConfig.VERSION_CODE)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(DailyTermWidgetProvider.EXTRA_ARTICLE_ID)?.let { articleId ->
            pendingArticleId = articleId
        }
    }
}

@Composable
fun FolioApp(
    repository: ArticleRepository,
    preferences: SettingsPreferences,
    pendingArticleId: String? = null,
    onArticleIdConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val mainTabs = listOf("home", "explore", "search", "more")
    val showFloatingNav = currentRoute in mainTabs

    LaunchedEffect(pendingArticleId) {
        pendingArticleId?.let { articleId ->
            navController.navigate("article/$articleId")
            onArticleIdConsumed()
        }
    }

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
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
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

            composable("more") {
                val moreViewModel: MoreViewModel = viewModel(
                    factory = MoreViewModel.Factory(repository)
                )
                MoreScreen(
                    viewModel = moreViewModel,
                    currentTheme = AppThemeMode.SYSTEM,
                    onThemeChange = {}
                )
            }

            composable("settings") {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(repository, preferences)
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
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
