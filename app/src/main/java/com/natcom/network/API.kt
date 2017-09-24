package com.natcom.network

import com.natcom.model.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface API {
    @GET("/lead/list/{type}")
    fun list(@Path("type") type: String, @Query("param") param: String?): Call<List<Lead>>

    @POST("/lead/assign/{id}")
    fun assign(@Path("id") id: Int): Call<Void>

    @Multipart
    @POST("/lead/upload/{id}")
    fun upload(@Path("id") id: Int, @Part file: MultipartBody.Part): Call<Picture>

    @POST("/lead/close/{id}")
    fun close(@Path("id") id: Int, @Body request: CloseRequest): Call<Void>

    @POST("/lead/shift/{id}")
    fun shift(@Path("id") id: Int, @Body request: ShiftRequest): Call<Void>

    @POST("/lead/deny/{id}")
    fun deny(@Path("id") id: Int, @Body request: DenyRequest): Call<Void>
}