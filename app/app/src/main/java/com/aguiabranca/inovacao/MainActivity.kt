package com.aguiabranca.inovacao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aguiabranca.inovacao.data.model.UserRole
import com.aguiabranca.inovacao.ui.screens.auth.LoginScreen
import com.aguiabranca.inovacao.ui.screens.operador.OperadorHomeScreen
import com.aguiabranca.inovacao.ui.screens.operador.NovaIdeiaScreen
import com.aguiabranca.inovacao.ui.screens.gestor.GestorPainelScreen
import com.aguiabranca.inovacao.ui.screens.gestor.FilaIdeiasScreen
import com.aguiabranca.inovacao.ui.screens.lideranca.LiderancaDashboardScreen
import com.aguiabranca.inovacao.ui.theme.AppTheme
import com.aguiabranca.inovacao.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authUiState.isLoggedIn) {
            when (authUiState.currentUser?.role) {
                UserRole.OPERADOR -> "operador_home"
                UserRole.GESTOR -> "gestor_painel"
                UserRole.LIDERANCA -> "lideranca_dashboard"
                else -> "login"
            }
        } else {
            "login"
        }
    ) {
        // Login
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    when (authUiState.currentUser?.role) {
                        UserRole.OPERADOR -> navController.navigate("operador_home") {
                            popUpTo("login") { inclusive = true }
                        }
                        UserRole.GESTOR -> navController.navigate("gestor_painel") {
                            popUpTo("login") { inclusive = true }
                        }
                        UserRole.LIDERANCA -> navController.navigate("lideranca_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                        else -> {}
                    }
                }
            )
        }

        // Operador
        composable("operador_home") {
            OperadorHomeScreen(
                onNavigateToNewIdeia = { navController.navigate("nova_ideia") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo("operador_home") { inclusive = true } }
                }
            )
        }

        composable("nova_ideia") {
            NovaIdeiaScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Gestor
        composable("gestor_painel") {
            GestorPainelScreen(
                onNavigateToFilaIdeias = { navController.navigate("fila_ideias") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo("gestor_painel") { inclusive = true } }
                }
            )
        }

        composable("fila_ideias") {
            FilaIdeiasScreen(
                onBackPress = { navController.popBackStack() }
            )
        }

        // Liderança
        composable("lideranca_dashboard") {
            LiderancaDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo("lideranca_dashboard") { inclusive = true } }
                }
            )
        }
    }
}
