package com.piceasy.tools.http.request

import com.piceasy.tools.bean.OssParam
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OssService {

    @POST("grantAKToken")
    @FormUrlEncoded
    fun getOssToken(@Field("clientToken") clientToken: String): Observable<Response<OssParam>>
}