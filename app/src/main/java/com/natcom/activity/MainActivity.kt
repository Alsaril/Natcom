package com.natcom.activity


import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.natcom.LEAD_KEY
import com.natcom.LIST_TYPE_KEY
import com.natcom.fragment.CloseLeadFragment
import com.natcom.fragment.LeadFragment
import com.natcom.fragment.ListFragment
import com.natcom.fragment.ListType
import com.natcom.model.Lead
import kotterknife.bindView
import natcom.com.natcom.R

class MainActivity : AppCompatActivity(), CHF {

    val tabs by bindView<TabLayout>(R.id.tabs)
    var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*if (!auth(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }*/

        setContentView(R.layout.main_activity)

        ListType.values().take(2).forEach { tabs.addTab(tabs.newTab().setText(it.name)); }

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                val args = Bundle()
                args.putSerializable(LIST_TYPE_KEY, ListType.values()[tab.position])
                val fragment = ListFragment()
                fragment.arguments = args
                changeFragment(fragment, true)
            }

        })

        if (savedInstanceState == null) {
            changeFragment(ListFragment(), false)
        }
    }

    fun changeFragment(fragment: Fragment, replaceBackStack: Boolean) {
        if (replaceBackStack) {
            clearBackStack()
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.root, fragment)
        if (!replaceBackStack) {
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
        changeFragment(leadFragment, false)
    }

    override fun closeLead(lead: Lead) {
        val closeFragment = CloseLeadFragment()
        closeFragment.arguments.putParcelable(LEAD_KEY, lead)
        changeFragment(closeFragment, false)
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