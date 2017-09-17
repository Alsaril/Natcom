package com.natcom.network

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.natcom.LOGIN_KEY
import com.natcom.MyApp
import com.natcom.PASSWORD_KEY
import com.natcom.activity.ListType
import com.natcom.model.Lead
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


object NetworkController {

    //val BASE_URL = "http://192.168.1.48:5000/"
    val BASE_URL = "http://188.225.77.144/"

    private val retrofit: Retrofit by lazy { init(MyApp.instance) }
    val api by lazy { retrofit.create(API::class.java) }

    private fun init(context: Context): Retrofit {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val okHttpClient = OkHttpClient.Builder().addInterceptor {
            val request = it.request()
            val authenticatedRequest = request.newBuilder()
                    .header("Authorization",
                            Credentials.basic(sp.getString(LOGIN_KEY, ""), sp.getString(PASSWORD_KEY, ""))).build()
            it.proceed(authenticatedRequest)
        }.build()

        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
    }

    var listCallback: ListResult? = null
        get
        set

    var listPerforming = false
        get
        private set

    var listCall: Call<List<Lead>>? = null

    fun list(type: ListType, param: String? = null, reset: Boolean = false) {
        if (reset) listCall?.cancel()
        else if (listPerforming) return
        listPerforming = true
        listCall = api.list(type.name.toLowerCase(), param)
        listCall?.enqueue(object : Callback<List<Lead>> {
            override fun onFailure(call: Call<List<Lead>>, t: Throwable) {
                if (call.isCanceled) return
                listPerforming = false
                listCallback?.onListResult(type, false)
            }

            override fun onResponse(call: Call<List<Lead>>, response: Response<List<Lead>>) {
                listPerforming = false
                if (response.code() == 200) {
                    listCallback?.onListResult(type, true, response.body())
                } else {
                    listCallback?.onListResult(type, false)
                }
            }
        })
    }

    fun picture(id: Int, uri: Uri) = api.upload(id,
            MultipartBody.Part.createFormData("file", "file", RequestBody.create(MediaType.parse("image/*"), File(uri.path))))
}

interface ListResult {
    fun onListResult(type: ListType, success: Boolean, list: List<Lead>? = null)
}
