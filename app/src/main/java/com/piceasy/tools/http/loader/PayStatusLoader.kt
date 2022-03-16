package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.PayStatus
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object PayStatusLoader {

    fun getPayStatus(serviceId: Int, token: String): Observable<Response<PayStatus>> {
        return RetrofitServiceManager.getInstance().payStatus.getPayStatus(serviceId, token)
    }
}