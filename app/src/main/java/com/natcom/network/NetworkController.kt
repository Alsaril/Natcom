package com.natcom.network

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.natcom.LOGIN_KEY
import com.natcom.MyApp
import com.natcom.PASSWORD_KEY
import com.natcom.activity.ListType
import com.natcom.model.CloseRequest
import com.natcom.model.DenyRequest
import com.natcom.model.Lead
import com.natcom.model.ShiftRequest
import com.natcom.reset
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
    private val api by lazy { retrofit.create(API::class.java) }

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

    var listPerfoming = false
        get
        private set

    var listCall: Call<List<Lead>>? = null

    fun list(type: ListType, param: String? = null, reset: Boolean = false) {
        if (reset) listCall?.cancel()
        else if (listPerfoming) return
        listPerfoming = true
        listCall = api.list(type.name.toLowerCase(), param)
        listCall?.enqueue(object : Callback<List<Lead>> {
            override fun onFailure(call: Call<List<Lead>>, t: Throwable) {
                if (call.isCanceled) return
                listPerfoming = false
                listCallback?.onListResult(type, false)
            }

            override fun onResponse(call: Call<List<Lead>>, response: Response<List<Lead>>) {
                listPerfoming = false
                if (response.code() == 200) {
                    listCallback?.onListResult(type, true, response.body())
                } else {
                    listCallback?.onListResult(type, false)
                }
                if (response.code() == 401) {
                    reset()
                }
            }
        })
    }

    var pictureCallback: PictureResult? = null
        get
        set

    fun picture(id: Int, file: File) {
        api.upload(id, MultipartBody.Part.createFormData("file", "file", RequestBody.create(MediaType.parse("image/*"), file))).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                pictureCallback?.onPictureResult(false)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    pictureCallback?.onPictureResult(true)
                } else {
                    pictureCallback?.onPictureResult(false)
                }
                if (response.code() == 401) {
                    reset()
                }
            }
        })
    }

    var closeCallback: CloseResult? = null
        get
        set

    fun close(id: Int, contract: Boolean, mount: Boolean, comment: String, date: String) {
        api.close(id, CloseRequest(contract, mount, comment, date)).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                closeCallback?.onCloseResult(false)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    closeCallback?.onCloseResult(true)
                } else {
                    closeCallback?.onCloseResult(false)
                }
                if (response.code() == 401) {
                    reset()
                }
            }
        })
    }

    var shiftCallback: ShiftResult? = null
        get
        set

    fun shift(id: Int, date: String, comment: String) {
        api.shift(id, ShiftRequest(date, comment)).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                shiftCallback?.onShiftResult(false)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    shiftCallback?.onShiftResult(true)
                } else {
                    shiftCallback?.onShiftResult(false)
                }
                if (response.code() == 401) {
                    reset()
                }
            }
        })
    }

    var denyCallback: DenyResult? = null
        get
        set

    fun deny(id: Int, comment: String) {
        api.deny(id, DenyRequest(comment)).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                denyCallback?.onDenyResult(false)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    denyCallback?.onDenyResult(true)
                } else {
                    denyCallback?.onDenyResult(false)
                }
                if (response.code() == 401) {
                    reset()
                }
            }
        })
    }

    var assignCallback: AssignResult? = null
        get
        set

    fun assign(id: Int) {
        api.assign(id).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                assignCallback?.onAssignResult(false)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    assignCallback?.onAssignResult(true)
                } else {
                    assignCallback?.onAssignResult(false)
                }
                if (response.code() == 401) {
                    reset()
                }
            }
        })
    }
}

interface ListResult {
    fun onListResult(type: ListType, success: Boolean, list: List<Lead>? = null)
}

interface PictureResult {
    fun onPictureResult(success: Boolean)
}

interface CloseResult {
    fun onCloseResult(success: Boolean)
}

interface ShiftResult {
    fun onShiftResult(success: Boolean)
}

interface DenyResult {
    fun onDenyResult(success: Boolean)
}

interface AssignResult {
    fun onAssignResult(success: Boolean)
}

