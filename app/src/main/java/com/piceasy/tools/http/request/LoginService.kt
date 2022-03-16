package com.piceasy.tools.http.request

import com.piceasy.tools.bean.UserInfo
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface LoginService {

    @POST("third/googleGrantLogin")
    @FormUrlEncoded
    fun getUser(
        @Field("questToken") questToken: String,
        @Field("googleToken") googleToken: String,
        @Field("osType") osType: String
    ): Observable<Response<UserInfo>>
}