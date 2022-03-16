package com.piceasy.tools.http.request

import com.piceasy.tools.bean.TencentCloudResult
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GenderTransService {

    @POST("sexPic")
    @FormUrlEncoded
    fun genderTrans(
        @Field("clientToken") clientToken: String,
        @Field("imageUrl") imageUrl: String,
        @Field("gender") gender: Int
    ): Observable<Response<TencentCloudResult>>
}
