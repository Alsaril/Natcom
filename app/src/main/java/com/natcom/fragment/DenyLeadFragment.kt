package com.natcom.fragment

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
import com.natcom.model.DenyRequest
import com.natcom.network.NetworkController
import kotterknife.bindView


class DenyLeadFragment : CustomFragment() {
    private val comment by bindView<EditText>(R.id.comment)
    private val ok by bindView<Button>(R.id.ok)
    private val cancel by bindView<Button>(R.id.cancel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initFragment(inflater.inflate(R.layout.deny_fragment, container, false), R.string.deny_lead)

        ok.setOnClickListener { save() }
        cancel.setOnClickListener { activity.onBackPressed() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun save() {
        if (comment.text.isEmpty()) {
            toast(R.string.empty_fields)
            return
        }

        AlertDialog.Builder(activity)
                .setTitle(R.string.sure)
                .setPositiveButton(R.string.ok) { _, _ -> deny() }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun deny() = invokeLater {
        val result = NetworkController.api.deny(
                (activity as LeadController).lead().id,
                DenyRequest(comment.text.toString())).awaitResponse()
        if (!result.isSuccessful()) {
            toast(R.string.error)
            return@invokeLater
        }
        toast(R.string.deny_success)
        activity.setResult(REQUEST_CODE, Intent(UPDATE_LIST))
        activity.finish()
    }
}