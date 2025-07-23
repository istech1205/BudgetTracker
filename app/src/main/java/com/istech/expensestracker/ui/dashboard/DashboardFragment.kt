package com.istech.expensestracker.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.istech.expensestracker.databinding.FragmentDashboardBinding
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import com.istech.expensestracker.R
import com.istech.expensestracker.viewmodel.BudgetViewModel
import com.istech.expensestracker.viewmodel.ExpenseViewModel
import java.util.Calendar
import dagger.hilt.android.AndroidEntryPoint

/**
 * DashboardFragment displays the monthly budget, total expenses, remaining balance,
 * and provides navigation to add expense, set budget, and view pie chart.
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSummaryObservers()
        binding.btnViewExpenseHistory.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_expenseList)
        }
        binding.btnAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addExpense)
        }
        binding.btnSetBudget.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_setBudget)
        }
        binding.btnViewPieChart.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_pieChart)
        }
    }

    /**
     * Observes budget and expenses for the current month and updates the dashboard summary.
     */
    private fun setupSummaryObservers() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        budgetViewModel.loadBudget(month, year)
        expenseViewModel.loadTotalExpensesForMonth(month, year)
        budgetViewModel.budget.observe(viewLifecycleOwner) { budget ->
            binding.tvMonthlyBudget.text = getString(com.istech.expensestracker.R.string.monthly_budget) + (budget?.amount?.toString() ?: "0.0")
            updateRemainingBalance()
        }
        expenseViewModel.totalExpenses.observe(viewLifecycleOwner) { total ->
            binding.tvTotalExpenses.text = getString(com.istech.expensestracker.R.string.total_expenses) + total.toString()
            updateRemainingBalance()
        }
    }

    /**
     * Updates the remaining balance based on budget and expenses.
     */
    private fun updateRemainingBalance() {
        val budget = budgetViewModel.budget.value?.amount ?: 0.0
        val expenses = expenseViewModel.totalExpenses.value ?: 0.0
        val remaining = budget - expenses
        binding.tvRemainingBalance.text = getString(com.istech.expensestracker.R.string.remaining_balance) + remaining.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 