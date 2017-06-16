package com.natcom.model

import android.os.Parcel
import android.os.Parcelable

data class Lead(val id: Int,
                val company: String,
                val address: String,
                val apartment: String,
                val date: String,
                val mountDate: String,
                val status: String,
                val responsible: String,
                val color: Int,
                val contacts: List<Contact>) : Parcelable {

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Lead> {
            override fun newArray(size: Int): Array<Lead?> = arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): Lead {
                with(source) {
                    val id = readInt()
                    val company = readString()
                    val address = readString()
                    val apartment = readString()
                    val date = readString()
                    val mountDate = readString()
                    val status = readString()
                    val responsible = readString()
                    val color = readInt()
                    val contacts = java.util.ArrayList<Contact>()
                    readTypedList<Contact>(contacts, Contact.CREATOR)
                    return Lead(id, company, address, apartment, date, mountDate, status, responsible, color, contacts)
                }
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeInt(id)
            writeString(company)
            writeString(address)
            writeString(apartment)
            writeString(date)
            writeString(mountDate)
            writeString(status)
            writeString(responsible)
            writeInt(color)
            writeTypedList(contacts)
        }
    }

    override fun describeContents() = 0

}
