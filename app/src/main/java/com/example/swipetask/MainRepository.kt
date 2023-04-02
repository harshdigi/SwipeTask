package com.example.swipetask

import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MainRepository constructor(private val retrofitService: RetrofitService) {

    suspend fun getAllProduct() : NetworkState<MutableList<ProductModel>> {
        val response = retrofitService.getAllProducts()

        return if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                NetworkState.Success(responseBody)
            } else {
                NetworkState.Error(response)
            }
        } else {
            NetworkState.Error(response)
        }
    }



}