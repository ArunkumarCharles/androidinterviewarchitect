package com.sevvanam.android_interview_architect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sevvanam.android_interview_architect.feature.checkout.CheckoutRoute
import com.sevvanam.android_interview_architect.feature.feed.FeedRoute
import com.sevvanam.android_interview_architect.feature.profile.ProfileRoute
import com.sevvanam.android_interview_architect.feature.topic.TopicRoute
import com.sevvanam.android_interview_architect.navigation.AppRoute
import com.sevvanam.android_interview_architect.ui.theme.AndroidinterviewarchitectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            AndroidinterviewarchitectTheme(darkTheme = darkTheme) {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun navigateTo(route: AppRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed (MVI)") },
                    selected = currentDestination?.hasRoute<AppRoute.Feed>() == true,
                    onClick = { navigateTo(AppRoute.Feed) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile (MVVM)") },
                    selected = currentDestination?.hasRoute<AppRoute.Profile>() == true,
                    onClick = { navigateTo(AppRoute.Profile) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Checkout") },
                    label = { Text("Checkout") },
                    selected = currentDestination?.hasRoute<AppRoute.Checkout>() == true,
                    onClick = { navigateTo(AppRoute.Checkout) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Topics") },
                    label = { Text("Topics") },
                    selected = currentDestination?.hasRoute<AppRoute.Topic>() == true,
                    onClick = { navigateTo(AppRoute.Topic) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Feed,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<AppRoute.Feed> { FeedRoute() }
            composable<AppRoute.Profile> { ProfileRoute() }
            composable<AppRoute.Checkout> { CheckoutRoute() }
            composable<AppRoute.Topic> { TopicRoute() }
        }
    }
}
