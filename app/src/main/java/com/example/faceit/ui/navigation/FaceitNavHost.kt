package com.example.faceit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.faceit.ui.screens.AddMatchScreen
import com.example.faceit.ui.screens.HomeDashboardScreen
import com.example.faceit.ui.screens.PlayerDetailScreen
import com.example.faceit.ui.screens.PlayerFormScreen

@Composable
fun FaceitNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeDashboardScreen(
                onOpenMyProfile = { id -> navController.navigate(PlayerDetailRoute(playerId = id)) },
                onOpenPlayer = { id -> navController.navigate(PlayerDetailRoute(playerId = id)) },
                onAddPlayer = { navController.navigate(PlayerFormRoute()) },
                onAddMatch = { id -> navController.navigate(AddMatchRoute(playerId = id)) }
            )
        }
        composable<PlayerDetailRoute> {
            PlayerDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                onEditPlayer = { id -> navController.navigate(PlayerFormRoute(playerId = id)) },
                onAddMatch = { id -> navController.navigate(AddMatchRoute(playerId = id)) },
                onEditMatch = { playerId, matchId ->
                    navController.navigate(AddMatchRoute(playerId = playerId, matchId = matchId))
                }
            )
        }
        composable<PlayerFormRoute> {
            PlayerFormScreen(
                onNavigateUp = { navController.navigateUp() },
                onSaved = { navController.navigateUp() }
            )
        }
        composable<AddMatchRoute> {
            AddMatchScreen(
                onNavigateUp = { navController.navigateUp() },
                onSaved = { navController.navigateUp() }
            )
        }
    }
}
