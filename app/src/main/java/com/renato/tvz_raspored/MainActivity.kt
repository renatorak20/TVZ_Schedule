package com.renato.tvz_raspored

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.renato.tvz_raspored.data.model.Department
import com.renato.tvz_raspored.data.recyclerview.CourseInfoAdapter
import com.renato.tvz_raspored.databinding.ActivityMainBinding
import com.renato.tvz_raspored.viewmodel.TestVM
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TestVM
    private var isSearchReady = false
    private lateinit var recyclerAdapter: CourseInfoAdapter

    private lateinit var departmentCode: String
    private lateinit var semesterNumber: String
    private val calendar = Calendar.getInstance()

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
                    viewModel.getAvailableCourseInfo(semesterNumber, departmentCode)
                }
                binding.chipGroup.addView(chip)
            }
        }

        viewModel.getCourseInfos().observe(this) {
            recyclerAdapter = CourseInfoAdapter(it)
            binding.calendar.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.calendar.recyclerView.adapter = recyclerAdapter
            binding.progressCircular.hide()
            enableCalendar()
        }

        viewModel.getCurrentMonth().observe(this) {
            binding.calendar.monthTitle.text = getCurrentMonthString()
        }

        binding.calendar.back.setOnClickListener {
            previousWeek()
        }

        binding.calendar.forward.setOnClickListener {
            nextWeek()
        }
    }

    private fun enableCalendar() {
        with(binding.calendar) {
            monthTitle.visibility = View.VISIBLE
            back.visibility = View.VISIBLE
            forward.visibility = View.VISIBLE
            linearLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
        }
        initializeCalendar()
    }

    private fun initializeCalendar() {
        viewModel.setCurrentMonth(calendar.get(Calendar.MONTH) + 1)
        viewModel.setCurrentDaysOfWeek(getDaysOfCurrentWeek())

        val daysAWeek = mutableListOf<String>()
        for(day in resources.getStringArray(R.array.days_in_week)) {
            daysAWeek.add(day)
        }
        for(dayAWeek in 0 until binding.calendar.linearLayout.childCount) {
            ((binding.calendar.linearLayout.getChildAt(dayAWeek) as LinearLayout).getChildAt(0) as TextView).text = daysAWeek[dayAWeek]
            ((binding.calendar.linearLayout.getChildAt(dayAWeek) as LinearLayout).getChildAt(1) as TextView).text = viewModel.getCurrentDaysOfWeek().value!![dayAWeek]
        }
    }

    private fun showDaysOfWeek() {
        for(dayAWeek in 0 until binding.calendar.linearLayout.childCount) {
            ((binding.calendar.linearLayout.getChildAt(dayAWeek) as LinearLayout).getChildAt(1) as TextView).text = viewModel.getCurrentDaysOfWeek().value!![dayAWeek]
        }
    }
    fun getCurrentMonthString() = resources.getStringArray(R.array.months)[viewModel.getCurrentMonth().value!!]

    fun getDaysOfCurrentWeek(): ArrayList<String> {
        val format: DateFormat = SimpleDateFormat("dd")
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY

        val days = arrayListOf<String>()
        for (i in 0..6) {
            days.add(format.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    fun nextWeek() {
        val format: DateFormat = SimpleDateFormat("dd")
        val days = arrayListOf<String>()
        for (i in 0..6) {
            days.add(format.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        viewModel.setCurrentDaysOfWeek(days)
        showDaysOfWeek()
    }
    fun previousWeek() {
        val format: DateFormat = SimpleDateFormat("dd")
        val days = arrayListOf<String>()
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        for (i in 0..6) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            days.add(format.format(calendar.time))
        }
        days.reverse()
        viewModel.setCurrentDaysOfWeek(days)
        showDaysOfWeek()
    }

}