package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.Order
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object OrderLoader {

    fun getOrders(): Observable<Response<List<Order>>> {
        return RetrofitServiceManager.getInstance().orders.getOrders(Constant.CLIENT_TOKEN, Constant.PRODUCT_ID)
    }
}