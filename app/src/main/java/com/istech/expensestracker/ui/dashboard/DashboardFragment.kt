package com.istech.expensestracker.ui.dashboard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.istech.expensestracker.R
import com.istech.expensestracker.databinding.FragmentDashboardBinding
import com.istech.expensestracker.ui.expenses.ExpenseAdapter
import com.istech.expensestracker.viewmodel.BudgetViewModel
import com.istech.expensestracker.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import android.widget.NumberPicker
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer

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
    private var selectedMonth: Int = -1
    private var selectedYear: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        updateMonthButton()
        binding.btnSelectMonth.setOnClickListener {
            showMonthYearPicker()
        }

        setupSummaryObservers()

        setupPieChart()
        handleClick()

    }

    private fun handleClick() {
        binding.llAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addExpense)
        }
        binding.llSetBudget.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_setBudget)
        }
        binding.btHistory.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_expenseList)
        }
    }

    private fun initData() {
        val calendar = Calendar.getInstance()
        selectedMonth = calendar.get(Calendar.MONTH) + 1
        selectedYear = calendar.get(Calendar.YEAR)
    }

    private fun updateMonthButton() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, selectedMonth - 1)
        val monthName =
            cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())
        binding.btnSelectMonth.text = "$monthName $selectedYear"
    }

    private fun showMonthYearPicker() {
        // Custom dialog with NumberPickers for month and year
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        val months = resources.getStringArray(R.array.months)
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.displayedValues = months
        monthPicker.value = selectedMonth
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 10
        yearPicker.maxValue = currentYear + 10
        yearPicker.value = selectedYear
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_month))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                selectedMonth = monthPicker.value
                selectedYear = yearPicker.value
                updateMonthButton()
                loadPieChart(selectedMonth, selectedYear)
            }
            .setNegativeButton(android.R.string.cancel, null)
        builder.show()
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
                getString(R.string.rupees) + (budget?.amount?.toString() ?: "0")
            updateRemainingBalance()
        }
        expenseViewModel.totalExpenses.observe(viewLifecycleOwner) { total ->
            binding.tvTotalExpenses.text = getString(R.string.rupees) + total.toString()
            updateRemainingBalance()
        }
    }


    /**
     * Sets up and loads the pie chart for the current month and year.
     */
    private fun setupPieChart() {
        loadPieChart(selectedMonth, selectedYear)
    }

    private fun loadPieChart(month: Int, year: Int) {
        lifecycleScope.launch {
            val categoryTotals = expenseViewModel.getCategoryTotalsForMonth(month, year)
            val entries = categoryTotals.map { PieEntry(it.total.toFloat(), it.category) }
            val pieChart = binding.pieChart
            pieChart.isVisible = entries.isNotEmpty()
            binding.tvNoPieChart.isVisible = entries.isEmpty()
            val layoutParams = binding.llStatsLayout.layoutParams as ConstraintLayout.LayoutParams

            if (entries.isNotEmpty()) {
                pieChart.visibility = View.VISIBLE
                binding.tvNoPieChart.visibility = View.GONE

                layoutParams.topToBottom = pieChart.id

            } else {
                pieChart.visibility = View.INVISIBLE
                binding.tvNoPieChart.visibility = View.VISIBLE

                layoutParams.topToBottom = binding.tvNoPieChart.id
            }

            binding.llStatsLayout.layoutParams = layoutParams

            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "").apply {
                    setColors(*com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS)
                    setDrawValues(false) // Hide values on slices
                }
                val data = PieData(dataSet)
                data.setDrawValues(false) // Hide values on slices
                pieChart.data = data
                pieChart.setDrawEntryLabels(false) // Hide labels on slices
                pieChart.invalidate()
                binding.pieChart.description = null
            } else {
                pieChart.clear()
            }
        }
    }

    /**
     * Updates the remaining balance based on budget and expenses.
     */
    private fun updateRemainingBalance() {
        val budget = budgetViewModel.budget.value?.amount ?: 0
        val expenses = expenseViewModel.totalExpenses.value ?: 0
        val remaining = budget - expenses
        binding.tvRemainingBalance.text = getString(R.string.rupees) + remaining.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 