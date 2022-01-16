package com.picfix.tools.http.request

import com.picfix.tools.bean.UserInfo
import com.picfix.tools.http.response.Response
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