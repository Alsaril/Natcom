package com.natcom

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson

val LIST_TYPE_KEY = "LIST_TYPE_KEY"
val LEAD_KEY = "LEAD_KEY"
val LIST_KEY = "LIST_KEY"
val PARAM_KEY = "PARAM_KEY"
val LOGIN_KEY = "LOGIN_KEY"
val PASSWORD_KEY = "PASSWORD_KEY"
val FRAGMENT_TAG = "FRAGMENT_TAG"

val gson = Gson()

fun auth(context: Context) = PreferenceManager.getDefaultSharedPreferences(context).contains(LOGIN_KEY)

fun reset() {
    PreferenceManager
            .getDefaultSharedPreferences(MyApp.instance)
            .edit()
            .remove(LOGIN_KEY)
            .remove(PASSWORD_KEY)
            .apply()
}
