package com.example.matrusneh.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.matrusneh.R
import com.example.matrusneh.ui.screens.HomeScreen
import com.example.matrusneh.ui.screens.KickCounterScreen
import com.example.matrusneh.ui.screens.LoginScreen
import com.example.matrusneh.ui.screens.NutritionScreen
import com.example.matrusneh.ui.screens.ProfileScreen
import com.example.matrusneh.ui.screens.WelcomeScreen
import com.example.matrusneh.ui.theme.PrimaryPink
import com.example.matrusneh.ui.theme.SoftPink
import com.example.matrusneh.ui.theme.TextDark
import com.example.matrusneh.ui.theme.White
import com.example.matrusneh.ui.viewmodel.MainViewModel



val bottomNavItems = listOf(
    Screen.Home,
    Screen.Kick,
    Screen.Nutrition,
    Screen.Profile
)

@Composable
fun AppNavigation(viewModel: MainViewModel, startDestination: String) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isBottomNavVisible = bottomNavItems.any { it.route == currentDestination?.route }

            if (isBottomNavVisible) {
                NavigationBar(
                    containerColor = White,
                    contentColor = PrimaryPink
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = stringResource(screen.titleResId)) },
                            label = { Text(stringResource(screen.titleResId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = White,
                                selectedTextColor = PrimaryPink,
                                indicatorColor = PrimaryPink,
                                unselectedIconColor = TextDark,
                                unselectedTextColor = TextDark
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(viewModel = viewModel, onGetStarted = {
                    navController.navigate(Screen.Login.route)
                })
            }
            composable(Screen.Login.route) {
                LoginScreen(viewModel = viewModel, onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(Screen.Kick.route) {
                KickCounterScreen(viewModel = viewModel)
            }
            composable(Screen.Nutrition.route) {
                NutritionScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = viewModel, onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}
