package com.tanodxyz.documentrenderer

import android.os.Parcel
import android.os.Parcelable

class SavedState(
    var currentPage: Int = 0,
    var zoomLevel: Float = 0F
) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readInt(),parcel.readFloat()) {}

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(currentPage)
        parcel.writeFloat(zoomLevel)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {
        override fun createFromParcel(parcel: Parcel): SavedState {
            return SavedState(parcel)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }
}