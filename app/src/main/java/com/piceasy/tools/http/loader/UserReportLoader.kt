package com.piceasy.tools.http.loader

import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object UserReportLoader {

    fun report(path: String, content: String, logType: String): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().userReport().report(Constant.CLIENT_TOKEN, path, content, logType)
    }
}