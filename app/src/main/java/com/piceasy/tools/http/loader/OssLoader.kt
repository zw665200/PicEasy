package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.OssParam
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object OssLoader : ObjectLoader() {

    fun getOssToken(token: String): Observable<Response<OssParam>> {
        return RetrofitServiceManager.getInstance().ossToken.getOssToken(token)
    }
}