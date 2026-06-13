package com.istech.expensestracker.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddExpense : Screen("add_expense")
    data object ExpenseList : Screen("expense_list")
    data object SetBudget : Screen("set_budget")
}
