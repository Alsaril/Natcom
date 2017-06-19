package com.natcom.fragment

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.natcom.R
import com.natcom.activity.LeadController
import com.natcom.network.DenyResult
import com.natcom.network.NetworkController
import com.natcom.network.ShiftResult
import kotterknife.bindView

class LeadFragment : BoundFragment(), ShiftResult, DenyResult {
    val company by bindView<TextView>(R.id.company)
    val address by bindView<TextView>(R.id.address)
    val date by bindView<TextView>(R.id.date)
    val mount_date by bindView<TextView>(R.id.mount_date)
    val close by bindView<Button>(R.id.close)
    val shift by bindView<Button>(R.id.shift)
    val deny by bindView<Button>(R.id.deny)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView(inflater.inflate(R.layout.lead_fragment, container, false))

        val lead = (activity as LeadController).lead()

        lead?.let {
            company.text = it.company
            address.text = it.address
            date.text = it.date
            mount_date.text = it.mountDate
        }

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.lead)

        NetworkController.shiftCallback = this
        NetworkController.denyCallback = this

        close.setOnClickListener { close() }
        shift.setOnClickListener { shift() }
        deny.setOnClickListener { deny() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        NetworkController.shiftCallback = null
        NetworkController.denyCallback = null
    }

    fun close() {
        (activity as LeadController).closeLead()
    }

    fun shift() {
        AlertDialog.Builder(activity)
                .setTitle(R.string.shift_lead)
                .setView(R.layout.shift_dialog)
                .setPositiveButton(R.string.ok) { d, which ->
                    val dialog = d as AlertDialog
                    val date = (dialog.findViewById(R.id.date) as EditText).text.toString()
                    val text = (dialog.findViewById(R.id.comment) as EditText).text.toString()
                    NetworkController.shift((activity as LeadController).lead()!!.id, date, text)
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
    }

    override fun onShiftResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.shift_success, Toast.LENGTH_SHORT).show()
        }
        (activity as LeadController).back()
    }

    fun deny() {
        AlertDialog.Builder(activity)
                .setTitle(R.string.deny_lead)
                .setView(R.layout.deny_dialog)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    val text = ((dialog as AlertDialog).findViewById(R.id.comment) as EditText).text.toString()
                    NetworkController.deny((activity as LeadController).lead()!!.id, text)
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
    }

    override fun onDenyResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.deny_success, Toast.LENGTH_SHORT).show()
        }
        (activity as LeadController).back()
    }
}