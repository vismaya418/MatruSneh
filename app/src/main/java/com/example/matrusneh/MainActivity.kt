package com.example.matrusneh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import java.util.Locale
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import com.example.matrusneh.data.local.MatruSnehDatabase
import com.example.matrusneh.data.repository.AppRepository
import com.example.matrusneh.ui.navigation.AppNavigation
import com.example.matrusneh.ui.navigation.Screen
import com.example.matrusneh.ui.theme.MatruSnehTheme
import com.example.matrusneh.ui.viewmodel.MainViewModel
import com.example.matrusneh.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = MatruSnehDatabase.getDatabase(this)
        val repository = AppRepository(database.userDao(), database.kickDao(), database.waterDao())
        val viewModel: MainViewModel by viewModels { MainViewModelFactory(repository) }

        setContent {
            val user by viewModel.user.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            // Handle Language Change
            val context = LocalContext.current
            LaunchedEffect(user?.languagePreference) {
                user?.languagePreference?.let { lang ->
                    val locale = Locale(lang)
                    Locale.setDefault(locale)
                    val config = Configuration(context.resources.configuration)
                    config.setLocale(locale)
                    context.resources.updateConfiguration(config, context.resources.displayMetrics)
                }
            }

            MatruSnehTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (user != null) {
                        // User exists, go to Home
                        AppNavigation(viewModel = viewModel, startDestination = Screen.Home.route)
                    } else {
                        // No user, go to Welcome
                        AppNavigation(viewModel = viewModel, startDestination = Screen.Welcome.route)
                    }
                }
            }
        }
    }
}
