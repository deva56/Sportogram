package com.example.sportogram.Network

import com.example.sportogram.Models.Post
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitInterface {

    @GET
    suspend fun getFeed(@Url url: String): Response<List<Post>>
}