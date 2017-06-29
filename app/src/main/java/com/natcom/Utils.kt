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
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


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

private val MONTH_NAMES = arrayOf("января",
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

fun compressImage(uri: Uri, f: (File) -> Unit) {
    Thread {
        val bitmap = BitmapFactory.decodeFile(uri.path)
        val widthCoef = bitmap.width.toDouble() / MAX_SIZE
        val heightCoef = bitmap.height.toDouble() / MAX_SIZE
        val scale = maxOf(widthCoef, heightCoef)
        val result = Bitmap.createScaledBitmap(bitmap,
                (bitmap.width / scale).toInt(), (bitmap.height / scale).toInt(), true)
        val os = BufferedOutputStream(FileOutputStream(uri.path))
        result.compress(Bitmap.CompressFormat.JPEG, 90, os)
        f(File(uri.path))
    }.start()
}