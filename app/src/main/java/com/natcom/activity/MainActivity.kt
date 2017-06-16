package com.natcom.activity


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.natcom.CookieHelper.auth
import com.natcom.LEAD_KEY
import com.natcom.fragment.CloseLeadFragment
import com.natcom.fragment.LeadFragment
import com.natcom.model.Lead
import natcom.com.natcom.R

class MainActivity : AppCompatActivity(), CHF {

    var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!auth(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            // set default fragment
        }
    }

    fun changeFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.root, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commitAllowingStateLoss()
        this.fragment = fragment
    }

    fun clearBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun openLead(lead: Lead) {
        val leadFragment = LeadFragment()
        leadFragment.arguments.putParcelable(LEAD_KEY, lead)
        clearBackStack()
        changeFragment(fragment!!, false)
        changeFragment(leadFragment, true)
    }

    override fun closeLead(lead: Lead) {
        val closeFragment = CloseLeadFragment()
        closeFragment.arguments.putParcelable(LEAD_KEY, lead)
        changeFragment(closeFragment, true)
    }

    override fun back() {
        supportFragmentManager.popBackStack()
        supportFragmentManager.popBackStack()
    }
}

interface CHF {
    fun openLead(lead: Lead)
    fun closeLead(lead: Lead)
    fun back()
}