package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.TencentCloudResult
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object AgeTransLoader {

    fun ageTrans(imageUrl: String, age: Int): Observable<Response<TencentCloudResult>> {
        return RetrofitServiceManager.getInstance().ageTrans().ageTrans(Constant.CLIENT_TOKEN, imageUrl, age)
    }
}