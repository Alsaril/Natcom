package com.natcom

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import okhttp3.Cookie

val ID_KEY = "IF_KEY"
val LIST_TYPE_KEY = "LIST_TYPE_KEY"
val LEAD_KEY = "LEAD_KEY"
val PARAM_KEY = "PARAM_KEY"

enum class Keys constructor(private val value: String) {
    KEY1("token"),
    KEY2("session_id");

    fun value(): String {
        return value
    }
}

object CookieHelper {

    internal fun auth(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return Keys.values()
                .filter { sp.contains(it.value()) }
                .count() > 0
    }

    internal fun reset() {
        val context = MyApp.instance
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        Keys.values().forEach { editor.remove(it.value()) }
        editor.remove(ID_KEY)
        editor.apply()
    }

    internal fun store(cookie: Cookie, e: SharedPreferences.Editor) {
        e.putString(cookie.name(), cookie.value())
        e.putLong(cookie.name() + "_expiresAt", cookie.expiresAt())
        e.putString(cookie.name() + "_domain", cookie.domain())
        e.putString(cookie.name() + "_path", cookie.path())
        e.putBoolean(cookie.name() + "_secure", cookie.secure())
        e.putBoolean(cookie.name() + "_httpOnly", cookie.httpOnly())
        e.putBoolean(cookie.name() + "_hostOnly", cookie.hostOnly())
    }

    internal operator fun get(keys: Keys, sp: SharedPreferences): Cookie {
        val name = keys.value()
        val cb = Cookie.Builder().name(name)
                .value(sp.getString(name, "")!!)
                .expiresAt(sp.getLong(name + "_expiresAt", 0))
                .domain(sp.getString(name + "_domain", "")!!)
                .path(sp.getString(name + "_path", "")!!)
        if (sp.getBoolean(name + "_secure", false)) {
            cb.secure()
        }
        if (sp.getBoolean(name + "_httpOnly", false)) {
            cb.httpOnly()
        }
        if (sp.getBoolean(name + "_hostOnly", false)) {
            cb.httpOnly()
        }
        return cb.build()
    }
}
