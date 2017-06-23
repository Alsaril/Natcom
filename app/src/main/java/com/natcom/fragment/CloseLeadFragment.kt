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
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.natcom.R
import com.natcom.activity.LeadController
import com.natcom.network.CloseResult
import com.natcom.network.NetworkController
import com.natcom.network.PictureResult
import com.natcom.prepareDate
import kotterknife.bindView
import java.io.File
import java.util.*


class CloseLeadFragment : BoundFragment(), PictureResult, CloseResult {
    val contract by bindView<CheckBox>(R.id.contract)
    val mount by bindView<CheckBox>(R.id.mount)
    val comment by bindView<EditText>(R.id.comment)
    val date by bindView<EditText>(R.id.date)
    val picture by bindView<Button>(R.id.picture)
    val save by bindView<Button>(R.id.save)

    private val TAKE_PICTURE = 1
    private var imageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView(inflater.inflate(R.layout.close_fragment, container, false))

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.close_lead)

        NetworkController.pictureCallback = this
        NetworkController.closeCallback = this
        picture.setOnClickListener { picture() }
        save.setOnClickListener { save() }

        date.setOnClickListener {
            val nDate = Calendar.getInstance()
            DatePickerDialog(activity, { _, year, month, day ->
                date.setText(prepareDate(year, month, day))
            }, nDate.get(Calendar.YEAR), nDate.get(Calendar.MONTH), nDate.get(Calendar.DAY_OF_MONTH)).show()
        }

        mount.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                if (!contract.isChecked && isChecked) {
                    Toast.makeText(activity, "Необходимо заключить договор", Toast.LENGTH_SHORT).show()
                    mount.isChecked = false
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NetworkController.pictureCallback = null
        NetworkController.closeCallback = null
    }

    val REQUEST_CODE = 1212

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
            } else {
                Toast.makeText(activity, "123", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun picture() {
        if (!checkAccess()) {
            requestAccess()
            return
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photo = File(Environment.getExternalStorageDirectory(), "Pic.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo))
        imageUri = Uri.fromFile(photo)
        startActivityForResult(intent, TAKE_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            NetworkController.picture((activity as LeadController).lead()!!.id, imageUri!!)
        }
    }

    override fun onPictureResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.post_success, Toast.LENGTH_SHORT).show()
        }
    }

    fun save() {
        if (comment.text.isEmpty() || (date.text.isEmpty() && mount.isChecked)) {
            Toast.makeText(activity, R.string.empty_fields, Toast.LENGTH_SHORT).show()
            return
        }
        NetworkController.close((activity as LeadController).lead()!!.id, contract.isChecked, mount.isChecked, comment.text.toString(), date.text.toString())
    }

    override fun onCloseResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.close_success, Toast.LENGTH_SHORT).show()
        }
        (activity as LeadController).back()
    }
}