package com.ziyuan.wenyan.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ziyuan.wenyan.ui.card.CardDetailScreen
import com.ziyuan.wenyan.ui.compare.CompareDetailScreen
import com.ziyuan.wenyan.ui.home.HomeScreen
import com.ziyuan.wenyan.ui.library.LibraryScreen
import com.ziyuan.wenyan.ui.profile.ProfileScreen
import com.ziyuan.wenyan.ui.quiz.QuizScreen
import com.ziyuan.wenyan.ui.review.ReviewScreen
import com.ziyuan.wenyan.ui.settings.SettingsScreen
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.texts.TextDetailScreen
import com.ziyuan.wenyan.ui.texts.TextLibraryScreen
import com.ziyuan.wenyan.ui.wrongbook.WrongBookScreen

// 全部路由
sealed class Dest(val route: String) {
    data object Home : Dest("home")
    data object Library : Dest("library")
    data object Quiz : Dest("quiz?mode={mode}&sourceId={sourceId}") {
        fun ofDaily() = "quiz?mode=daily&sourceId="
        fun ofCompare(compareId: String) = "quiz?mode=compare&sourceId=$compareId"
        fun ofWrong() = "quiz?mode=wrong&sourceId="
        fun ofReview() = "quiz?mode=review&sourceId="
    }
    data object Profile : Dest("profile")
    data object Card : Dest("card/{cardId}") {
        fun of(cardId: String) = "card/$cardId"
    }
    data object Compare : Dest("compare/{compareId}") {
        fun of(compareId: String) = "compare/$compareId"
    }
    data object Texts : Dest("texts")
    data object TextDetail : Dest("text/{textId}") {
        fun of(textId: String) = "text/$textId"
    }
    data object WrongBook : Dest("wrongbook")
    data object Review : Dest("review")
    data object Settings : Dest("settings")
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomItems = listOf(
        BottomItem(Dest.Home.route, "首页", Icons.Filled.Home),
        BottomItem(Dest.Library.route, "卡片", Icons.Filled.Search),
        BottomItem(Dest.Quiz.route, "检测", Icons.Filled.Edit),
        BottomItem(Dest.Profile.route, "我的", Icons.Filled.Person)
    )
    val showBottomBar = currentRoute in bottomItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = AppTheme.colors.navBar) {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OreGreen,
                                selectedTextColor = OreGreen,
                                indicatorColor = OreGreen.copy(alpha = 0.24f),
                                unselectedIconColor = OreTextSecondary,
                                unselectedTextColor = OreTextSecondary
                            ),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Dest.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Home.route) {
                HomeScreen(
                    onOpenCard = { navController.navigate(Dest.Card.of(it)) },
                    onOpenLibrary = { navController.navigate(Dest.Library.route) },
                    onOpenTexts = { navController.navigate(Dest.Texts.route) },
                    onStartReview = { navController.navigate(Dest.Review.route) },
                    onStartQuiz = { navController.navigate(Dest.Quiz.ofDaily()) }
                )
            }
            composable(Dest.Library.route) {
                LibraryScreen(
                    onOpenCard = { navController.navigate(Dest.Card.of(it)) },
                    onOpenCompare = { navController.navigate(Dest.Compare.of(it)) },
                    onOpenTexts = { navController.navigate(Dest.Texts.route) }
                )
            }
            composable(
                route = Dest.Quiz.route,
                arguments = listOf(
                    navArgument("mode") { defaultValue = "daily" },
                    navArgument("sourceId") { defaultValue = "" }
                )
            ) { entry ->
                QuizScreen(
                    onOpenCard = { navController.navigate(Dest.Card.of(it)) },
                    mode = entry.arguments?.getString("mode") ?: "daily",
                    sourceId = entry.arguments?.getString("sourceId")
                )
            }
            composable(Dest.Profile.route) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Dest.Settings.route) },
                    onOpenWrongBook = { navController.navigate(Dest.WrongBook.route) }
                )
            }
            composable(
                route = Dest.Card.route,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) { entry ->
                CardDetailScreen(
                    cardId = entry.arguments?.getString("cardId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenCard = { navController.navigate(Dest.Card.of(it)) },
                    onOpenCompare = { navController.navigate(Dest.Compare.of(it)) },
                    onStartQuiz = {
                        navController.navigate("quiz?mode=card&sourceId=" + entry.arguments?.getString("cardId").orEmpty())
                    }
                )
            }
            composable(
                route = Dest.Compare.route,
                arguments = listOf(navArgument("compareId") { type = NavType.StringType })
            ) { entry ->
                CompareDetailScreen(
                    compareId = entry.arguments?.getString("compareId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onStartCompareQuiz = { navController.navigate(Dest.Quiz.ofCompare(it)) }
                )
            }
            composable(Dest.Texts.route) {
                TextLibraryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenText = { navController.navigate(Dest.TextDetail.of(it)) }
                )
            }
            composable(
                route = Dest.TextDetail.route,
                arguments = listOf(navArgument("textId") { type = NavType.StringType })
            ) { entry ->
                TextDetailScreen(
                    textId = entry.arguments?.getString("textId").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Dest.WrongBook.route) {
                WrongBookScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCard = { navController.navigate(Dest.Card.of(it)) },
                    onStartQuiz = { navController.navigate(Dest.Quiz.ofWrong()) }
                )
            }
            composable(Dest.Review.route) {
                ReviewScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCard = { navController.navigate(Dest.Card.of(it)) }
                )
            }
            composable(Dest.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
