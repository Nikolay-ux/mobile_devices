package com.example.myapplication1.api

import com.example.myapplication1.model.CbrResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CbrApiService {
    @GET("scripts/xml_metall.asp")
    fun getMetalRates(
        @Query("date_req1") dateReq1: String,
        @Query("date_req2") dateReq2: String
    ): Response<CbrResponse>
}