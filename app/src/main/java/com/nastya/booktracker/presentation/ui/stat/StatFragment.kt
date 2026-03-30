package com.nastya.booktracker.presentation.ui.stat

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentStatBinding
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class StatFragment : Fragment() {
    private var _binding: FragmentStatBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupChart()

        viewModel.loadYearStat(LocalDate.now().year)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.monthStats.collect { entries ->
                    updateChart(entries)
                }
            }
        }
    }

    private fun initViewModel() {
        val application = requireNotNull(this.activity).application
        val dailyReadingDao = BookDatabase.getInstance(application).dailyReadingDao
        val viewModelFactory = StatViewModelFactory(dailyReadingDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[StatViewModel::class.java]
    }

    private fun setupChart() = with(binding.idBarChart) {
        description.isEnabled = false
        legend.isEnabled = false
        axisRight.isEnabled = false

        axisLeft.axisMinimum = 0f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 11f
        xAxis.setLabelCount(12, true)

        xAxis.valueFormatter = object : ValueFormatter() {
            private val months =
                arrayOf("Я", "Ф", "М", "А", "М", "И", "И", "А", "С", "О", "Н", "Д")

            override fun getFormattedValue(value: Float): String {
                return months.getOrNull(value.toInt()) ?: ""
            }
        }

        data = BarData(
            BarDataSet(emptyList(), "").apply {
                color = ContextCompat.getColor(
                    requireContext(),
                    R.color.light_brown
                )
            }
        ).apply {
            barWidth = 0.9f
        }

        setFitBars(true)
    }

    private fun updateChart(stats: List<StatViewModel.MonthStat>) {
        val entries = stats.map {
            BarEntry((it.month - 1).toFloat(), it.pages.toFloat())
        }

        val chart = binding.idBarChart
        val dataSet = binding.idBarChart.data.getDataSetByIndex(0) as BarDataSet
        dataSet.values = entries

        val maxValue = stats.maxOfOrNull { it.pages } ?: 0
        chart.axisLeft.axisMaximum = (maxValue * 1.2f).coerceAtLeast(1f)

        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}