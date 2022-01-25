package com.picfix.tools.http.loader

import com.picfix.tools.bean.UserInfo
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/25 14:56
 */
object BaseLoader {

    fun visitLogin(): Observable<Response<List<UserInfo>>> {
        return RetrofitServiceManager.getInstance().baseService.visit(Constant.QUEST_TOKEN,"AN")
    }

}