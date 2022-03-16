package com.piceasy.tools.http.request

import com.piceasy.tools.bean.AlipayParam
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface AliPayService {

    @POST("orderPay")
    @FormUrlEncoded
    fun getOrderParam(
        @Field("serviceId") serviceId: Int,
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String,
        @Field("channelCode") channelCode: String,
        @Field("isBack") isBack: Int
    ): Observable<Response<AlipayParam>>
}