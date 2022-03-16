package com.piceasy.tools.view.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.CountDownTimer
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.baidu.mobads.action.ActionType
import com.baidu.mobads.action.BaiduAction
import com.bytedance.msdk.adapter.util.UIUtils
import com.bytedance.msdk.api.AdError
import com.bytedance.msdk.api.TTAdConstant
import com.bytedance.msdk.api.v2.GMMediationAdSdk
import com.bytedance.msdk.api.v2.GMSettingConfigCallback
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAd
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdListener
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdLoadCallback
import com.bytedance.msdk.api.v2.slot.GMAdSlotSplash
import com.piceasy.tools.R
import com.piceasy.tools.bean.UserInfo
import com.piceasy.tools.config.Constant
import com.piceasy.tools.http.loader.BaseLoader
import com.piceasy.tools.http.loader.ConfigLoader
import com.piceasy.tools.http.response.ResponseTransformer
import com.piceasy.tools.http.schedulers.SchedulerProvider
import com.piceasy.tools.utils.JLog
import com.piceasy.tools.utils.SplashUtils
import com.piceasy.tools.view.base.BaseActivity
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
@author ZW
@description:
@date : 2020/11/25 10:31
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private lateinit var textView: TextView
    private lateinit var splashBg: ImageView
    private lateinit var timer: CountDownTimer
    private var kv = MMKV.defaultMMKV()
    private var show = false
    private lateinit var mSplashContainer: FrameLayout
    private var mSettingConfigCallback = GMSettingConfigCallback {
        openAd()
    }

    override fun setLayout(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        textView = findViewById(R.id.splash_start)
        splashBg = findViewById(R.id.splash_bg)
        mSplashContainer = findViewById(R.id.splash_container)

        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }

        initTimer()
        initWxApi()

        launch(Dispatchers.IO) {
            initUserInfo()
            getConfig()
        }

    }


    override fun initData() {

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && !show) {
            val value = kv?.decodeBool("service_agree")
            if (value == null || !value) {
                val intent = Intent(this, AgreementActivity::class.java)
                intent.putExtra("index", 3)
                startActivityForResult(intent, 0x1)
                show = true
            } else {
                loadAdWithCallback()
                getConfig()
            }
        }
        super.onWindowFocusChanged(hasFocus)
    }

    private fun initUserInfo() {
        val userInfo = kv?.decodeParcelable("userInfo", UserInfo::class.java)
        if (userInfo != null) {
            JLog.i("userinfo is not null")
            Constant.CLIENT_TOKEN = userInfo.client_token
            Constant.USER_NAME = userInfo.nickname
            Constant.USER_ID = userInfo.id.toString()
            Constant.USER_ICON = userInfo.avatar
        } else {
            JLog.i("userinfo is null")
            getAccessToken()
        }
    }


    private fun initTimer() {
        timer = object : CountDownTimer(5 * 1000L, 1000) {
            override fun onFinish() {
                jumpTo()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
    }

    private fun initWxApi() {
        Constant.api = WXAPIFactory.createWXAPI(this, Constant.TENCENT_APP_ID, false)
        Constant.api.registerApp(Constant.TENCENT_APP_ID)

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Constant.api.registerApp(Constant.TENCENT_APP_ID)
            }
        }, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP))
    }

    private fun jumpTo() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    @SuppressLint("CheckResult")
    private fun getConfig() {
        launch(Dispatchers.IO) {
            ConfigLoader.getConfig()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.offcialSite != null) {
                        Constant.WEBSITE = it.offcialSite
                    }

                    if (it.testState != null) {
                        Constant.TEST = it.testState!!
                    }

                }, {

                })
        }
    }

    private fun getAccessToken() {
        launch(Dispatchers.IO) {
            if (Constant.PRODUCT_ID == "3") {
                BaseLoader.getToken(this@SplashActivity)
                    .compose(ResponseTransformer.handleResult())
                    .compose(SchedulerProvider.getInstance().applySchedulers())
                    .subscribe({
                        Constant.QUEST_TOKEN = it.questToken
                        visitLogin()
                    }, {

                    })
            } else {
                BaseLoader.getGoogleToken(this@SplashActivity)
                    .compose(ResponseTransformer.handleResult())
                    .compose(SchedulerProvider.getInstance().applySchedulers())
                    .subscribe({
                        Constant.QUEST_TOKEN = it.questToken
                        visitLogin()
                    }, {

                    })
            }
        }
    }

    private fun visitLogin() {
        launch(Dispatchers.IO) {
            BaseLoader.visitLogin()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    MMKV.defaultMMKV()?.encode("userInfo", it[0])

                    Constant.CLIENT_TOKEN = it[0].client_token
                    Constant.USER_NAME = it[0].nickname
                    Constant.USER_ID = it[0].id.toString()
                    Constant.USER_ICON = it[0].avatar

                    //active upload
                    if (Constant.OCPC) {
                        BaiduAction.logAction(ActionType.REGISTER)
                    }

                }, {

                })
        }
    }

    private fun loadAdWithCallback() {
        if (GMMediationAdSdk.configLoadSuccess()) {
            openAd()
        } else {
            GMMediationAdSdk.registerConfigCallback(mSettingConfigCallback)
        }
    }

    private fun openAd() {

        if (!Constant.AD_OPENNING) {
            timer.start()
            return
        }

        val adSlot = GMAdSlotSplash.Builder()
            .setImageAdSize(UIUtils.getScreenWidth(this), UIUtils.getScreenHeight(this))
            .setSplashPreLoad(true)//开屏gdt开屏广告预加载
            .setMuted(false) //声音开启
            .setVolume(1f)//admob 声音配置，与setMuted配合使用
            .setTimeOut(5000)//设置超时
            .setSplashButtonType(TTAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN)//合规设置，点击区域设置
            .setDownloadType(TTAdConstant.DOWNLOAD_TYPE_POPUP)//合规设置，下载弹窗
            .build()

        val networkRequestInfo = SplashUtils.getGMNetworkRequestInfo()

        val mTTSplashAd = GMSplashAd(this, "887719321")

        mTTSplashAd.setAdSplashListener(object : GMSplashAdListener {
            override fun onAdClicked() {
                JLog.i("ad is click")
            }

            override fun onAdShow() {
                JLog.i("ad is show")
                timer.start()
            }

            override fun onAdShowFail(p0: AdError) {
                JLog.i("ad is show fail")
                JLog.i("error = ${p0.message}")
            }

            override fun onAdSkip() {
                timer.cancel()
                jumpTo()
            }

            override fun onAdDismiss() {
            }
        })

        mTTSplashAd.loadAd(adSlot, networkRequestInfo, object : GMSplashAdLoadCallback {
            override fun onSplashAdLoadFail(p0: AdError) {
                JLog.i(p0.message)
                JLog.i(p0.code.toString())
                JLog.i(p0.thirdSdkErrorCode.toString())
                jumpTo()
            }

            override fun onSplashAdLoadSuccess() {
                JLog.i("success")
                if (Constant.AD_OPENNING) {
                    mSplashContainer.visibility = View.VISIBLE
                    mTTSplashAd.showAd(mSplashContainer)
                } else {
                    timer.start()
                }

            }

            override fun onAdLoadTimeout() {
                JLog.i("timeout")
                jumpTo()
            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            if (resultCode == 0x1) {
                kv?.encode("service_agree", true)
                loadAdWithCallback()
            }

            if (resultCode == 0x2) {
                kv?.encode("service_agree", true)
                loadAdWithCallback()
            }
        }
    }

}