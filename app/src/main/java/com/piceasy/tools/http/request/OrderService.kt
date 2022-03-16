package com.piceasy.tools.http.request

import com.piceasy.tools.bean.Order
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderService {

    @POST("orderList")
    @FormUrlEncoded
    fun getOrders(
        @Field("clientToken") token: String,
        @Field("productId") productId: String
    ): Observable<Response<List<Order>>>
}