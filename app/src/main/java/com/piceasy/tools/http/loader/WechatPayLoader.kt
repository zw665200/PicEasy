package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.WechatPayParam
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import com.piceasy.tools.utils.AppUtil
import io.reactivex.Observable

object WechatPayLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<WechatPayParam>> {
        return RetrofitServiceManager.getInstance().wechatPayStatus.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId()
        )
    }
}