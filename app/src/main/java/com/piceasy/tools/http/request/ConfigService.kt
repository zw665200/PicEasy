package com.piceasy.tools.http.request

import com.piceasy.tools.bean.Config
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ConfigService {

    @GET("siteInfo")
    fun getConfig(@Query("serverCode") serviceCode: String): Observable<Response<Config>>
}