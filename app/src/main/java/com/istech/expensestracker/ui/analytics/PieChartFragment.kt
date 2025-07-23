package com.istech.expensestracker.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.istech.expensestracker.R
import com.istech.expensestracker.databinding.FragmentPieChartBinding
import com.istech.expensestracker.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * PieChartFragment displays a pie chart of spending by category for a selected month and year.
 */
@AndroidEntryPoint
class PieChartFragment : Fragment() {
    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerPieChart.tvHeaderTitle.text = getString(R.string.pie_chart_title)
        binding.headerPieChart.ivBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        setupMonthSpinner()
        setDefaults()
        loadPieChart()
        binding.spinnerPieMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                loadPieChart()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        binding.etPieYear.setOnEditorActionListener { _, _, _ -> loadPieChart(); false }
    }

    /**
     * Sets up the month dropdown spinner.
     */
    private fun setupMonthSpinner() {
        val months = resources.getStringArray(R.array.months)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPieMonth.adapter = adapter
    }

    /**
     * Sets default values for month and year fields.
     */
    private fun setDefaults() {
        val calendar = Calendar.getInstance()
        binding.spinnerPieMonth.setSelection(calendar.get(Calendar.MONTH))
        binding.etPieYear.setText(calendar.get(Calendar.YEAR).toString())
    }

    /**
     * Loads and displays the pie chart for the selected month and year.
     */
    private fun loadPieChart() {
        val month = binding.spinnerPieMonth.selectedItemPosition + 1
        val year = binding.etPieYear.text.toString().toIntOrNull() ?: return
        lifecycleScope.launch {
            val categoryTotals = viewModel.getCategoryTotalsForMonth(month, year)
            val entries = categoryTotals.map { PieEntry(it.total.toFloat(), it.category) }
            binding.noData.isVisible = entries.isEmpty()
            binding.pieChart.isVisible = entries.isNotEmpty()
            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "").apply {
                    setColors(*com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS)
                }
                val data = PieData(dataSet)
                binding.pieChart.data = data
                binding.pieChart.invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 