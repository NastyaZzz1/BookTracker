package com.nastya.booktracker.presentation.ui.stat

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.FragmentStatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.O)
class StatFragment : Fragment() {
    private var _binding: FragmentStatBinding? = null
    private val binding get() = _binding!!
    private lateinit var barData: BarData
    private lateinit var barDataSet: BarDataSet
    private lateinit var viewModel: StatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dailyReadingDao = BookDatabase.getInstance(application).dailyReadingDao

        val viewModelFactory = StatViewModelFactory(dailyReadingDao)
        val viewModel = ViewModelProvider(this, viewModelFactory)[StatViewModel::class.java]

        this.viewModel = viewModel

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getBarChartData()  //добавляем данным в массив

        viewModel.barChartData.observe(viewLifecycleOwner) { entries ->
            barDataSet = BarDataSet(entries, "Bar Chart Data") //инициализируем DataSet
            barData = BarData(barDataSet) //инициализируем Data
            binding.idBarChart.data = barData  //устанавливаем данные для диаграммы
            binding.idBarChart.description.isEnabled = false
            binding.idBarChart.legend.isEnabled = false
            binding.idBarChart.axisRight.isEnabled = false
            barDataSet.setColor(resources.getColor(R.color.light_brown))
            binding.idBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM


            val months = arrayOf("Я", "Ф", "М", "А", "М", "И", "И", "А", "С", "О", "Н", "Д")

            binding.idBarChart.xAxis.valueFormatter = object : ValueFormatter() {

                override fun getFormattedValue(value: Float): String {
                    return if (value < months.size) months[value.toInt()] else ""
                }
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return getFormattedValue(value)
                }
            }
            binding.idBarChart.xAxis.granularity = 1f
            binding.idBarChart.xAxis.setLabelCount(months.size, true)
            binding.yearStat.text = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())


            with(binding.idBarChart) {
                axisLeft.axisMinimum = 0f
                invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}