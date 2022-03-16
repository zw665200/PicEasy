package com.piceasy.tools.http.loader

import android.content.Context
import android.os.Build
import com.piceasy.tools.bean.*
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.http.response.Response
import com.piceasy.tools.utils.AES
import com.piceasy.tools.utils.AppUtil
import com.piceasy.tools.utils.DeviceUtil
import com.piceasy.tools.utils.JLog
import com.tencent.mmkv.MMKV
import io.reactivex.Observable

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/25 14:56
 */
object BaseLoader {

    fun getToken(context: Context): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val device = Build.DEVICE
        val questTime = AppUtil.getChinaDate()
        val time = AppUtil.date2TimeStamp(questTime)

        val questFrom = AppUtil.getChannelId()
        val random = AppUtil.getRandomIV("jxwl@888")
        val questToken = AES.encryptByECB(questFrom, AppUtil.MD5Encode((time / 1000).toString()) + questFrom)
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

        JLog.i("deviceId = $deviceId")

        val token = GetToken(questTime, questToken, questFrom, device, deviceId, brand, mode, productId, random)

        return RetrofitServiceManager.getInstance().baseService.getToken(token)
    }

    fun getGoogleToken(context: Context): Observable<Response<Token>> {
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

        return RetrofitServiceManager.getInstance().baseService.getGoogleToken(token)
    }

    fun visitLogin(): Observable<Response<List<UserInfo>>> {
        return RetrofitServiceManager.getInstance().baseService.visit(Constant.QUEST_TOKEN, "AN")
    }

    fun getMenuList(): Observable<Response<Menu>> {
        return RetrofitServiceManager.getInstance().baseService.getSubServiceList(Constant.CLIENT_TOKEN)
    }

    fun getServiceList(): Observable<Response<List<Price>>> {
        return RetrofitServiceManager.getInstance().baseService.getServiceList()
    }

    fun checkSubPay(): Observable<Response<SubPay>> {
        return RetrofitServiceManager.getInstance().baseService.checkPay(Constant.CLIENT_TOKEN)
    }

    fun getUserByWechat(token: String, code: String): Observable<Response<UserInfo>> {
        return RetrofitServiceManager.getInstance().baseService.getUserByWechat(token, code, "AN")
    }

}