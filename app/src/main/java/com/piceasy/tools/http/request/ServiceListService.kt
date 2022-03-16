package com.piceasy.tools.http.request

import com.piceasy.tools.bean.Price
import com.piceasy.tools.config.Constant
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ServiceListService {

    @GET("serverList/${Constant.PRODUCT_ID}")
    fun getServiceList(): Observable<Response<List<Price>>>
}