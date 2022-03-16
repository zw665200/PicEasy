package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.FastPayParam
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import com.piceasy.tools.utils.AppUtil
import io.reactivex.Observable

object FastPayParamLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<FastPayParam>> {
        return RetrofitServiceManager.getInstance().fastPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId()
        )
    }
}