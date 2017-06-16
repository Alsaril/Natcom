package com.natcom.network

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.natcom.CookieHelper
import com.natcom.CookieHelper.reset
import com.natcom.Keys
import com.natcom.MyApp
import com.natcom.fragment.ListType
import com.natcom.model.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object NetworkController {

    val BASE_URL = "http://188.225.77.144/"

    private val retrofit: Retrofit by lazy { init(MyApp.instance!!) }
    private val api by lazy { retrofit.create(API::class.java) }

    private fun init(context: Context): Retrofit {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val okHttpClient = OkHttpClient.Builder().cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val e = sp.edit()
                for (c in cookies) {
                    Keys.values()
                            .filter { c.name() == it.value() }
                            .forEach { CookieHelper.store(c, e) }
                }
                e.apply()
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> =
                    Keys.values()
                            .filter { sp.contains(it.value()) }
                            .map { CookieHelper.get(it, sp) }
        }).build()

        val gson = GsonBuilder().create()


        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    var loginCallback: LoginResult? = null
        get
        set

    fun login(login: String, password: String) {
        api.login(LoginRequest(login, password)).enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginCallback?.onLoginResult(false)
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.code() == 200) {
                    loginCallback?.onLoginResult(true, response.body().id)
                } else {
                    loginCallback?.onLoginResult(false)
                }
            }
        })
    }

    var listCallback: ListResult? = null
        get
        set

    fun list(type: ListType, param: String? = null) {
        api.list(type.name.toLowerCase(), param).enqueue(object : Callback<List<Lead>> {
            override fun onFailure(call: Call<List<Lead>>, t: Throwable) {
                listCallback?.onListResult(false)
            }

            override fun onResponse(call: Call<List<Lead>>, response: Response<List<Lead>>) {
                if (response.code() == 200) {
                    listCallback?.onListResult(true, response.body())
                } else {
                    listCallback?.onListResult(false)
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

    fun picture(id: Int, uri: Uri) {
        api.upload(id, RequestBody.create(MediaType.parse("image/*"), File(uri.path))).enqueue(object : Callback<Void> {
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

interface LoginResult {
    fun onLoginResult(success: Boolean, id: Int = 0)
}

interface ListResult {
    fun onListResult(success: Boolean, list: List<Lead>? = null)
}

interface PictureResult {
    fun onPictureResult(success: Boolean)
}

interface CloseResult {
    fun onCloseResult(success: Boolean)
}

interface DenyResult {
    fun onDenyResult(success: Boolean)
}

interface AssignResult {
    fun onAssignResult(success: Boolean)
}

