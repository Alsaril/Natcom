package com.natcom.fragment

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.natcom.LEAD_KEY
import com.natcom.R
import com.natcom.activity.CHF
import com.natcom.model.Lead
import com.natcom.network.DenyResult
import com.natcom.network.NetworkController
import com.rv150.musictransfer.fragment.BoundFragment
import kotterknife.bindView

class LeadFragment : BoundFragment(), DenyResult {
    val company by bindView<TextView>(R.id.company)
    val address by bindView<TextView>(R.id.address)
    val date by bindView<TextView>(R.id.date)
    val mount_date by bindView<TextView>(R.id.mount_date)
    val close by bindView<Button>(R.id.close)
    val shift by bindView<Button>(R.id.shift)
    val deny by bindView<Button>(R.id.deny)

    var lead: Lead? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView(inflater.inflate(R.layout.lead_fragment, container, false))

        lead = arguments.getParcelable<Lead>(LEAD_KEY)

        lead?.let {
            company.text = it.company
            address.text = it.address
            date.text = it.date
            mount_date.text = it.mountDate
        }

        NetworkController.denyCallback = this

        close.setOnClickListener { close() }
        shift.setOnClickListener { shift() }
        deny.setOnClickListener { deny() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun close() {
        lead?.let { (activity as CHF).closeLead(it) }
    }

    fun shift() {
        TODO("Not implemented")
    }

    fun deny() {
        val editText = EditText(activity)
        AlertDialog.Builder(activity)
                .setMessage("Comment")
                .setTitle("Deny lead")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    NetworkController.deny(lead!!.id, editText.text.toString())
                }.show()
    }

    override fun onDenyResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Deny successful!", Toast.LENGTH_SHORT).show()
        }
        (activity as CHF).back()
    }
}