package com.natcom.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.natcom.FRAGMENT_TAG
import com.natcom.LEAD_KEY
import com.natcom.POSITION_KEY
import com.natcom.R
import com.natcom.fragment.*
import com.natcom.model.Lead

class LeadActivity : AppCompatActivity(), LeadController {
    lateinit var lead: Lead
    var fragment: Fragment? = null

    override fun lead() = lead

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lead_activity)

        val mActionBarToolbar = findViewById<Toolbar>(R.id.toolbar_actionbar)
        setSupportActionBar(mActionBarToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        lead = intent.getParcelableExtra<Lead>(LEAD_KEY)
                ?: savedInstanceState?.getParcelable(LEAD_KEY)
                ?: run {
            finish()
            return
        }

        if (savedInstanceState == null) {
            changeFragment(LeadFragment(), false)
        } else {
            fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun changeFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.root, fragment, FRAGMENT_TAG)
        if (addToBackStack) {
            transaction.addToBackStack(null)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }
        transaction.commitAllowingStateLoss()
        this.fragment = fragment
    }

    override fun denyLead() = changeFragment(DenyLeadFragment())

    override fun closeLead() = changeFragment(CloseLeadFragment())

    override fun shiftLead() = changeFragment(ShiftLeadFragment())

    override fun pictures() = changeFragment(PictureListFragment())

    override fun back() {
        supportFragmentManager.popBackStack()
        supportFragmentManager.popBackStack()
    }

    override fun fullscreen(position: Int) {
        val fragment = FullscreenFragment()
        val bundle = Bundle()
        bundle.putInt(POSITION_KEY, position)
        fragment.arguments = bundle
        changeFragment(fragment)
    }
}

interface LeadController {
    fun lead(): Lead
    fun denyLead()
    fun closeLead()
    fun shiftLead()
    fun pictures()
    fun back()
    fun fullscreen(position: Int)
}
