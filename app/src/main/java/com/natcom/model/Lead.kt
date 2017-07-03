package com.natcom.model

import android.os.Parcel
import android.os.Parcelable

data class Lead(val id: Int,
                val company: String,
                val address: String,
                val apartment: String,
                val date: String,
                val mountDate: String?,
                val status: String,
                val responsible: String,
                val comment: String,
                val color: Int,
                val editable: Int,
                val images: ArrayList<Picture>,
                val contacts: ArrayList<Contact>) : Parcelable {

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<Lead> {
            override fun newArray(size: Int): Array<Lead?> = arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): Lead {
                with(source) {
                    val pictures = ArrayList<Picture>()
                    val contacts = ArrayList<Contact>()
                    readTypedList(pictures, Picture.CREATOR)
                    readTypedList<Contact>(contacts, Contact.CREATOR)
                    return Lead(readInt(),
                            readString(),
                            readString(),
                            readString(),
                            readString(),
                            readString(),
                            readString(),
                            readString(),
                            readString(),
                            readInt(),
                            readInt(),
                            pictures,
                            contacts)
                }
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeTypedList(images)
            writeTypedList(contacts)
            writeInt(id)
            writeString(company)
            writeString(address)
            writeString(apartment)
            writeString(date)
            writeString(mountDate)
            writeString(status)
            writeString(responsible)
            writeString(comment)
            writeInt(color)
            writeInt(editable)
        }
    }

    override fun describeContents() = 0

}
