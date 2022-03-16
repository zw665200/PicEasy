package com.piceasy.tools.http.loader

import android.content.Context
import android.os.Build
import com.piceasy.tools.bean.GetToken
import com.piceasy.tools.bean.Token
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import com.piceasy.tools.utils.AES
import com.piceasy.tools.utils.AppUtil
import com.piceasy.tools.utils.DeviceUtil
import com.tencent.mmkv.MMKV
import io.reactivex.Observable

object TokenLoader {

    fun getToken(context: Context): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val device = Build.DEVICE
        val questTime = AppUtil.getChinaDate()
        val time = AppUtil.date2TimeStamp(questTime)

        val questFrom = AppUtil.getChannelId()
        val random = AppUtil.getRandomIV("jxwl@888")
        val questToken = AES.encryptByCBC(questFrom, AppUtil.MD5Encode((time / 1000).toString()) + questFrom, random)
        val productId = Constant.PRODUCT_ID

        val deviceId: String

        val mmkv = MMKV.defaultMMKV()
        var uuid = mmkv?.decodeString("uuid")
        if (uuid == null) {
            uuid = DeviceUtil.getUUID(context)
            mmkv?.encode("uuid", uuid)
            deviceId = uuid
        } else {
            deviceId = uuid
        }

        val token = GetToken(questTime, questToken, questFrom, device, deviceId, brand, mode, productId, random)

        return RetrofitServiceManager.getInstance().token.getToken(token)
    }
}