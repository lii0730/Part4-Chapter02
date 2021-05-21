package com.example.aop_part4_chapter02.service

import retrofit2.Call
import retrofit2.http.GET

interface MusicService {
    @GET("/v3/1c9a620b-0029-4c9f-9f77-77a1284c0de0")
    fun loadMusics() : Call<MusicDTO>

}