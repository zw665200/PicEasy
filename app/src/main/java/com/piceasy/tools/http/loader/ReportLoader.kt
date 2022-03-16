package com.piceasy.tools.http.loader

import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object ReportLoader {

    fun report(): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().report().report(Constant.CLIENT_TOKEN)
    }
}