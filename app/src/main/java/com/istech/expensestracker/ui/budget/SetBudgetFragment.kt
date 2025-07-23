package com.istech.expensestracker.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.istech.expensestracker.databinding.FragmentSetBudgetBinding
import com.istech.expensestracker.model.Budget
import com.istech.expensestracker.viewmodel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import android.widget.Toast

/**
 * SetBudgetFragment allows the user to set or edit the monthly budget.
 */
@AndroidEntryPoint
class SetBudgetFragment : Fragment() {
    private var _binding: FragmentSetBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonthSpinner()
        setDefaults()
        binding.btnSaveBudget.setOnClickListener { saveBudget() }
    }

    /**
     * Sets up the month dropdown spinner.
     */
    private fun setupMonthSpinner() {
        val months = resources.getStringArray(com.istech.expensestracker.R.array.months)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = adapter
    }

    /**
     * Sets default values for month and year fields.
     */
    private fun setDefaults() {
        val calendar = Calendar.getInstance()
        binding.spinnerMonth.setSelection(calendar.get(Calendar.MONTH))
        binding.etYear.setText(calendar.get(Calendar.YEAR).toString())
    }

    /**
     * Collects input and saves the budget using the ViewModel.
     */
    private fun saveBudget() {
        val amount = binding.etBudgetAmount.text.toString().toDoubleOrNull()
        val month = binding.spinnerMonth.selectedItemPosition + 1
        val year = binding.etYear.text.toString().toIntOrNull()
        if (amount == null || year == null) {
            Toast.makeText(requireContext(), getString(com.istech.expensestracker.R.string.input_error), Toast.LENGTH_SHORT).show()
            return
        }
        val budget = com.istech.expensestracker.model.Budget(amount = amount, month = month, year = year)
        viewModel.insertOrUpdateBudget(budget)
        Toast.makeText(requireContext(), getString(com.istech.expensestracker.R.string.save_success), Toast.LENGTH_SHORT).show()
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 