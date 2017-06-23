package com.natcom.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.natcom.R
import com.natcom.REQUEST_CODE
import com.natcom.UPDATE_LIST
import com.natcom.activity.LeadController
import com.natcom.network.NetworkController
import com.natcom.network.ShiftResult
import com.natcom.prepareDate
import kotterknife.bindView
import java.util.*


class ShiftLeadFragment : BoundFragment(), ShiftResult {
    val date by bindView<EditText>(R.id.date)
    val comment by bindView<EditText>(R.id.comment)
    val ok by bindView<Button>(R.id.ok)
    val cancel by bindView<Button>(R.id.cancel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView(inflater.inflate(R.layout.shift_fragment, container, false))

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.shift_lead)

        NetworkController.shiftCallback = this
        date.setOnClickListener {
            val nDate = Calendar.getInstance()
            DatePickerDialog(activity, { _, year, month, day ->
                date.setText(prepareDate(year, month, day))
            }, nDate.get(Calendar.YEAR), nDate.get(Calendar.MONTH), nDate.get(Calendar.DAY_OF_MONTH)).show()
        }
        ok.setOnClickListener { save() }
        cancel.setOnClickListener { activity.onBackPressed() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NetworkController.shiftCallback = null
    }

    override fun onShiftResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.shift_success, Toast.LENGTH_SHORT).show()
            activity.setResult(REQUEST_CODE, Intent(UPDATE_LIST))
            activity.finish()
        }
    }

    fun save() {
        if (comment.text.isEmpty() || date.text.isEmpty()) {
            Toast.makeText(activity, R.string.empty_fields, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(activity)
                .setTitle(R.string.sure)
                .setPositiveButton(R.string.ok) { _, _ ->
                    NetworkController.shift((activity as LeadController).lead()!!.id, date.text.toString(), comment.text.toString())
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
    }
}