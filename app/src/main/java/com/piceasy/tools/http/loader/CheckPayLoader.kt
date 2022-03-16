package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.CheckPayParam
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object CheckPayLoader {

    fun checkPay(): Observable<Response<CheckPayParam>> {
        return RetrofitServiceManager.getInstance().checkPayService().checkPay(Constant.PRODUCT_ID, Constant.CLIENT_TOKEN)
    }
}