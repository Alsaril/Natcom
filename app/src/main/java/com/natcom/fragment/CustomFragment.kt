package com.natcom.fragment

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.natcom.JobHolder
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.atomic.AtomicReference

open class CustomFragment : Fragment() {
    var root = AtomicReference<View?>()
    private val jobHolder = JobHolder()

    private fun trySet(root: View) {
        this.root.compareAndSet(null, root)
    }

    protected fun initFragment(root: View, @StringRes title: Int) {
        trySet(root)
        (activity as AppCompatActivity).supportActionBar?.title = getString(title)
    }

    protected fun initFragment(root: View, title: String) {
        trySet(root)
        (activity as AppCompatActivity).supportActionBar?.title = title
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = root.get()

    override fun onDestroyView() {
        super.onDestroyView()
        jobHolder.dispose()
    }

    fun invokeLater(f: suspend () -> Unit) = jobHolder.add(launch(UI) { f() })
}