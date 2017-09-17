package com.natcom.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.natcom.R
import com.natcom.activity.LeadController
import com.natcom.formatDate
import kotterknife.bindView
import java.text.SimpleDateFormat
import java.util.*

class LeadFragment : CustomFragment() {
    val address by bindView<TextView>(R.id.address)
    val apartment by bindView<TextView>(R.id.apartment)
    val date by bindView<TextView>(R.id.date)
    val mount_date by bindView<TextView>(R.id.mount_date)
    val status by bindView<TextView>(R.id.status)
    val responsible by bindView<TextView>(R.id.responsible)
    val comment by bindView<TextView>(R.id.comment)
    val contacts by bindView<LinearLayout>(R.id.contacts)
    val close by bindView<Button>(R.id.close)
    val shift by bindView<Button>(R.id.shift)
    val deny by bindView<Button>(R.id.deny)
    val pictures by bindView<Button>(R.id.pictures)


    lateinit var leadController: LeadController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        leadController = activity as LeadController // hack
        val lead = leadController.lead()            // hack
        // init MUST be the first instruction
        initFragment(inflater.inflate(R.layout.lead_fragment, container, false), "${getString(R.string.lead)} №${formatNumber(lead.id)}")

        address.text = lead.address
        apartment.text = lead.apartment
        date.text = formatDate(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(lead.date))
        mount_date.text = lead.mountDate
        status.text = lead.status
        responsible.text = lead.responsible
        comment.text = lead.comment
        contacts.removeAllViews()
        lead.contacts.forEach {
            val view = getLayoutInflater(savedInstanceState).inflate(R.layout.contact_item, contacts, false)
            (view.findViewById(R.id.name) as TextView).text = it.name
            val phones = view.findViewById(R.id.phones) as LinearLayout
            it.phones.forEach {
                val phone = getLayoutInflater(savedInstanceState).inflate(R.layout.phone_item, contacts, false)
                (phone.findViewById(R.id.phone) as TextView).text = it
                phones.addView(phone)
            }
            contacts.addView(view)
        }
        if (lead.editable == 0) {
            close.isEnabled = false
            shift.isEnabled = false
            deny.isEnabled = false
        }

        close.setOnClickListener { leadController.closeLead() }
        shift.setOnClickListener { leadController.shiftLead() }
        deny.setOnClickListener { leadController.denyLead() }
        pictures.setOnClickListener { leadController.pictures() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun formatNumber(number: Int): String {
        val s = number.toString()
        return s.substring(0, 4) + "-" + s.substring(4)
    }
}