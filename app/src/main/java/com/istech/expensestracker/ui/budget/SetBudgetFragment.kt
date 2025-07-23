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
import com.istech.expensestracker.R

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
        // Observe changes to month/year and prefill budget
        initViews()
        initCallbacks()
        handleClick()
        // Initial prefill
        loadAndPrefillBudget()
    }

    private fun handleClick() {
        binding.spinnerMonth.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    loadAndPrefillBudget()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }
        binding.etYear.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) loadAndPrefillBudget()
        }
        binding.btnSaveBudget.setOnClickListener { saveBudget() }
    }

    private fun initCallbacks() {
        setupMonthSpinner()
        setDefaults()
        // Observe budget LiveData to prefill
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            if (budget != null) {
                binding.etBudgetAmount.setText(budget.amount.toString())
            } else {
                binding.etBudgetAmount.setText("")
            }
        }
    }

    private fun initViews() {
        binding.headerSetBudget.tvHeaderTitle.text = getString(R.string.set_budget_title)
        binding.headerSetBudget.ivBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
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

    private fun loadAndPrefillBudget() {
        val month = binding.spinnerMonth.selectedItemPosition + 1
        val year = binding.etYear.text.toString().toIntOrNull() ?: return
        viewModel.loadBudget(month, year)
    }

    /**
     * Collects input and saves the budget using the ViewModel.
     */
    private fun saveBudget() {
        val amount = binding.etBudgetAmount.text.toString().toIntOrNull()
        if (amount != null) {
            if (amount < 1000) {
                Toast.makeText(
                    requireContext(), "Budget should not be less than 1000", Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        val month = binding.spinnerMonth.selectedItemPosition + 1
        val year = binding.etYear.text.toString().toIntOrNull()
        if (amount == null || year == null) {
            Toast.makeText(
                requireContext(),
                getString(com.istech.expensestracker.R.string.input_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val budget =
            com.istech.expensestracker.model.Budget(amount = amount, month = month, year = year)
        viewModel.insertOrUpdateBudget(budget)
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