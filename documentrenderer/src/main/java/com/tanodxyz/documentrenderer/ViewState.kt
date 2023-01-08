package com.tanodxyz.documentrenderer

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

class ViewState(
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

    companion object CREATOR : Parcelable.Creator<ViewState> {
        override fun createFromParcel(parcel: Parcel): ViewState {
            return ViewState(parcel)
        }

        override fun newArray(size: Int): Array<ViewState?> {
            return arrayOfNulls(size)
        }
    }
}