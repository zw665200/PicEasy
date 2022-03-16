package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.AlipayParam
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import com.piceasy.tools.utils.AppUtil
import io.reactivex.Observable

object AliPayLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<AlipayParam>> {
        return RetrofitServiceManager.getInstance().aliPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId(),
            1
        )
    }
}