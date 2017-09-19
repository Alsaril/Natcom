package com.natcom.fragment

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import com.natcom.*
import com.natcom.activity.LeadController
import com.natcom.model.CloseRequest
import com.natcom.network.NetworkController
import kotterknife.bindView
import java.io.File
import java.util.*


class CloseLeadFragment : CustomFragment() {
    private val contract by bindView<CheckBox>(R.id.contract)
    private val mount by bindView<CheckBox>(R.id.mount)
    private val cashless by bindView<CheckBox>(R.id.cashless)
    private val comment by bindView<EditText>(R.id.comment)
    private val date by bindView<EditText>(R.id.date)
    private val leadSum by bindView<EditText>(R.id.leadSum)
    private val prepay by bindView<EditText>(R.id.prepay)
    private val picture by bindView<Button>(R.id.picture)
    private val save by bindView<Button>(R.id.save)

    private val REQUEST_CODE = 1212
    private val TAKE_PICTURE = 1
    private var imageUri: Uri? = null

    private lateinit var leadController: LeadController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initFragment(inflater.inflate(R.layout.close_fragment, container, false), R.string.close_lead)

        leadController = activity as LeadController

        picture.setOnClickListener { picture() }
        save.setOnClickListener { save() }

        date.setOnClickListener {
            val nDate = Calendar.getInstance()
            DatePickerDialog(activity, { _, year, month, day ->
                date.setText(prepareDate(year, month, day))
            }, nDate.get(Calendar.YEAR), nDate.get(Calendar.MONTH), nDate.get(Calendar.DAY_OF_MONTH)).show()
        }

        mount.setOnCheckedChangeListener { _, isChecked ->
            if (!contract.isChecked && isChecked) {
                toast(R.string.need_contract)
                mount.isChecked = false
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun checkAccess() = ContextCompat.checkSelfPermission(context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestAccess() {
        if (!checkAccess()) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                picture()
            }
        }
    }

    fun picture() {
        if (!checkAccess()) {
            requestAccess()
            return
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photo = File(Environment.getExternalStorageDirectory(), "pic.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo))
        imageUri = Uri.fromFile(photo)
        startActivityForResult(intent, TAKE_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = imageUri
        if (requestCode != TAKE_PICTURE || resultCode != Activity.RESULT_OK || uri == null) return
        invokeLater {
            compressImage(uri).await()
            val result = NetworkController.picture(leadController.lead().id, uri).awaitResponse()
            toast(if (!result.isSuccessful()) R.string.error else R.string.post_success)
        }
    }

    private fun save() = invokeLater {
        if (comment.text.isEmpty() || (date.text.isEmpty() && mount.isChecked)) {
            toast(R.string.empty_fields)
            return@invokeLater
        }

        val closeRequest = CloseRequest(
                contract.isChecked,
                mount.isChecked,
                comment.text.toString(),
                date.text.toString(),
                leadSum.text.toString(),
                prepay.text.toString(),
                cashless.isChecked)

        val result = NetworkController.api.close(leadController.lead().id, closeRequest).awaitResponse()
        if (!result.isSuccessful()) {
            toast(R.string.error)
            return@invokeLater
        }
        toast(R.string.close_success)
        leadController.back()
    }
}