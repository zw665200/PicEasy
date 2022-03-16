package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.Config
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import com.piceasy.tools.utils.AppUtil
import io.reactivex.Observable

object ConfigLoader {

    fun getConfig(): Observable<Response<Config>> {
        return RetrofitServiceManager.getInstance().config.getConfig(AppUtil.getChannelId())
    }
}