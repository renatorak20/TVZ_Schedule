package com.renato.tvz_raspored

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.renato.tvz_raspored.data.model.CourseInfo
import com.renato.tvz_raspored.data.model.Department
import com.renato.tvz_raspored.data.recyclerview.CourseInfoAdapter
import com.renato.tvz_raspored.databinding.ActivityMainBinding
import com.renato.tvz_raspored.viewmodel.TestVM
import java.lang.ClassCastException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TestVM
    private var isSearchReady = false
    private lateinit var recyclerAdapter: CourseInfoAdapter

    private lateinit var departmentCode: String
    private lateinit var semesterNumber: String
    private val calendar = Calendar.getInstance()

    private var isPreviouslyPressedBack = false
    private var isPreviouslyPressedFront = false
    private var firstClick = true

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[TestVM::class.java]

        viewModel.getAvailableDepartments()

        viewModel.getDepartments().observe(this) {
            isSearchReady = true
        }

        binding.autoComplete.threshold = 2
        binding.autoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! >= 2) {
                    val adapter = ArrayAdapter(applicationContext, R.layout.autocomplete_item, viewModel.getDepartments().value?.filter { it.Name.contains(s.toString()) } as ArrayList<String>)
                    binding.autoComplete.setAdapter(adapter)
                } else if (s.isEmpty()) {
                    binding.autoComplete.setAdapter(null)
                }
            }
        })

        binding.autoComplete.setOnItemClickListener { adapterView, view, position, l ->
            viewModel.getAvailableSemesters((binding.autoComplete.adapter.getItem(position) as Department).Code)
            binding.autoComplete.text.clear()
        }

        viewModel.getSemesters().observe(this) { semesters ->
            for (semester in semesters) {
                var chip = Chip(this)
                chip.text = this.getString(R.string.semester_chip, semester.SemesterNumber, semester.Department)
                chip.setOnClickListener {
                    binding.progressCircular.show()
                    semesterNumber = (it as Chip).text.toString()[0].toString()
                    val regex = Regex("\\(([^)]+)\\)")
                    val matchResult = regex.find(it.text.toString())
                    departmentCode = matchResult!!.groupValues[1]
                    enableCalendar()
                    val edgesOfWeek = getFirstAndLastDayOfCurrentWeek()
                    viewModel.getAvailableCourseInfo(semesterNumber, departmentCode, edgesOfWeek.first, edgesOfWeek.second)
                }
                binding.chipGroup.addView(chip)
            }
        }

        viewModel.getCourseInfos().observe(this) { courses ->
            try {
                val filtered = courses.filter { it.start.contains(getTodaysDayAndMonth()) }.sortedBy { it.start }
                recyclerAdapter = CourseInfoAdapter(ArrayList(filtered), this)
                binding.calendar.recyclerView.adapter = recyclerAdapter
                binding.calendar.recyclerView.layoutManager = LinearLayoutManager(this)
            } catch (e: ClassCastException) {
                Log.e("ERROR", e.toString())
            }
            binding.progressCircular.hide()
        }

        viewModel.getCurrentMonth().observe(this) {
            binding.calendar.monthTitle.text = getCurrentMonthString()
        }

        binding.calendar.back.setOnClickListener {
            if(firstClick) {
                isPreviouslyPressedFront = false
                isPreviouslyPressedBack = true
                previousWeek()
                firstClick = false
            } else if(isPreviouslyPressedFront) {
                isPreviouslyPressedFront = false
                isPreviouslyPressedBack = true
                previousWeek()
            }
            previousWeek()
            val edgesOfWeek = getFirstAndLastDayOfCurrentWeek()
            viewModel.getAvailableCourseInfo(semesterNumber, departmentCode, edgesOfWeek.first, edgesOfWeek.second)
        }

        binding.calendar.forward.setOnClickListener {
            if(firstClick) {
                isPreviouslyPressedFront = true
                isPreviouslyPressedBack = false
                nextWeek()
                firstClick = false
            } else if(isPreviouslyPressedBack) {
                isPreviouslyPressedFront = true
                isPreviouslyPressedBack = false
                nextWeek()
            }
            nextWeek()
            val edgesOfWeek = getFirstAndLastDayOfCurrentWeek()
            viewModel.getAvailableCourseInfo(semesterNumber, departmentCode, edgesOfWeek.first, edgesOfWeek.second)
        }
    }

    private fun enableCalendar() {
        with(binding.calendar) {
            back.visibility = View.VISIBLE
            forward.visibility = View.VISIBLE
            linearLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
        }
        initializeCalendar()
    }

    private fun disableCalendar() {
        with(binding.calendar) {
            back.visibility = View.INVISIBLE
            forward.visibility = View.INVISIBLE
            linearLayout.visibility = View.INVISIBLE
            recyclerView.visibility = View.INVISIBLE
        }
    }

    private fun initializeCalendar() {
        viewModel.setCurrentMonth(calendar.get(Calendar.MONTH) + 1)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        viewModel.setCurrentDaysOfWeek(getDaysOfCurrentWeek())

        val daysAWeek = mutableListOf<String>()
        for(day in resources.getStringArray(R.array.days_in_week)) {
            daysAWeek.add(day)
        }
        for(dayAWeek in 0 until binding.calendar.linearLayout.childCount) {
            ((binding.calendar.linearLayout.getChildAt(dayAWeek) as LinearLayout).getChildAt(0) as TextView).text = daysAWeek[dayAWeek]
            ((binding.calendar.linearLayout.getChildAt(dayAWeek) as LinearLayout).getChildAt(1) as TextView).text = longToShort(viewModel.getCurrentDaysOfWeek().value!![dayAWeek])

            (binding.calendar.linearLayout.getChildAt(dayAWeek) as LinearLayout).setOnClickListener {
                changeCurrentDay(dayAWeek)
            }
        }
    }

    private fun changeCurrentDay(dateIndex: Int) {
        try {
            disableCalendar()
            binding.progressCircular.visibility = View.VISIBLE
            binding.progressCircular.show()
            binding.calendar.recyclerView.adapter = CourseInfoAdapter(ArrayList(viewModel.getCourseInfos().value?.filter { it.start.contains(normalToInvertedShort(viewModel.getCurrentDaysOfWeek().value!![dateIndex])) }?.sortedBy { it.start }!!), this)
            binding.progressCircular.hide()
            enableCalendar()
        }catch (e: ClassCastException) {}
    }

    private fun showDaysOfWeek() {
        for(dayAWeek in 0 until binding.calendar.linearLayout.childCount) {
            ((binding.calendar.linearLayout.getChildAt(dayAWeek) as LinearLayout).getChildAt(1) as TextView).text = longToShort(viewModel.getCurrentDaysOfWeek().value!![dayAWeek])
        }
    }
    fun getCurrentMonthString() = resources.getStringArray(R.array.months)[viewModel.getCurrentMonth().value!!]

    private fun getDaysOfCurrentWeek(): ArrayList<String> {
        val format: DateFormat = SimpleDateFormat("dd.MM.yyyy")
        val days = arrayListOf<String>()
        for (i in 0..6) {
            days.add(format.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        return days
    }

    private fun nextWeek() {
        val format: DateFormat = SimpleDateFormat("dd.MM.yyyy")
        val days = arrayListOf<String>()
        for (i in 0..6) {
            days.add(format.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        viewModel.setCurrentDaysOfWeek(days)
        showDaysOfWeek()
    }
    fun previousWeek() {
        val format: DateFormat = SimpleDateFormat("dd.MM.yyyy")
        val days = arrayListOf<String>()
        for (i in 0..6) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            days.add(format.format(calendar.time))
        }
        days.reverse()
        viewModel.setCurrentDaysOfWeek(days)
        showDaysOfWeek()
    }

    private fun getFirstAndLastDayOfCurrentWeek(): Pair<String, String> {
        return normalToInverted(viewModel.getCurrentDaysOfWeek().value!![0]) to normalToInverted(viewModel.getCurrentDaysOfWeek().value!![viewModel.getCurrentDaysOfWeek().value?.size!! - 1])
    }

    private fun getTodaysDayAndMonth(): String {
        val dateFormat = SimpleDateFormat("MM-dd")
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    private fun longToShort(date: String): String {
        val inputDateFormat = SimpleDateFormat("dd.MM.yyyy")
        val outputDateFormat = SimpleDateFormat("dd.MM.")
        val date = inputDateFormat.parse(date)
        return outputDateFormat.format(date)
    }

    private fun normalToInverted(date: String): String {
        val inputDateFormat = SimpleDateFormat("dd.MM.yyyy")
        val outputDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = inputDateFormat.parse(date)
        return outputDateFormat.format(date)
    }

    private fun normalToInvertedShort(date: String): String {
        val inputDateFormat = SimpleDateFormat("dd.MM.yyyy")
        val outputDateFormat = SimpleDateFormat("MM-dd")
        val date = inputDateFormat.parse(date)
        return outputDateFormat.format(date)
    }

}