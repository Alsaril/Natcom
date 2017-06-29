package com.natcom.fragment

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

open class BoundFragment : Fragment() {
    lateinit var root: View
    var set: Boolean = false

    private fun trySet(root: View) {
        if (!set) {
            synchronized(set) {
                if (!set) {
                    this.root = root
                    set = true
                }
            }
        }
    }

    protected fun initFragment(root: View, @StringRes title: Int) {
        trySet(root)
        (activity as AppCompatActivity).supportActionBar?.title = getString(title)
    }

    protected fun initFragment(root: View, title: String) {
        trySet(root)
        (activity as AppCompatActivity).supportActionBar?.title = title
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return root
    }
}