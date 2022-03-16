package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.Price
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object ServiceListLoader {

    fun getServiceList(): Observable<Response<List<Price>>> {
        return RetrofitServiceManager.getInstance().price.getServiceList()
    }
}