package com.renato.tvz_raspored.data.networking

import com.renato.tvz_raspored.data.model.CourseInfo
import com.renato.tvz_raspored.data.model.Department
import com.renato.tvz_raspored.data.model.Semester
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleService {

    @GET("CalendarJson/Departments")
    suspend fun getDepartments(): Response<ArrayList<Department>>

    @GET("CalendarJson/Semesters")
    suspend fun getSemesters(
        @Query("department") department: String,
        @Query("year") year: String
    ): Response<ArrayList<Semester>>

    @GET("CalendarJson")
    suspend fun getSchedule(
        @Query("semester") semester: String,
        @Query("department") department: String,
        @Query("year") year: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): Response<ArrayList<CourseInfo>>

}