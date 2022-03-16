package com.piceasy.tools.http.request

import com.piceasy.tools.bean.MorphResult
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GetMorphUrlService {

    @POST("getMorphUrl")
    @FormUrlEncoded
    fun getMorphUrl(
        @Field("clientToken") clientToken: String,
        @Field("jobId") jobId: String
    ): Observable<Response<MorphResult>>
}