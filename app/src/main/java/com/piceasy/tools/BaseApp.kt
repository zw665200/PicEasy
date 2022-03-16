package com.piceasy.tools

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.baidu.mobads.action.BaiduAction
import com.bytedance.msdk.api.v2.*
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.RetrofitServiceManager
import com.piceasy.tools.utils.AppUtil
import com.piceasy.tools.utils.JLog
import com.piceasy.tools.utils.RomUtil
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import com.piceasy.tools.config.Constant.REPORT_OPENNING


class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initData()
        initRom()
        initHttpRequest()
        initMMKV()
        initBaiduAction()
        initAppsFlyer()
        initBugly()
        initTTAd()
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

    private fun initBaiduAction() {
        //OPPO用户不激活OCPC
        if (Constant.OCPC) {
            System.loadLibrary("msaoaidsec")
            BaiduAction.init(this, Constant.USER_ACTION_SET_ID, Constant.APP_SECRET_KEY)
            BaiduAction.setActivateInterval(this, 7)
            BaiduAction.setPrintLog(true)
        }
    }

    private fun initAppsFlyer() {
        if (!REPORT_OPENNING) return

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

    private fun initTTAd() {
        val gmPrivacyConfig = object : GMPrivacyConfig() {
            override fun isCanUsePhoneState(): Boolean {
                return true
            }
        }

        val adConfig = GMAdConfig.Builder()
            .setAppId("5278559")
            .setAppName("PicEasy_android")
            .setDebug(true)
            .setPrivacyConfig(gmPrivacyConfig)
            .setPangleOption(
                GMPangleOption.Builder()
                    .setTitleBarTheme(GMAdConstant.TITLE_BAR_THEME_DARK)
                    .setIsUseTextureView(true)
                    .setAllowShowNotify(true)
                    .setAllowShowPageWhenScreenLock(true)
                    .setDirectDownloadNetworkType(GMAdConstant.NETWORK_STATE_WIFI, GMAdConstant.NETWORK_STATE_3G)
                    .build()
            )
            .build()

        GMMediationAdSdk.initialize(this, adConfig)

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