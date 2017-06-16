package com.rv150.musictransfer.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

open class BoundFragment : Fragment() {
    internal var root: View? = null

    protected fun initView(root: View?) {
        this.root = root
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return root
    }
}