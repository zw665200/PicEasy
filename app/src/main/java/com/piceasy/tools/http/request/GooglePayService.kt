package com.piceasy.tools.http.request

import com.piceasy.tools.bean.GooglePayResult
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GooglePayService {

    //    @POST("notify/googlePayBack")
    @POST("googleSub")
    @FormUrlEncoded
    fun orderValidate(
        @Field("clientToken") token: String,
        @Field("packageName") packageName: String,
        @Field("productId") productId: String,
        @Field("purchaseToken") purchaseToken: String
    ): Observable<Response<GooglePayResult>>
}