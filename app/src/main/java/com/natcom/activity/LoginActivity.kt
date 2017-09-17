package com.natcom.activity

import android.content.Intent
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.natcom.LOGIN_KEY
import com.natcom.PASSWORD_KEY
import com.natcom.R
import com.natcom.toast
import kotterknife.bindView

class LoginActivity : AppCompatActivity() {
    val login by bindView<TextView>(R.id.login)
    val password by bindView<TextView>(R.id.password)
    val confirm by bindView<Button>(R.id.confirm)
    val progress by bindView<ProgressBar>(R.id.progress)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val mActionBarToolbar = findViewById(R.id.toolbar_actionbar) as Toolbar
        setSupportActionBar(mActionBarToolbar)
        supportActionBar?.title = getString(R.string.login)

        confirm.setOnClickListener { confirm() }
    }

    fun confirm() {
        if (login.text.isEmpty() || password.text.isEmpty()) {
            toast(R.string.empty_fields)
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