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

    fun setDepartments(departments: ArrayList<Department>) {
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

    fun setSemesters(semesters: ArrayList<Semester>) {
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

    fun setCourseInfos(coursesInfo: ArrayList<CourseInfo>) {
        _courseInfos.value = coursesInfo
    }

    fun getAvailableCourseInfo(semester: String, department: String) {
        viewModelScope.launch {
            val courseInfoResponse = Network().getService().getSchedule(semester, department, Calendar.getInstance().get(Calendar.YEAR).toString(), "2023-10-14", "2023-10-21")

            if(courseInfoResponse.isSuccessful) {
                courseInfoResponse.body()?.let { setCourseInfos(it) }
            }
        }
    }

}