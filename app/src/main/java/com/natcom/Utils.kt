package com.natcom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


val LIST_TYPE_KEY = "LIST_TYPE_KEY"
val LEAD_KEY = "LEAD_KEY"
val LIST_KEY = "LIST_KEY"
val PARAM_KEY = "PARAM_KEY"
val LOGIN_KEY = "LOGIN_KEY"
val POSITION_KEY = "POSITION_KEY"
val PASSWORD_KEY = "PASSWORD_KEY"
val FRAGMENT_TAG = "FRAGMENT_TAG"

val gson = Gson()

val UPDATE_LIST = "UPDATE_LIST"
val REQUEST_CODE = 214

fun auth(context: Context) = PreferenceManager.getDefaultSharedPreferences(context).contains(LOGIN_KEY)

fun reset() {
    PreferenceManager
            .getDefaultSharedPreferences(MyApp.instance)
            .edit()
            .remove(LOGIN_KEY)
            .remove(PASSWORD_KEY)
            .apply()
}

private val MONTH_NAMES = arrayOf(
        "января",
        "февраля",
        "марта",
        "апреля",
        "мая",
        "июня",
        "июля",
        "августа",
        "сентября",
        "октября",
        "ноября",
        "декабря")

fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    return "${calendar.get(Calendar.DAY_OF_MONTH)} ${MONTH_NAMES[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}"
}

fun prepareDate(year: Int, month: Int, day: Int): String {
    val c = Calendar.getInstance()
    c.set(year, month, day, 0, 0)
    return SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.getDefault()).format(c.time)
}

fun Fragment.toast(@StringRes text: Int) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(@StringRes text: Int) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

val MAX_SIZE = 1280

fun compressImage(uri: Uri) = async(UI) {
    val bitmap = BitmapFactory.decodeFile(uri.path)
    val widthCoef = bitmap.width.toDouble() / MAX_SIZE
    val heightCoef = bitmap.height.toDouble() / MAX_SIZE
    val scale = maxOf(widthCoef, heightCoef)
    val result = Bitmap.createScaledBitmap(bitmap,
            (bitmap.width / scale).toInt(), (bitmap.height / scale).toInt(), true)
    val os = BufferedOutputStream(FileOutputStream(uri.path))
    run(CommonPool) {
        result.compress(Bitmap.CompressFormat.JPEG, 90, os)
    }
    return@async
}


suspend fun <T : Any?> Call<T>.awaitResponse(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                if (response.isSuccessful) {
                    continuation.resume(Result(throwable = FailedResponseException("code=${response.code()} body=${response.body()}")))
                } else {
                    continuation.resume(Result(response))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                continuation.resume(Result(throwable = t))
            }
        })
    }
}

class Result<T>(response: Response<T>? = null, val throwable: Throwable? = null) {
    private var value: T? = response?.body()

    fun isSuccessful() = value != null

    fun value(): T = value!!
}

class JobHolder {
    private val jobs = ArrayList<Job>()

    fun add(job: Job) {
        jobs.add(job)
        job.invokeOnCompletion { jobs.remove(job) }
    }

    fun dispose() {
        for (job in jobs) {
            job.cancel()
        }
        jobs.clear()
    }
}

class FailedResponseException(message: String) : Exception(message)