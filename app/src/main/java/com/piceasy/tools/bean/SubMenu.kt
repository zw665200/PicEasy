package com.piceasy.tools.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubMenu(
    var server_code: String,
    var sale_price: String,
    var desc: String
) : Parcelable
