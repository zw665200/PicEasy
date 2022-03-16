package com.piceasy.tools.http.request

import com.piceasy.tools.bean.OrderDetail
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderDetailService {

    @POST("orderDetail")
    @FormUrlEncoded
    fun getOrderDetail(@Field("orderSn") orderSn: String, @Field("clientToken") token: String): Observable<Response<OrderDetail>>
}