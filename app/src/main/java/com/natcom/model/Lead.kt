package com.natcom.model

import android.os.Parcel
import android.os.Parcelable

data class Lead(val id: Int,
                val company: String,
                val address: String,
                val date: String,
                val mountDate: String,
                val status: String) : Parcelable {

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Lead> {
            override fun newArray(size: Int): Array<Lead?> = arrayOfNulls(size)

            override fun createFromParcel(source: Parcel) =
                    Lead(source.readInt(),
                            source.readString(),
                            source.readString(),
                            source.readString(),
                            source.readString(),
                            source.readString())

        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(company)
        dest.writeString(address)
        dest.writeString(date)
        dest.writeString(mountDate)
        dest.writeString(status)
    }

    override fun describeContents() = 0

}
