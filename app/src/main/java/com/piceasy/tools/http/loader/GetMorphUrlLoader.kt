package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.MorphResult
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object GetMorphUrlLoader {

    fun getMorphUrl(jobId: String): Observable<Response<MorphResult>> {
        return RetrofitServiceManager.getInstance().morphUrlService.getMorphUrl(Constant.CLIENT_TOKEN, jobId)
    }
}