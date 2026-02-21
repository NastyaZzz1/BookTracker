package com.nastya.booktracker.presentation.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.view.CalendarView
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.CalendarDayLayoutBinding
import com.nastya.booktracker.databinding.FragmentCalendarBinding
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Month
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CalendarViewModel
    private val monthCalendarView: CalendarView get() = binding.calendarView
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireNotNull(this.activity).application
        val dailyReadingDao = BookDatabase.getInstance(application).dailyReadingDao

        val viewModelFactory =
            CalendarViewModelFactory(dailyReadingDao, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[CalendarViewModel::class.java]

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

        viewModel.dailyGoal.observe(this.viewLifecycleOwner, Observer {
            it?.let {
                binding.dayGoal.text = it.toString()
                setupWeekCalendar(startMonth, endMonth, currentMonth, daysOfWeek)
            }
        })
        viewModel.monthlyGoal.observe(this.viewLifecycleOwner, Observer {
            it?.let {
                binding.monthGoal.text = it.toString()
                setupWeekCalendar(startMonth, endMonth, currentMonth, daysOfWeek)
            }
        })
        viewModel.yearlyGoal.observe(this.viewLifecycleOwner, Observer {
            it?.let {
                binding.yearGoal.text = it.toString()
                setupWeekCalendar(startMonth, endMonth, currentMonth, daysOfWeek)
            }
        })

        binding.changeGoalBtn.setOnClickListener {
            viewModel.showInfDialogChangeGoal(requireContext())
        }

        setupWeekCalendar(startMonth, endMonth, currentMonth, daysOfWeek)
    }

    private fun setupWeekCalendar(
        startMonth: YearMonth,
        endMonth: YearMonth,
        currentMonth: YearMonth,
        daysOfWeek: List<DayOfWeek>
    ) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
            val progressView = CalendarDayLayoutBinding.bind(view).circularProgress
            val vBG = CalendarDayLayoutBinding.bind(view).vBG
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        val currentSelection = selectedDate
                        if (currentSelection == day.date) {
                            selectedDate = null
                            binding.calendarView.notifyDateChanged(currentSelection)
                        }
                        else {
                            selectedDate = day.date
                            binding.calendarView.notifyDateChanged(day.date)
                            if (currentSelection != null) {
                                binding.calendarView.notifyDateChanged(currentSelection)
                            }
                        }
                    }
                }
            }
        }
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                viewModel.viewModelScope.launch {
                    container.progressView.progress = viewModel.dailyProgressGet(data.date).toInt()
                }

                if (data.position == DayPosition.MonthDate) {
                    when {
                        selectedDate == data.date -> {
                            container.textView.setTextColor(Color.BLACK)
                            container.vBG.setBackgroundResource(R.drawable.selected_bg)
                            viewModel.viewModelScope.launch {
                                viewModel.showInfDialogDetailData(requireContext(), data.date)
                            }
                        }

                        today == data.date -> {
                            container.textView.setTextColor(Color.WHITE)
                            container.vBG.setBackgroundResource(R.drawable.today_bg)
                        }

                        else -> {
                            container.textView.setTextColor(Color.BLACK)
                            container.vBG.setBackgroundResource(R.drawable.not_selected_bg)
                        }
                    }
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }
            }
        }
        binding.titlesContainer.root.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }

        binding.calendarView.monthScrollListener = { updateTitle() }
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)
    }

    private fun updateTitle() {
        val month = monthCalendarView.findFirstVisibleMonth()?.yearMonth ?: return
        binding.oneYearText.text = month.year.toString()
        binding.oneMonthText.text = month.month.displayText(short = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.ENGLISH)
}