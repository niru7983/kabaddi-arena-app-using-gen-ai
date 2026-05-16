package com.example.kabaddiarena.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.kabaddiarena.ui.logger.LiveLoggerScreen
import com.example.kabaddiarena.ui.logger.LiveLoggerViewModel
import com.example.kabaddiarena.ui.summary.PerformanceSummaryScreen
import com.example.kabaddiarena.ui.summary.PerformanceSummaryViewModel
import com.example.kabaddiarena.ui.home.HomeScreen
import com.example.kabaddiarena.ui.auth.LoginScreen
import com.example.kabaddiarena.ui.auth.SignupScreen
import com.example.kabaddiarena.ui.profile.ProfileScreen
import com.example.kabaddiarena.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object LoginRoute
@Serializable object SignupRoute
@Serializable object HomeRoute
@Serializable object ProfileRoute
@Serializable object SettingsRoute
@Serializable data class LoggerRoute(val matchId: Long)
@Serializable data class SummaryRoute(val matchId: Long)

@Composable
fun KabaddiNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = LoginRoute
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(SignupRoute)
                }
            )
        }

        composable<SignupRoute> {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                onNavigateToLogger = { matchId ->
                    navController.navigate(LoggerRoute(matchId))
                },
                onNavigateToSummary = { matchId ->
                    navController.navigate(SummaryRoute(matchId))
                }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen()
        }

        composable<LoggerRoute> { backStackEntry ->
            val route: LoggerRoute = backStackEntry.toRoute()
            val viewModel: LiveLoggerViewModel = hiltViewModel()
            
            LaunchedEffect(route.matchId) {
                viewModel.setMatch(route.matchId)
            }

            LiveLoggerScreen(
                viewModel = viewModel,
                onFinishMatch = {
                    navController.navigate(SummaryRoute(route.matchId))
                }
            )
        }

        composable<SummaryRoute> { backStackEntry ->
            val route: SummaryRoute = backStackEntry.toRoute()
            val viewModel: PerformanceSummaryViewModel = hiltViewModel()

            LaunchedEffect(route.matchId) {
                viewModel.loadSummary(route.matchId)
            }

            PerformanceSummaryScreen(viewModel = viewModel)
        }
    }
}
