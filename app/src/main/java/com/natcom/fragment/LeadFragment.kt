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
    private val address by bindView<TextView>(R.id.address)
    private val apartment by bindView<TextView>(R.id.apartment)
    private val date by bindView<TextView>(R.id.date)
    private val mount_date by bindView<TextView>(R.id.mount_date)
    private val status by bindView<TextView>(R.id.status)
    private val responsible by bindView<TextView>(R.id.responsible)
    private val comment by bindView<TextView>(R.id.comment)
    private val contacts by bindView<LinearLayout>(R.id.contacts)
    private val close by bindView<Button>(R.id.close)
    private val shift by bindView<Button>(R.id.shift)
    private val deny by bindView<Button>(R.id.deny)
    private val pictures by bindView<Button>(R.id.pictures)


    private lateinit var leadController: LeadController

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
            val view = layoutInflater.inflate(R.layout.contact_item, contacts, false)
            view.findViewById<TextView>(R.id.name).text = it.name
            val phones = view.findViewById<LinearLayout>(R.id.phones)
            it.phones.forEach {
                val phone = layoutInflater.inflate(R.layout.phone_item, contacts, false)
                phone.findViewById<TextView>(R.id.phone).text = it
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