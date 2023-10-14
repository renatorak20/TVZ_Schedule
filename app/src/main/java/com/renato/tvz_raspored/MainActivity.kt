package com.renato.tvz_raspored

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.renato.tvz_raspored.data.model.Department
import com.renato.tvz_raspored.data.recyclerview.CourseInfoAdapter
import com.renato.tvz_raspored.databinding.ActivityMainBinding
import com.renato.tvz_raspored.viewmodel.TestVM
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TestVM
    private var isSearchReady = false
    private lateinit var recyclerAdapter: CourseInfoAdapter

    private lateinit var departmentCode: String
    private lateinit var semesterNumber: String

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
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = recyclerAdapter
        }

    }
}