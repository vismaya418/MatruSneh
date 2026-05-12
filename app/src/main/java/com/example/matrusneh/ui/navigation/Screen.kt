package com.example.matrusneh.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import com.example.matrusneh.R

sealed class Screen(val route: String, val titleResId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector?) {
    object Welcome : Screen("welcome", R.string.app_name, null)
    object Login : Screen("login", R.string.login_title, null)
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Kick : Screen("kick", R.string.nav_kick, Icons.Default.ThumbUp)
    object Nutrition : Screen("nutrition", R.string.nav_nutrition, Icons.Default.List)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
}
