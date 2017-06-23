package com.natcom.fragment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.natcom.R
import com.natcom.activity.LeadController
import com.natcom.formatDate
import com.natcom.network.DenyResult
import com.natcom.network.NetworkController
import com.natcom.network.ShiftResult
import kotterknife.bindView
import java.text.SimpleDateFormat
import java.util.*

class LeadFragment : BoundFragment(), ShiftResult, DenyResult {
    val company by bindView<TextView>(R.id.company)
    val address by bindView<TextView>(R.id.address)
    val apartment by bindView<TextView>(R.id.apartment)
    val date by bindView<TextView>(R.id.date)
    val mount_date by bindView<TextView>(R.id.mount_date)
    val status by bindView<TextView>(R.id.status)
    val responsible by bindView<TextView>(R.id.responsible)
    val contacts by bindView<LinearLayout>(R.id.contacts)
    val close by bindView<Button>(R.id.close)
    val shift by bindView<Button>(R.id.shift)
    val deny by bindView<Button>(R.id.deny)

    var leadController: LeadController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView(inflater.inflate(R.layout.lead_fragment, container, false))

        leadController = (activity as LeadController)
        val lead = leadController?.lead()

        lead?.let {
            company.text = it.company
            address.text = it.address
            apartment.text = it.apartment
            date.text = formatDate(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.date))
            mount_date.text = it.mountDate
            status.text = it.status
            responsible.text = it.responsible
            contacts.removeAllViews()
            it.contacts.forEach {
                val view = getLayoutInflater(savedInstanceState).inflate(R.layout.contact_item, contacts, false)
                (view.findViewById(R.id.name) as TextView).text = it.name
                val phones = view.findViewById(R.id.phones) as LinearLayout
                it.phones.forEach {
                    val tw = TextView(activity)
                    tw.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    tw.text = it
                    phones.addView(tw)
                }
                contacts.addView(view)
            }
            if (it.editable == 0) {
                close.isEnabled = false
                shift.isEnabled = false
                deny.isEnabled = false
            }
        }


        (activity as AppCompatActivity).supportActionBar?.title = "${getString(R.string.lead)} №${lead?.id}"

        NetworkController.shiftCallback = this
        NetworkController.denyCallback = this

        close.setOnClickListener { leadController?.closeLead() }
        shift.setOnClickListener { leadController?.shiftLead() }
        deny.setOnClickListener { leadController?.denyLead() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        NetworkController.shiftCallback = null
        NetworkController.denyCallback = null
    }

    override fun onShiftResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.shift_success, Toast.LENGTH_SHORT).show()
        }
        leadController?.back()
    }

    override fun onDenyResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.deny_success, Toast.LENGTH_SHORT).show()
        }
        leadController?.back()
    }
}