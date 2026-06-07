package com.istech.expensestracker.ui.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.istech.expensestracker.R
import com.istech.expensestracker.databinding.FragmentExpenseListBinding
import com.istech.expensestracker.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

/**
 * ExpenseListFragment displays a paginated list of expenses with filtering options.
 */
@AndroidEntryPoint
class ExpenseListFragment : Fragment() {
    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter
    private var selectedCategory: String? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var isMonthDefault: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerExpenseList.tvHeaderTitle.text = getString(R.string.expense_list_title)
        binding.headerExpenseList.ivBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        setupRecyclerView()
        initDefaultMonthFilter()
        setupCategorySpinner()
        setupDateFilter()
        observeExpenses()
        observeFilteredTotal()
        adapter.onDeleteClick = { expense ->
            showDeleteExpenseDialog(expense)
        }
        adapter.onUpdateClick = { expense ->
            showUpdateExpenseDialog(expense)
        }
    }

    /**
     * Sets startDate/endDate to the current month's boundaries as the default filter.
     */
    private fun initDefaultMonthFilter() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        startDate = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        endDate = cal.timeInMillis
        val monthName = android.text.format.DateFormat.format("MMM yyyy", startDate!!)
        binding.btnFilterDate.text = monthName
    }

    /**
     * Sets up the RecyclerView with the ExpenseAdapter.
     */
    private fun setupRecyclerView() {
        adapter = ExpenseAdapter()
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter
        adapter.addLoadStateListener { loadState ->
            val isEmpty = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0
            binding.noData.isVisible = isEmpty
            binding.rvExpenses.isVisible = !isEmpty
        }
    }

    /**
     * Populates the category spinner and handles selection for filtering.
     */
    private fun setupCategorySpinner() {
        val categories =
            resources.getStringArray(com.istech.expensestracker.R.array.expense_categories)
        val allCategories =
            listOf(getString(com.istech.expensestracker.R.string.filter_category)) + categories
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, allCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
        binding.spinnerCategory.setSelection(0)
        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    selectedCategory = if (position == 0) null else categories[position - 1]
                    observeExpenses()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    /**
     * Sets up the date filter button to show a date picker dialog for start and end dates.
     * If a specific day is already selected (not the default month), tapping resets back to
     * the current month filter.
     */
    private fun setupDateFilter() {
        binding.btnFilterDate.setOnClickListener {
            if (!isMonthDefault) {
                initDefaultMonthFilter()
                isMonthDefault = true
                observeExpenses()
                return@setOnClickListener
            }
            val today = Calendar.getInstance()
            val dialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth, 0, 0, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    startDate = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    endDate = cal.timeInMillis
                    isMonthDefault = false
                    binding.btnFilterDate.text =
                        android.text.format.DateFormat.format("dd MMM yyyy", startDate!!)
                    observeExpenses()
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }
    }

    /**
     * Observes the paginated list of expenses from the ViewModel, applying filters if set.
     */
    private fun observeExpenses() {
        val liveData = getFilteredExpensesLiveData()
        liveData.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
        viewModel.loadFilteredTotal(selectedCategory, startDate, endDate)
    }

    private fun observeFilteredTotal() {
        viewModel.filteredTotal.observe(viewLifecycleOwner) { total ->
            binding.tvFilteredTotal.text = getString(R.string.rupees) + total.toString()
        }
    }

    private fun getFilteredExpensesLiveData() = when {
        selectedCategory != null && startDate != null && endDate != null ->
            viewModel.getExpensesByCategoryAndDatePaged(selectedCategory!!, startDate!!, endDate!!)

        selectedCategory != null ->
            viewModel.getExpensesByCategoryPaged(selectedCategory!!)

        startDate != null && endDate != null ->
            viewModel.getExpensesByDatePaged(startDate!!, endDate!!)

        else ->
            viewModel.getAllExpensesPaged()
    }

    private fun showDeleteExpenseDialog(expense: com.istech.expensestracker.model.Expense) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_expense_confirm)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                viewModel.deleteExpense(expense)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun showUpdateExpenseDialog(expense: com.istech.expensestracker.model.Expense) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_expense, null)
        val etAmount = dialogView.findViewById<android.widget.EditText>(R.id.etAmount)
        val etNote = dialogView.findViewById<android.widget.EditText>(R.id.etNote)
        val etDate = dialogView.findViewById<android.widget.EditText>(R.id.etDate)
        val spinnerCategory = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategory)

        etAmount.setText(expense.amount.toString())
        etNote.setText(expense.note ?: "")

        var selectedDate = expense.date
        etDate.setText(android.text.format.DateFormat.format("dd MMM yyyy", selectedDate))
        etDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    cal.set(year, month, dayOfMonth)
                    selectedDate = cal.timeInMillis
                    etDate.setText(android.text.format.DateFormat.format("dd MMM yyyy", selectedDate))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }

        val categories =
            resources.getStringArray(com.istech.expensestracker.R.array.expense_categories)
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        val catIndex = categories.indexOf(expense.category)
        if (catIndex >= 0) spinnerCategory.setSelection(catIndex)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.update)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val amount = etAmount.text.toString().toIntOrNull() ?: return@setPositiveButton
                val note = etNote.text.toString().takeIf { it.isNotBlank() }
                val category = spinnerCategory.selectedItem.toString()
                val updated = expense.copy(amount = amount, note = note, category = category, date = selectedDate)
                viewModel.updateExpense(updated)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 