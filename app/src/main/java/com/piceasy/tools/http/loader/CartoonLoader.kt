package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.TencentCloudResult
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object CartoonLoader {

    fun cartoonTrans(imageUrl: String): Observable<Response<TencentCloudResult>> {
        return RetrofitServiceManager.getInstance().cartoonTrans().cartoon(Constant.CLIENT_TOKEN, imageUrl)
    }
}