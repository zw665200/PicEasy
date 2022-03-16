package com.piceasy.tools.http.loader

import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object AccountLoader {

    fun delete(): Observable<Response<String>> {
        return RetrofitServiceManager.getInstance().accountDelete().delete(Constant.CLIENT_TOKEN, Constant.PRODUCT_ID)
    }
}