package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.GooglePayResult
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object GooglePayLoader {

    fun googlePayValidate(packName: String, productId: String, purchaseToken: String): Observable<Response<GooglePayResult>> {
        return RetrofitServiceManager.getInstance().orderValidate().orderValidate(Constant.CLIENT_TOKEN, packName, productId, purchaseToken)
    }
}