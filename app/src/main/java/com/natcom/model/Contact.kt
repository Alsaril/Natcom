package com.natcom.model

import android.os.Parcel
import android.os.Parcelable

data class Contact(val name: String, val phones: ArrayList<String>) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Contact> {
            override fun newArray(size: Int): Array<Contact?> = arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): Contact {
                val name = source.readString()
                val phones = ArrayList<String>()
                source.readStringList(phones)
                return Contact(name, phones)
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeStringList(phones)
    }

    override fun describeContents() = 0
}
