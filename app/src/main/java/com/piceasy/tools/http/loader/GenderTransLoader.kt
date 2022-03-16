package com.piceasy.tools.http.loader

import com.piceasy.tools.bean.TencentCloudResult
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import io.reactivex.Observable

object GenderTransLoader {

    /**
     * @param imageUrl
     * @param gender 0:male to female  1:female to male
     */
    fun genderTrans(imageUrl: String, gender: Int): Observable<Response<TencentCloudResult>> {
        return RetrofitServiceManager.getInstance().genderTrans().genderTrans(Constant.CLIENT_TOKEN, imageUrl, gender)
    }
}