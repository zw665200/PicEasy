package com.piceasy.tools.http.loader

import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object ComplaintLoader : ObjectLoader() {

    fun reportComplaint(
        uid: String,
        complaintAType: String,
        phone: String,
        payAccount: String,
        problemDesc: String,
        pic: String
    ): Observable<Response<List<String?>?>> {
        return RetrofitServiceManager.getInstance().reportComplaint().reportComplaint(
            uid,
            Constant.USER_NAME,
            Constant.CLIENT_TOKEN,
            complaintAType,
            phone,
            payAccount,
            problemDesc,
            pic,
            Constant.PRODUCT_ID
        )
    }
}