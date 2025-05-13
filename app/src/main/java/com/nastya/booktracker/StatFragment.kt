package com.nastya.booktracker

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
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
    private lateinit var barEntriesList: ArrayList<BarEntry>

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

        getBarChartData()  //добавляем данным в массив
        barDataSet = BarDataSet(barEntriesList, "Bar Chart Data") //инициализируем DataSet
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

    }

    private fun getBarChartData() {
        barEntriesList = ArrayList()

        barEntriesList.add(BarEntry(1f, 1f))
        barEntriesList.add(BarEntry(2f, 2f))
        barEntriesList.add(BarEntry(3f, 3f))
        barEntriesList.add(BarEntry(4f, 4f))
        barEntriesList.add(BarEntry(5f, 2f))
        barEntriesList.add(BarEntry(6f, 6f))
        barEntriesList.add(BarEntry(7f, 7f))
        barEntriesList.add(BarEntry(8f, 5f))
        barEntriesList.add(BarEntry(9f, 9f))
        barEntriesList.add(BarEntry(10f, 7f))
        barEntriesList.add(BarEntry(11f, 11f))
        barEntriesList.add(BarEntry(12f, 8f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}