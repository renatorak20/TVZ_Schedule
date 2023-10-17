package com.renato.tvz_raspored.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renato.tvz_raspored.data.model.CourseInfo
import com.renato.tvz_raspored.data.model.Department
import com.renato.tvz_raspored.data.model.Semester
import com.renato.tvz_raspored.data.networking.Network
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.Calendar

class TestVM: ViewModel() {

    private val _departments = MutableLiveData<ArrayList<Department>>()
    fun getDepartments() = _departments

    private fun setDepartments(departments: ArrayList<Department>) {
        _departments.value = departments
    }

    fun getAvailableDepartments() {
        viewModelScope.launch {
            val departmentsResponse = Network().getService().getDepartments()

            if(departmentsResponse.isSuccessful) {
                departmentsResponse.body()?.let { setDepartments(it) }
            }
        }
    }

    private val _semesters = MutableLiveData<ArrayList<Semester>>()

    fun getSemesters() = _semesters
    private fun setSemesters(semesters: ArrayList<Semester>) {
        _semesters.value = semesters
    }

    fun getAvailableSemesters(department: String) {
        viewModelScope.launch {
            val semestersResponse = Network().getService().getSemesters(department, Calendar.getInstance().get(Calendar.YEAR).toString())

            if(semestersResponse.isSuccessful) {
                semestersResponse.body()?.let { setSemesters(it) }
            }
        }
    }

    private val _courseInfos = MutableLiveData<ArrayList<CourseInfo>>()
    fun getCourseInfos() = _courseInfos

    private fun setCourseInfos(coursesInfo: ArrayList<CourseInfo>) {
        _courseInfos.value = coursesInfo
    }

    fun getAvailableCourseInfo(semester: String, department: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            val courseInfoResponse = Network().getService().getSchedule(semester, department, Calendar.getInstance().get(Calendar.YEAR).toString(), startDate, endDate)

            if(courseInfoResponse.isSuccessful) {
                courseInfoResponse.body()?.let { setCourseInfos(it) }
            }
        }
    }

    private val _currentMonth = MutableLiveData<Int>()
    fun getCurrentMonth() = _currentMonth

    fun setCurrentMonth(month: Int) {
        _currentMonth.value = month
    }

    private val _currentDaysOfWeek = MutableLiveData<ArrayList<String>>()
    fun getCurrentDaysOfWeek() = _currentDaysOfWeek

    fun setCurrentDaysOfWeek(daysOfWeek: ArrayList<String>) {
        _currentDaysOfWeek.value = daysOfWeek
    }

}