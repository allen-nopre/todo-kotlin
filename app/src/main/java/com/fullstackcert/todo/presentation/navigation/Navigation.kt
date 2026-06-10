package com.fullstackcert.todo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fullstackcert.todo.presentation.auth.LoginScreen
import com.fullstackcert.todo.presentation.auth.RegisterScreen
import com.fullstackcert.todo.presentation.auth.SplashScreen
import com.fullstackcert.todo.presentation.todo.AddEditTodoScreen
import com.fullstackcert.todo.presentation.todo.TodoDetailScreen
import com.fullstackcert.todo.presentation.todo.TodoListScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TODO_LIST = "todo_list"
    const val TODO_DETAIL = "todo_detail/{todoId}"
    const val ADD_TODO = "add_todo"
    const val EDIT_TODO = "edit_todo/{todoId}"

    fun todoDetail(id: Int) = "todo_detail/$id"
    fun editTodo(id: Int) = "edit_todo/$id"
}

@Composable
fun TodoNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToTodos = {
                    navController.navigate(Routes.TODO_LIST) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.TODO_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TODO_LIST) { backStackEntry ->
            val refreshed = backStackEntry.savedStateHandle
                .getStateFlow("refreshed", false)
                .collectAsState()
            TodoListScreen(
                onNavigateToDetail = { id -> navController.navigate(Routes.todoDetail(id)) },
                onNavigateToAdd = { navController.navigate(Routes.ADD_TODO) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                refreshTrigger = refreshed.value,
                onRefreshConsumed = {
                    backStackEntry.savedStateHandle["refreshed"] = false
                }
            )
        }

        composable(
            Routes.TODO_DETAIL,
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val refreshed = backStackEntry.savedStateHandle
                .getStateFlow("refreshed", false)
                .collectAsState()
            TodoDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Routes.editTodo(id)) },
                refreshTrigger = refreshed.value,
                onRefreshConsumed = {
                    backStackEntry.savedStateHandle["refreshed"] = false
                }
            )
        }

        composable(Routes.ADD_TODO) {
            AddEditTodoScreen(
                todoId = null,
                onNavigateBack = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refreshed", true)
                    navController.popBackStack()
                }
            )
        }

        composable(
            Routes.EDIT_TODO,
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId")
            AddEditTodoScreen(
                todoId = todoId,
                onNavigateBack = {
                    navController.navigate(Routes.TODO_LIST) {
                        popUpTo(Routes.TODO_LIST) { inclusive = false }
                    }
                }
            )
        }
    }
}
