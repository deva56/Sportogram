/** App network repository for getting list of feed posts. Singleton instance. */

package com.example.sportogram.Repository

import com.example.sportogram.Models.Post
import com.example.sportogram.Network.RetrofitInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(private val retrofitInterface: RetrofitInterface) {

    suspend fun getFeed(url: String): Response<List<Post>> {
        return withContext(Dispatchers.IO) {
            retrofitInterface.getFeed(url)
        }
    }
}