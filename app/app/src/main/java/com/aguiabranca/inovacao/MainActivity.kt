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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.aguiabranca.inovacao.data.model.UserRole
import com.aguiabranca.inovacao.data.session.Sessao
import com.aguiabranca.inovacao.ui.screens.auth.LoginScreen
import com.aguiabranca.inovacao.ui.screens.gestor.DetalheIdeiaGestorScreen
import com.aguiabranca.inovacao.ui.screens.gestor.DetalheProjetoScreen
import com.aguiabranca.inovacao.ui.screens.gestor.FilaIdeiasScreen
import com.aguiabranca.inovacao.ui.screens.gestor.FormularioProjetoScreen
import com.aguiabranca.inovacao.ui.screens.gestor.GestorPainelScreen
import com.aguiabranca.inovacao.ui.screens.gestor.ProjetosGestorScreen
import com.aguiabranca.inovacao.ui.screens.lideranca.DetalheProjetoLiderancaScreen
import com.aguiabranca.inovacao.ui.screens.lideranca.FormularioOrientacaoScreen
import com.aguiabranca.inovacao.ui.screens.lideranca.HistoricoOrientacaoScreen
import com.aguiabranca.inovacao.ui.screens.lideranca.LiderancaDashboardScreen
import com.aguiabranca.inovacao.ui.screens.lideranca.OrientacoesScreen
import com.aguiabranca.inovacao.ui.screens.operador.DetalheIdeiaScreen
import com.aguiabranca.inovacao.ui.screens.operador.FormularioIdeiaScreen
import com.aguiabranca.inovacao.ui.screens.operador.OperadorHomeScreen
import com.aguiabranca.inovacao.ui.theme.AppTheme
import com.aguiabranca.inovacao.ui.viewmodel.AuthViewModel
import com.aguiabranca.inovacao.ui.viewmodel.GestorViewModel
import com.aguiabranca.inovacao.ui.viewmodel.LiderancaViewModel
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

private const val GRAFO_OPERADOR = "operador"
private const val GRAFO_GESTOR = "gestor"
private const val GRAFO_LIDERANCA = "lideranca"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()
    val sessaoExpirada by Sessao.expirada.collectAsState()

    // A API recusou o token durante o uso: derruba a sessão e volta ao login.
    LaunchedEffect(sessaoExpirada) {
        if (sessaoExpirada) {
            authViewModel.sessaoExpirou()
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    // Cada perfil entra no seu próprio grafo; trocar de usuário leva para a home certa.
    LaunchedEffect(authUiState.isLoggedIn, authUiState.currentUser?.role) {
        val destino = destinoDoPerfil(authUiState.currentUser?.role)
        if (authUiState.isLoggedIn && destino != null) {
            navController.navigate(destino) { popUpTo(0) { inclusive = true } }
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
        startDestination = destinoDoPerfil(authUiState.currentUser?.role).takeIf { authUiState.isLoggedIn } ?: "login"
    ) {
        composable("login") {
            LoginScreen(viewModel = authViewModel, onLoginSuccess = { /* a navegação é feita pelo perfil */ })
        }

        // ---------- Operador ----------
        navigation(startDestination = "operador_home", route = GRAFO_OPERADOR) {
            composable("operador_home") { entrada ->
                val viewModel = viewModelDoGrafo<OperadorViewModel>(navController, entrada, GRAFO_OPERADOR)
                OperadorHomeScreen(
                    viewModel = viewModel,
                    onNavigateToNewIdeia = { navController.navigate("nova_ideia") },
                    onAbrirIdeia = { id -> navController.navigate("detalhe_ideia/$id") },
                    onLogout = { sair(authViewModel, navController) }
                )
            }

            composable("nova_ideia") { entrada ->
                FormularioIdeiaScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_OPERADOR),
                    onBack = { navController.popBackStack() }
                )
            }

            composable("detalhe_ideia/{id}") { entrada ->
                DetalheIdeiaScreen(
                    ideiaId = entrada.arguments?.getString("id").orEmpty(),
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_OPERADOR),
                    onEditar = { id -> navController.navigate("editar_ideia/$id") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("editar_ideia/{id}") { entrada ->
                FormularioIdeiaScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_OPERADOR),
                    ideiaId = entrada.arguments?.getString("id"),
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ---------- Gestor ----------
        navigation(startDestination = "gestor_painel", route = GRAFO_GESTOR) {
            composable("gestor_painel") { entrada ->
                GestorPainelScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_GESTOR),
                    onNavigateToFilaIdeias = { navController.navigate("fila_ideias") },
                    onNavigateToProjetos = { navController.navigate("projetos") },
                    onLogout = { sair(authViewModel, navController) }
                )
            }

            composable("fila_ideias") { entrada ->
                FilaIdeiasScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_GESTOR),
                    onAbrirIdeia = { id -> navController.navigate("analisar_ideia/$id") },
                    onBackPress = { navController.popBackStack() }
                )
            }

            composable("analisar_ideia/{id}") { entrada ->
                DetalheIdeiaGestorScreen(
                    ideiaId = entrada.arguments?.getString("id").orEmpty(),
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_GESTOR),
                    onBack = { navController.popBackStack() }
                )
            }

            composable("projetos") { entrada ->
                ProjetosGestorScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_GESTOR),
                    onNovoProjeto = { navController.navigate("novo_projeto") },
                    onAbrirProjeto = { id -> navController.navigate("detalhe_projeto/$id") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("novo_projeto") { entrada ->
                FormularioProjetoScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_GESTOR),
                    onBack = { navController.popBackStack() }
                )
            }

            composable("detalhe_projeto/{id}") { entrada ->
                DetalheProjetoScreen(
                    projetoId = entrada.arguments?.getString("id").orEmpty(),
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_GESTOR),
                    onEditar = { id -> navController.navigate("editar_projeto/$id") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("editar_projeto/{id}") { entrada ->
                FormularioProjetoScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_GESTOR),
                    projetoId = entrada.arguments?.getString("id"),
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ---------- Liderança ----------
        navigation(startDestination = "lideranca_dashboard", route = GRAFO_LIDERANCA) {
            composable("lideranca_dashboard") { entrada ->
                LiderancaDashboardScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_LIDERANCA),
                    onAbrirOrientacoes = { navController.navigate("orientacoes") },
                    onAbrirProjeto = { id -> navController.navigate("retorno_projeto/$id") },
                    onLogout = { sair(authViewModel, navController) }
                )
            }

            composable("orientacoes") { entrada ->
                OrientacoesScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_LIDERANCA),
                    onNova = { navController.navigate("nova_orientacao") },
                    onEditar = { id -> navController.navigate("editar_orientacao/$id") },
                    onHistorico = { id -> navController.navigate("historico_orientacao/$id") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("nova_orientacao") { entrada ->
                FormularioOrientacaoScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_LIDERANCA),
                    onBack = { navController.popBackStack() }
                )
            }

            composable("editar_orientacao/{id}") { entrada ->
                FormularioOrientacaoScreen(
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_LIDERANCA),
                    orientacaoId = entrada.arguments?.getString("id"),
                    onBack = { navController.popBackStack() }
                )
            }

            composable("historico_orientacao/{id}") { entrada ->
                HistoricoOrientacaoScreen(
                    orientacaoId = entrada.arguments?.getString("id").orEmpty(),
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_LIDERANCA),
                    onBack = { navController.popBackStack() }
                )
            }

            composable("retorno_projeto/{id}") { entrada ->
                DetalheProjetoLiderancaScreen(
                    projetoId = entrada.arguments?.getString("id").orEmpty(),
                    viewModel = viewModelDoGrafo(navController, entrada, GRAFO_LIDERANCA),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun destinoDoPerfil(role: UserRole?): String? = when (role) {
    UserRole.OPERADOR -> GRAFO_OPERADOR
    UserRole.GESTOR -> GRAFO_GESTOR
    UserRole.LIDERANCA -> GRAFO_LIDERANCA
    null -> null
}

private fun sair(authViewModel: AuthViewModel, navController: NavHostController) {
    authViewModel.logout()
    navController.navigate("login") { popUpTo(0) { inclusive = true } }
}

/**
 * ViewModel compartilhado pelas telas de um mesmo perfil: vive enquanto o grafo daquele perfil
 * estiver na pilha, então entrar como operador não cria os ViewModels de gestor e liderança.
 */
@Composable
private inline fun <reified T : ViewModel> viewModelDoGrafo(
    navController: NavHostController,
    entrada: NavBackStackEntry,
    rotaDoGrafo: String
): T {
    val dono = runCatching { navController.getBackStackEntry(rotaDoGrafo) }.getOrDefault(entrada)
    return viewModel(viewModelStoreOwner = dono)
}
