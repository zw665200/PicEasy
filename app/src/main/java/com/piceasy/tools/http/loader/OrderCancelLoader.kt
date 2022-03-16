package com.piceasy.tools.http.loader

import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object OrderCancelLoader {

    fun orderCancel(orderSn: String): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().orderCancel().orderCancel(orderSn, Constant.CLIENT_TOKEN)
    }
}