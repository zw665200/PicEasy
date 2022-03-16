package com.piceasy.tools.http.request

import com.piceasy.tools.bean.CheckPayParam
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CheckPayService {

    @POST("cspayStatusV2")
    @FormUrlEncoded
    fun checkPay(
        @Field("productId") productId: String,
        @Field("clientToken") clientToken: String
    ): Observable<Response<CheckPayParam>>
}