package com.natcom.activity

import android.content.Intent
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.natcom.ID_KEY
import com.natcom.R
import com.natcom.network.LoginResult
import com.natcom.network.NetworkController
import kotterknife.bindView

class LoginActivity : AppCompatActivity(), LoginResult {
    val login by bindView<TextView>(R.id.login)
    val password by bindView<TextView>(R.id.password)
    val confirm by bindView<Button>(R.id.confirm)
    val progress by bindView<ProgressBar>(R.id.progress)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.natcom.R.layout.login_activity)

        NetworkController.loginCallback = this
        confirm.setOnClickListener { confirm() }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkController.loginCallback = null
    }

    fun confirm() {
        if (login.text.isEmpty() || password.text.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            return
        }
        confirm.isEnabled = false
        progress.visibility = View.VISIBLE

        NetworkController.login(login.text.toString(), password.text.toString())
    }

    override fun onLoginResult(success: Boolean, id: Int) {
        confirm.isEnabled = true
        progress.visibility = View.GONE

        if (!success) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            return
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(ID_KEY, id).apply()
        Toast.makeText(this, "id = $id", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}