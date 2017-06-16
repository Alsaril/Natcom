package com.natcom.network

import com.natcom.model.CloseRequest
import com.natcom.model.DenyRequest
import com.natcom.model.Lead
import com.natcom.model.ShiftRequest
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface API {
    @GET("/lead/list/{type}")
    fun list(@Path("type") type: String, @Query("param") param: String?): Call<List<Lead>>

    @POST("/lead/assign/{id}")
    fun assign(@Path("id") id: Int): Call<Void>

    @Multipart
    @POST("/lead/upload/{id}")
    fun upload(@Path("id") id: Int, @Part file: RequestBody): Call<Void>

    @POST("/lead/close/{id}")
    fun close(@Path("id") id: Int, @Body request: CloseRequest): Call<Void>

    @POST("/lead/shift/{id}")
    fun shift(@Path("id") id: Int, @Body request: ShiftRequest): Call<Void>

    @POST("/lead/deny/{id}")
    fun deny(@Path("id") id: Int, @Body request: DenyRequest): Call<Void>
}