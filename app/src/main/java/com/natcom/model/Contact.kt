package com.natcom.model

import android.os.Parcel
import android.os.Parcelable

data class Contact(val name: String, val phone: String?) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Contact> {
            override fun newArray(size: Int): Array<Contact?> = arrayOfNulls(size)

            override fun createFromParcel(source: Parcel) =
                    Contact(source.readString(),
                            source.readString())
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(phone)
    }

    override fun describeContents() = 0
}
