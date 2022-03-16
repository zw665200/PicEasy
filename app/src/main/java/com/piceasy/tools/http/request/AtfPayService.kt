package com.piceasy.tools.http.request

import com.piceasy.tools.bean.AlipayParam
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AtfPayService {

    @POST("atfOrder")
    @FormUrlEncoded
    fun getOrderParam(
        @Field("serverId") serverId: Int,
        @Field("clientToken") token: String,
        @Field("channelCode") channelCode: String,
        @Field("unit") unit: String
    ): Observable<Response<AlipayParam>>
}