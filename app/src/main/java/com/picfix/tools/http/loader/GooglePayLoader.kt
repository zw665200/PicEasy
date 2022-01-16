package com.picfix.tools.http.loader

import com.picfix.tools.bean.GooglePayResult
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object GooglePayLoader {

    fun googlePayValidate(packName: String, productId: String, purchaseToken: String): Observable<Response<GooglePayResult>> {
        return RetrofitServiceManager.getInstance().orderValidate().orderValidate(Constant.CLIENT_TOKEN, packName, productId, purchaseToken)
    }
}