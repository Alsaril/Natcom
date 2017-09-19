package com.natcom.model

import android.os.Parcel
import android.os.Parcelable

class Picture(val id: Int, val url: String) : Parcelable {
    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<Picture> {
            override fun newArray(size: Int): Array<Picture?> = arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): Picture = with(source) {
                return Picture(readInt(),
                        readString())
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(id)
        writeString(url)
    }

    override fun describeContents() = 0
}