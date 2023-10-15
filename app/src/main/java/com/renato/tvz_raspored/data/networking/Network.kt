package com.renato.tvz_raspored.data.networking

import com.google.gson.GsonBuilder
import com.renato.tvz_raspored.data.model.CourseInfo
import com.renato.tvz_raspored.utils.ScheduleItemDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Network {

    private val service: ScheduleService
    private val baseURL = "https://homer.tvz.hr/"

    fun getService(): ScheduleService = service

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val gson = GsonBuilder()
            .registerTypeAdapter(CourseInfo::class.java, ScheduleItemDeserializer())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()

        service = retrofit.create(ScheduleService::class.java)
    }
}