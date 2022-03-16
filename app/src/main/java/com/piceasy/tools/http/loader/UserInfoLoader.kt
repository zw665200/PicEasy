package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.UserInfo
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object UserInfoLoader : ObjectLoader() {

    fun getUser(token: String, code: String): Observable<Response<UserInfo>> {
        return RetrofitServiceManager.getInstance().userInfo.getUser(token, code, "AN")
    }
}