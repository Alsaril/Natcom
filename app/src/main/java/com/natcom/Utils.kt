package com.natcom

import android.content.Context
import android.preference.PreferenceManager

val ID_KEY = "IF_KEY"
val LIST_TYPE_KEY = "LIST_TYPE_KEY"
val LEAD_KEY = "LEAD_KEY"
val PARAM_KEY = "PARAM_KEY"
val LOGIN_KEY = "LOGIN_KEY"
val PASSWORD_KEY = "PASSWORD_KEY"
val FRAGMENT_TAG = "FRAGMENT_TAG"

fun auth(context: Context): Boolean {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    return sp.contains(LOGIN_KEY)
}

fun reset() {
    val context = MyApp.instance
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    editor.remove(LOGIN_KEY)
    editor.remove(PASSWORD_KEY)
    editor.apply()
}
