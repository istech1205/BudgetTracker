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
        setupCategorySpinner()
        setupDateFilter()
        observeExpenses()
        adapter.onDeleteClick = { expense ->
            showDeleteExpenseDialog(expense)
        }
        adapter.onUpdateClick = { expense ->
            showUpdateExpenseDialog(expense)
        }
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
     */
    private fun setupDateFilter() {
        binding.btnFilterDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startDate = calendar.timeInMillis
                    // Set endDate to end of the same day
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    endDate = calendar.timeInMillis
                    observeExpenses()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // Do not restrict maxDate here, allow future dates for filtering
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
        val spinnerCategory = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategory)
        etAmount.setText(expense.amount.toString())
        etNote.setText(expense.note ?: "")
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
                val updated = expense.copy(amount = amount, note = note, category = category)
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