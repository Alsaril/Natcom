package com.natcom.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
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
import com.natcom.network.DenyResult
import com.natcom.network.NetworkController
import kotterknife.bindView


class DenyLeadFragment : BoundFragment(), DenyResult {
    val comment by bindView<EditText>(R.id.comment)
    val ok by bindView<Button>(R.id.ok)
    val cancel by bindView<Button>(R.id.cancel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initFragment(inflater.inflate(R.layout.deny_fragment, container, false), R.string.deny_lead)

        NetworkController.denyCallback = this
        ok.setOnClickListener { save() }
        cancel.setOnClickListener { activity.onBackPressed() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NetworkController.denyCallback = null
    }

    override fun onDenyResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.deny_success, Toast.LENGTH_SHORT).show()
            activity.setResult(REQUEST_CODE, Intent(UPDATE_LIST))
            activity.finish()
        }
    }

    fun save() {
        if (comment.text.isEmpty()) {
            Toast.makeText(activity, R.string.empty_fields, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(activity)
                .setTitle(R.string.sure)
                .setPositiveButton(R.string.ok) { _, _ ->
                    NetworkController.deny((activity as LeadController).lead().id, comment.text.toString())
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
    }
}