package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.widget.ImageView
import android.widget.TextView
import com.picfix.tools.R
import com.picfix.tools.bean.UserInfo
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.DBManager
import com.picfix.tools.http.loader.ConfigLoader
import com.picfix.tools.http.loader.ServiceListLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.d_pics.*
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/**
@author ZW
@description:
@date : 2020/11/25 10:31
 */
class SplashActivity : BaseActivity() {
    private lateinit var textView: TextView
    private lateinit var splashBg: ImageView
    private lateinit var timer: CountDownTimer
    private var kv = MMKV.defaultMMKV()
    private var show = false

    override fun setLayout(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        textView = findViewById(R.id.splash_start)
        splashBg = findViewById(R.id.splash_bg)

        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }

        initUserInfo()
        initTimer()
        clearDatabase()
        getConfig()
        getServiceList()
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

                timer.start()
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
        }else{
            JLog.i("userinfo is null")
        }
    }


    private fun initTimer() {
        timer = object : CountDownTimer(3 * 1000L, 1000) {
            override fun onFinish() {
                jumpTo()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
    }

    private fun jumpTo() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearDatabase() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(this@SplashActivity)
        }
    }


    @SuppressLint("CheckResult")
    private fun getConfig() {
        thread {
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

    @SuppressLint("CheckResult")
    private fun getServiceList() {
        thread {
            ServiceListLoader.getServiceList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        for (child in it) {
                            //save service list
                            MMKV.defaultMMKV()?.encode(child.server_code + child.expire_type, child)
                        }
                    }
                }, {
                    ToastUtil.show(this@SplashActivity, "获取服务列表失败")
                })
        }
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
                timer.start()
            }

            if (resultCode == 0x2) {
                kv?.encode("service_agree", true)
                timer.start()
            }
        }
    }

}