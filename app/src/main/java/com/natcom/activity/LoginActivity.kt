package com.natcom.activity

import android.content.Intent
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.natcom.LOGIN_KEY
import com.natcom.PASSWORD_KEY
import com.natcom.R
import kotterknife.bindView

class LoginActivity : AppCompatActivity() {
    val login by bindView<TextView>(R.id.login)
    val password by bindView<TextView>(R.id.password)
    val confirm by bindView<Button>(R.id.confirm)
    val progress by bindView<ProgressBar>(R.id.progress)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        confirm.setOnClickListener { confirm() }
    }

    fun confirm() {
        if (login.text.isEmpty() || password.text.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            return
        }
        confirm.isEnabled = false
        progress.visibility = View.VISIBLE

        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(LOGIN_KEY, login.text.toString())
        editor.putString(PASSWORD_KEY, password.text.toString())
        editor.apply()

        startActivity(Intent(this, ListActivity::class.java))
        finish()
    }
}