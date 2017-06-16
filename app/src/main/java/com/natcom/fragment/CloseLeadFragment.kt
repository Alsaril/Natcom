package com.natcom.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.natcom.LEAD_KEY
import com.natcom.R
import com.natcom.activity.CHF
import com.natcom.model.Lead
import com.natcom.network.CloseResult
import com.natcom.network.NetworkController
import com.natcom.network.PictureResult
import com.rv150.musictransfer.fragment.BoundFragment
import kotterknife.bindView
import java.io.File


class CloseLeadFragment : BoundFragment(), PictureResult, CloseResult {
    val contract by bindView<CheckBox>(R.id.contract)
    val mount by bindView<CheckBox>(R.id.mount)
    val comment by bindView<EditText>(R.id.comment)
    val date by bindView<EditText>(R.id.date)
    val picture by bindView<Button>(R.id.picture)
    val save by bindView<Button>(R.id.save)

    private val TAKE_PICTURE = 1
    private var imageUri: Uri? = null
    var lead: Lead? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView(inflater.inflate(R.layout.close_fragment, container, false))

        lead = arguments.getParcelable<Lead>(LEAD_KEY)

        NetworkController.pictureCallback = this
        NetworkController.closeCallback = this
        picture.setOnClickListener { picture() }
        save.setOnClickListener { save() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NetworkController.pictureCallback = null
        NetworkController.closeCallback = null
    }

    fun picture() {
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
            NetworkController.picture(lead!!.id, imageUri!!)
        }
    }

    override fun onPictureResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "POST successful!", Toast.LENGTH_SHORT).show()
        }
    }

    fun save() {
        if (comment.text.isEmpty() || date.text.isEmpty()) {
            Toast.makeText(activity, R.string.empty_fields, Toast.LENGTH_SHORT).show()
            return
        }
        NetworkController.close(lead!!.id, contract.isChecked, mount.isChecked, comment.text.toString(), date.text.toString())
    }

    override fun onCloseResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Close successful!", Toast.LENGTH_SHORT).show()
        }
        (activity as CHF).back()
    }
}