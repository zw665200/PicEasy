package com.picfix.tools

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.RomUtil
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import com.alipay.android.phone.mrpc.core.s


class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initData()
        initRom()
        initHttpRequest()
        initMMKV()
        initAppsFlyer()
        initBugly()
    }

    private fun initData() {
        if (AppUtil.isDebugger(this)) {
            Constant.isDebug = true
        }
    }

    /**
     * 读取设备信息
     *
     */
    private fun initRom() {
        val name = RomUtil.getName()
        if (name != null) {
            Constant.ROM = name
        } else {
            Constant.ROM = Constant.ROM_OTHER
        }
    }

    private fun initHttpRequest() {
        RetrofitServiceManager.getInstance().initRetrofitService()
    }


    /**
     * init mmkv
     */
    private fun initMMKV() {
        MMKV.initialize(this)
    }

    private fun initAppsFlyer() {
        AppsFlyerLib.getInstance().init(Constant.APPS_FLYER_KEY, null, this)
        AppsFlyerLib.getInstance().start(this, Constant.APPS_FLYER_KEY, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                JLog.i("Launch sent successfully, got 200 response code from server")
            }

            override fun onError(i: Int, s: String) {
                JLog.i("Launch failed to be sent:Error code: $i Error description: $s")
            }
        })
        AppsFlyerLib.getInstance().setDebugLog(true)
    }


    private fun initBugly() {
        Bugly.init(applicationContext, Constant.BUGLY_APPID, false)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.fontScale != 1.0f) {
            resources
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

}