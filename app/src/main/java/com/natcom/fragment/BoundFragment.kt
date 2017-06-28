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

    protected fun initFragment(root: View, @StringRes title: Int) {
        this.root = root
        (activity as AppCompatActivity).supportActionBar?.title = getString(title)
    }

    protected fun initFragment(root: View, title: String) {
        this.root = root
        (activity as AppCompatActivity).supportActionBar?.title = title
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return root
    }
}