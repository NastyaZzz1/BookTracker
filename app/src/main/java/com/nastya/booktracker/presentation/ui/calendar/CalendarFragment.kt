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
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kizitonwose.calendar.view.CalendarView
import com.nastya.booktracker.GoalPreferencesRepository
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
        val goalRepo = GoalPreferencesRepository(requireContext().applicationContext)

        val viewModelFactory =
            CalendarViewModelFactory(dailyReadingDao, goalRepo)
        viewModel = ViewModelProvider(this, viewModelFactory)[CalendarViewModel::class.java]

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

        observeEvents()
        setupGoalListener()
        setupGoalsObserved()
        setupWeekCalendar(currentMonth, startMonth, endMonth, daysOfWeek)
    }

    private fun setupGoalListener() {
        binding.itemDayGoal.setOnLongClickListener {
            viewModel.onChangeGoalClicked(GoalType.DAY)
            true
        }

        binding.itemMonthGoal.setOnLongClickListener {
            viewModel.onChangeGoalClicked(GoalType.MONTH)
            true
        }

        binding.itemYearGoal.setOnLongClickListener {
            viewModel.onChangeGoalClicked(GoalType.YEAR)
            true
        }
    }

    private fun setupGoalsObserved() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dailyGoal.collect {
                        binding.dayGoal.text = formatMinutes(it)
                        binding.calendarView.notifyMonthChanged(
                            binding.calendarView.findFirstVisibleMonth()?.yearMonth
                                ?: return@collect
                        )
                    }
                }
                launch {
                    viewModel.monthlyGoal.collect {
                        binding.monthGoal.text = formatMinutes(it)
                    }
                }
                launch {
                    viewModel.yearlyGoal.collect {
                        binding.yearGoal.text = formatMinutes(it)
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is UiEvent.ShowGoalDialog -> showGoalDialog(event.goalType)
                        is UiEvent.ShowDayDetailDialog -> showDayDetailDialog(event.date)
                    }
                }
            }
        }
    }

    private fun setupWeekCalendar(
        currentMonth: YearMonth,
        startMonth: YearMonth,
        endMonth: YearMonth,
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
                        viewModel.onDayClicked(day.date)

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

                lifecycleScope.launch {
                    val progress = viewModel.getDailyProgress(data.date)
                    container.progressView.progress = progress.toInt()
                }

                if (data.position == DayPosition.MonthDate) {
                    when {
                        selectedDate == data.date -> {
                            container.textView.setTextColor(Color.BLACK)
                            container.vBG.setBackgroundResource(R.drawable.selected_bg)
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

    private fun showGoalDialog(type: GoalType) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_goal, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Изменить цель")
            .setView(dialogView)
            .setPositiveButton("Окей", null)
            .create()

        val label = dialogView.findViewById<TextView>(R.id.goal_label)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        timePicker.setIs24HourView(true)

        val (currentFlow, onChange) = when (type) {
            GoalType.DAY -> viewModel.dailyGoal to viewModel::onDayGoalChanged
            GoalType.MONTH -> viewModel.monthlyGoal to viewModel::onMonthGoalChanged
            GoalType.YEAR -> viewModel.yearlyGoal to viewModel::onYearGoalChanged
        }

        label.text = when (type) {
            GoalType.DAY -> "На день"
            GoalType.MONTH -> "На месяц"
            GoalType.YEAR -> "На год"
        }

        val value = currentFlow.value
        timePicker.hour = value / 60
        timePicker.minute = value % 60

        timePicker.setOnTimeChangedListener { _, h, m ->
            onChange(h * 60 + m)
        }

        dialog.show()
    }

    private fun showDayDetailDialog(date: LocalDate) {
        viewLifecycleOwner.lifecycleScope.launch {
            val books = viewModel.getBooksForDate(date)
            val time = viewModel.getReadingTimeForDate(date)

            val message = books.joinToString("\n") {
                "${it.bookTitle}: ${viewModel.formatTimeMinutes(it.readingTime)}"
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(viewModel.formatLocalDate(date))
                .setMessage("Время чтения: ${viewModel.formatTimeMinutes(time)}\n$message")
                .setNegativeButton("Окей", null)
                .show()
        }
    }

    private fun formatMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours == 0 -> "$minutes мин"
            minutes == 0 -> "$hours ч"
            else -> "$hours ч $minutes мин"
        }
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