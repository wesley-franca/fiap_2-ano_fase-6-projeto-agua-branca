package com.aguiabranca.inovacao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aguiabranca.inovacao.data.session.Sessao
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aguiabranca.inovacao.data.model.UserRole
import com.aguiabranca.inovacao.ui.screens.auth.LoginScreen
import com.aguiabranca.inovacao.ui.screens.operador.DetalheIdeiaScreen
import com.aguiabranca.inovacao.ui.screens.operador.FormularioIdeiaScreen
import com.aguiabranca.inovacao.ui.screens.operador.OperadorHomeScreen
import com.aguiabranca.inovacao.ui.screens.gestor.GestorPainelScreen
import com.aguiabranca.inovacao.ui.screens.gestor.FilaIdeiasScreen
import com.aguiabranca.inovacao.ui.screens.lideranca.LiderancaDashboardScreen
import com.aguiabranca.inovacao.ui.theme.AppTheme
import com.aguiabranca.inovacao.ui.viewmodel.AuthViewModel
import com.aguiabranca.inovacao.ui.viewmodel.OperadorViewModel

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
    val sessaoExpirada by Sessao.expirada.collectAsState()
    val operadorViewModel: OperadorViewModel = viewModel()

    // A API recusou o token durante o uso: derruba a sessão e volta ao login.
    LaunchedEffect(sessaoExpirada) {
        if (sessaoExpirada) {
            authViewModel.sessaoExpirou()
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    // Enquanto o app confere se existe sessão salva, evita piscar a tela de login.
    if (authUiState.isRestoring) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

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

        // Operador — as telas compartilham o mesmo ViewModel para não recarregar a cada navegação.
        composable("operador_home") {
            OperadorHomeScreen(
                viewModel = operadorViewModel,
                onNavigateToNewIdeia = { navController.navigate("nova_ideia") },
                onAbrirIdeia = { id -> navController.navigate("detalhe_ideia/$id") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo("operador_home") { inclusive = true } }
                }
            )
        }

        composable("nova_ideia") {
            FormularioIdeiaScreen(
                viewModel = operadorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("detalhe_ideia/{id}") { entrada ->
            DetalheIdeiaScreen(
                ideiaId = entrada.arguments?.getString("id").orEmpty(),
                viewModel = operadorViewModel,
                onEditar = { id -> navController.navigate("editar_ideia/$id") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("editar_ideia/{id}") { entrada ->
            FormularioIdeiaScreen(
                viewModel = operadorViewModel,
                ideiaId = entrada.arguments?.getString("id"),
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
