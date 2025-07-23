package com.istech.expensestracker.ui.dashboard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.istech.expensestracker.R
import com.istech.expensestracker.databinding.FragmentDashboardBinding
import com.istech.expensestracker.ui.expenses.ExpenseAdapter
import com.istech.expensestracker.viewmodel.BudgetViewModel
import com.istech.expensestracker.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

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
    private lateinit var expenseAdapter: ExpenseAdapter
    private var selectedCategory: String? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var filterDialog: BottomSheetDialog? = null
    private var categoryDialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSummaryObservers()
        setupDashboardDateFilter()
        setupExpenseHistoryRecycler()
        binding.llAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addExpense)
        }
        binding.llSetBudget.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_setBudget)
        }
        binding.ivSeeProgress.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_pieChart)
        }
        binding.btCategory.setOnClickListener {
            showCategoryBottomSheet()
        }
        // See All button is currently commented out
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
            binding.tvMonthlyBudget.text =
                getString(R.string.rupees) + (budget?.amount?.toString() ?: "0.0")
            updateRemainingBalance()
        }
        expenseViewModel.totalExpenses.observe(viewLifecycleOwner) { total ->
            binding.tvTotalExpenses.text = getString(R.string.rupees) + total.toString()
            updateRemainingBalance()
        }
    }


    private fun setupDashboardDateFilter() {
        binding.btDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupExpenseHistoryRecycler() {
        expenseAdapter = ExpenseAdapter()
        binding.rvDashboardExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDashboardExpenses.adapter = expenseAdapter
        observeDashboardExpenses()
    }


    private fun showCategoryBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        val categories = resources.getStringArray(R.array.expense_categories)
        categories.forEach { category ->
            val tv = TextView(requireContext())
            tv.text = category
            tv.setPadding(40, 40, 40, 40)
            tv.textSize = 18f
            tv.setOnClickListener {
                selectedCategory = category
                observeDashboardExpenses()
                dialog.dismiss()
            }
            container.addView(tv)
        }
        val tvClear = TextView(requireContext())
        tvClear.text = getString(R.string.filter_category) + " (Clear)"
        tvClear.setPadding(40, 40, 40, 40)
        tvClear.textSize = 16f
        tvClear.setOnClickListener {
            selectedCategory = null
            observeDashboardExpenses()
            dialog.dismiss()
        }
        container.addView(tvClear)
        dialog.setContentView(container)
        dialog.show()
        categoryDialog = dialog
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                startDate = calendar.timeInMillis
                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDayOfMonth ->
                        calendar.set(endYear, endMonth, endDayOfMonth)
                        endDate = calendar.timeInMillis
                        observeDashboardExpenses()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun observeDashboardExpenses() {
        val category = selectedCategory
        val start = startDate
        val end = endDate
        val liveData = when {
            category != null && start != null && end != null -> expenseViewModel.getExpensesByCategoryPaged(
                category
            ) // You can implement a combined filter if needed
            category != null -> expenseViewModel.getExpensesByCategoryPaged(category)
            start != null && end != null -> expenseViewModel.getExpensesByDatePaged(start, end)
            else -> expenseViewModel.getAllExpensesPaged()
        }
        liveData.observe(viewLifecycleOwner) {
            expenseAdapter.submitData(lifecycle, it)
        }
    }

    /**
     * Updates the remaining balance based on budget and expenses.
     */
    private fun updateRemainingBalance() {
        val budget = budgetViewModel.budget.value?.amount ?: 0.0
        val expenses = expenseViewModel.totalExpenses.value ?: 0.0
        val remaining = budget - expenses
        binding.tvRemainingBalance.text = getString(R.string.rupees) + remaining.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 