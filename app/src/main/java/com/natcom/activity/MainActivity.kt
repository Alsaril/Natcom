package com.natcom.activity


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.natcom.*
import com.natcom.fragment.*
import com.natcom.model.Lead
import kotterknife.bindView
import java.util.*
import java.util.Calendar.*


class MainActivity : AppCompatActivity(), CHF {

    val navigation by bindView<BottomNavigationView>(R.id.navigation)
    var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!auth(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.main_activity)

        val mActionBarToolbar = findViewById(R.id.toolbar_actionbar) as Toolbar
        setSupportActionBar(mActionBarToolbar)

        navigation.setOnNavigationItemSelectedListener {
            val type = when (it.itemId) {
                R.id.today -> ListType.TODAY
                R.id.tomorrow -> ListType.TOMORROW
                else -> ListType.DATE
            }
            if (type == ListType.DATE) {
                val date = Calendar.getInstance()
                DatePickerDialog(this, { view, year, month, day ->
                    run {
                        val args = Bundle()
                        args.putSerializable(LIST_TYPE_KEY, type)
                        args.putString(PARAM_KEY, "$year.$month.$day")
                        val fragment = ListFragment()
                        fragment.arguments = args
                        changeFragment(fragment, true)
                    }
                }, date.get(YEAR), date.get(MONTH), date.get(DAY_OF_MONTH)).show()
            } else {
                val args = Bundle()
                args.putSerializable(LIST_TYPE_KEY, type)
                val fragment = ListFragment()
                fragment.arguments = args
                changeFragment(fragment, true)
            }
            true
        }

        if (savedInstanceState == null) {
            changeFragment(ListFragment(), false)
        } else {
            fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        val searchMenuItem = menu.findItem(R.id.action_search);
        val mSearchView = searchMenuItem.actionView as SearchView
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                (fragment as SearchFragment).update(query)
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                changeFragment(SearchFragment(), false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                back()
                return true
            }
        })
        return true
    }

    fun changeFragment(fragment: Fragment, replaceBackStack: Boolean) {
        if (replaceBackStack) {
            clearBackStack()
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.root, fragment, FRAGMENT_TAG)
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
        val bundle = Bundle()
        bundle.putParcelable(LEAD_KEY, lead)
        leadFragment.arguments = bundle
        clearBackStack()
        changeFragment(leadFragment, false)
    }

    override fun closeLead(lead: Lead) {
        val closeFragment = CloseLeadFragment()
        val bundle = Bundle()
        bundle.putParcelable(LEAD_KEY, lead)
        closeFragment.arguments = bundle
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