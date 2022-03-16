package com.piceasy.tools.http.request

import com.piceasy.tools.bean.GetToken
import com.piceasy.tools.bean.Token
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {

    @POST("getGoogleQuestToken")
    fun getToken(@Body getToken: GetToken): Observable<Response<Token>>
}