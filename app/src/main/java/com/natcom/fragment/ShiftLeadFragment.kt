package com.natcom.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.natcom.*
import com.natcom.activity.LeadController
import com.natcom.model.ShiftRequest
import com.natcom.network.NetworkController
import kotterknife.bindView
import java.util.*


class ShiftLeadFragment : CustomFragment() {
    private val date by bindView<EditText>(R.id.date)
    private val comment by bindView<EditText>(R.id.comment)
    private val ok by bindView<Button>(R.id.ok)
    private val cancel by bindView<Button>(R.id.cancel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initFragment(inflater.inflate(R.layout.shift_fragment, container, false), R.string.shift_lead)

        date.setOnClickListener {
            val date = Calendar.getInstance()
            DatePickerDialog(activity, { _, year, month, day ->
                this.date.setText(prepareDate(year, month, day))
            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
        }
        ok.setOnClickListener { save() }
        cancel.setOnClickListener { activity.onBackPressed() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun save() {
        if (comment.text.isEmpty() || date.text.isEmpty()) {
            toast(R.string.empty_fields)
            return
        }

        AlertDialog.Builder(activity)
                .setTitle(R.string.sure)
                .setPositiveButton(R.string.ok) { _, _ -> shift() }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
    }

    private fun shift() = invokeLater {
        val result = NetworkController.api.shift((activity as LeadController).lead().id,
                ShiftRequest(date.text.toString(), comment.text.toString())).awaitResponse()
        if (!result.isSuccessful()) {
            toast(R.string.error)
            return@invokeLater
        }
        toast(R.string.shift_success)
        activity.setResult(REQUEST_CODE, Intent(UPDATE_LIST))
        activity.finish()
    }
}