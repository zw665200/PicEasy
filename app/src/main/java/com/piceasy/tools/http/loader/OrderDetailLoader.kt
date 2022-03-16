package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.OrderDetail
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object OrderDetailLoader {

    fun getOrderStatus(orderSn: String): Observable<Response<OrderDetail>> {
        return RetrofitServiceManager.getInstance().orderDetail.getOrderDetail(orderSn, Constant.CLIENT_TOKEN)
    }
}