package com.istech.expensestracker.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.istech.expensestracker.R
import com.istech.expensestracker.ui.navigation.Screen
import com.istech.expensestracker.ui.screens.AddExpenseScreen
import com.istech.expensestracker.ui.screens.DashboardScreen
import com.istech.expensestracker.ui.screens.ExpenseListScreen
import com.istech.expensestracker.ui.screens.SetBudgetScreen
import com.istech.expensestracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity serves as the entry point for the app's UI using Jetpack Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setStatusBarColor()

        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                onAddExpense = { navController.navigate(Screen.AddExpense.route) },
                                onSetBudget = { navController.navigate(Screen.SetBudget.route) },
                                onViewHistory = { navController.navigate(Screen.ExpenseList.route) }
                            )
                        }

                        composable(Screen.AddExpense.route) {
                            AddExpenseScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.ExpenseList.route) {
                            ExpenseListScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.SetBudget.route) {
                            SetBudgetScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.primaryDark))
                view.setPadding(0, statusBarInsets.top, 0, 0)
                insets
            }
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryDark)
        }
    }
} 