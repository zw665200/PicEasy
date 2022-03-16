package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.TencentCloudMorphResult
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object MorphLoader {

    fun morph(urls: String): Observable<Response<TencentCloudMorphResult>> {
        return RetrofitServiceManager.getInstance().morphService().morph(Constant.CLIENT_TOKEN, urls)
    }
}