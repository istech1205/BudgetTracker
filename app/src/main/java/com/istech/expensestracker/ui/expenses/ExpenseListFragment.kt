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
        val header = requireView().findViewById<android.view.View>(R.id.header_expense_list)
        val title = header.findViewById<android.widget.TextView>(R.id.tv_header_title)
        val back = header.findViewById<android.widget.ImageView>(R.id.iv_back)
        title.text = getString(R.string.expense_list_title)
        back.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        setupRecyclerView()
        setupCategorySpinner()
        setupDateFilter()
        observeExpenses()
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
                            observeExpenses()
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
    }

    /**
     * Observes the paginated list of expenses from the ViewModel, applying filters if set.
     */
    private fun observeExpenses() {
        val category = selectedCategory
        val start = startDate
        val end = endDate
        val liveData = when {
            category != null -> viewModel.getExpensesByCategoryPaged(category)
            start != null && end != null -> viewModel.getExpensesByDatePaged(start, end)
            else -> viewModel.getAllExpensesPaged()
        }
        liveData.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 