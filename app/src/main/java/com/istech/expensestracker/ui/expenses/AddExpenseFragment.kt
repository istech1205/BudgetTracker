package com.istech.expensestracker.ui.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.istech.expensestracker.databinding.FragmentAddExpenseBinding
import com.istech.expensestracker.model.Expense
import com.istech.expensestracker.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import android.widget.Toast
import com.istech.expensestracker.R

/**
 * AddExpenseFragment allows the user to add a new expense.
 */
@AndroidEntryPoint
class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        handleClick()
        initCallbacks()
    }

    private fun initCallbacks() {
        setupCategorySpinner()
        setupDatePicker()
    }

    private fun handleClick() {
        binding.headerAddExpense.ivBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.btnSaveExpense.setOnClickListener {
            if (binding.etAmount.text.isNotBlank() && binding.etAmount.text.toString().toInt() > 0) {
                saveExpense()

            } else {
                Toast.makeText(
                    requireContext(),
                    "Amount should not be less than 0",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun initViews() {
        binding.headerAddExpense.tvHeaderTitle.text = getString(R.string.add_expense_title)

    }

    /**
     * Sets up the category dropdown spinner.
     */
    private fun setupCategorySpinner() {
        val categories =
            resources.getStringArray(com.istech.expensestracker.R.array.expense_categories)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    /**
     * Sets up the date picker for selecting the expense date.
     */
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.timeInMillis
                    binding.etDate.setText(
                        android.text.format.DateFormat.format(
                            "dd MMM yyyy",
                            selectedDate
                        )
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // Disable future dates
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }
        binding.etDate.setText(android.text.format.DateFormat.format("dd MMM yyyy", selectedDate))
    }

    /**
     * Collects input and saves the expense using the ViewModel.
     */
    private fun saveExpense() {
        val amount = binding.etAmount.text.toString().toIntOrNull()
        val category = binding.spinnerCategory.selectedItem.toString()
        if (amount == null || category.isBlank()) {
            Toast.makeText(
                requireContext(),
                getString(com.istech.expensestracker.R.string.input_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val note = binding.etNote.text.toString().takeIf { it.isNotBlank() }
        val expense = com.istech.expensestracker.model.Expense(
            amount = amount,
            category = category,
            date = selectedDate,
            note = note
        )
        viewModel.insertExpense(expense)
        Toast.makeText(
            requireContext(),
            getString(com.istech.expensestracker.R.string.save_success),
            Toast.LENGTH_SHORT
        ).show()
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 