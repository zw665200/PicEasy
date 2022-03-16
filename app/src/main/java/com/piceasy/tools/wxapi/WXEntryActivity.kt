package com.piceasy.tools.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.LogReportManager
import com.piceasy.tools.http.loader.TokenLoader
import com.piceasy.tools.http.response.ResponseTransformer
import com.piceasy.tools.http.schedulers.SchedulerProvider
import com.piceasy.tools.utils.JLog
import com.piceasy.tools.utils.RomUtil
import com.piceasy.tools.utils.ToastUtil
import com.piceasy.tools.R
import com.piceasy.tools.http.loader.BaseLoader
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WXEntryActivity : Activity(), CoroutineScope by MainScope(), IWXAPIEventHandler {
    private var api: IWXAPI? = null
    private var mmkv = MMKV.defaultMMKV()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = Constant.api
        if (api == null) {
            api = WXAPIFactory.createWXAPI(this, Constant.TENCENT_APP_ID, false)
        }

        try {
            val intent = intent
            api!!.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api!!.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq) {
        when (req.type) {
            ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX -> {
            }
            ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX -> {
            }
            else -> {
            }
        }
        finish()
    }

    override fun onResp(resp: BaseResp) {
        val result = when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
                    paySuccess()
                } else {
                    getAuthSuccess(resp)
                }
                R.string.errcode_success
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> R.string.errcode_cancel
            BaseResp.ErrCode.ERR_AUTH_DENIED -> R.string.errcode_deny
            BaseResp.ErrCode.ERR_UNSUPPORT -> R.string.errcode_unsupported
            else -> R.string.errcode_unknown
        }

        JLog.i("response result = $result")

        finish()
    }

    private fun paySuccess() {
        JLog.i("weChat pay success")
    }

    private fun getAuthSuccess(resp: BaseResp) {
        JLog.i("get auth code success")
        val code = (resp as SendAuth.Resp).code
        JLog.i("code = $code")
        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        launch(Dispatchers.IO) {
            BaseLoader.getToken(this@WXEntryActivity)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.QUEST_TOKEN = it.questToken
                    getUserInfo(code)
                }, {
                    ToastUtil.show(this@WXEntryActivity, "获取验证失败，请检查网络")
                })
        }
    }

    private fun getUserInfo(code: String) {
        launch(Dispatchers.IO) {
            BaseLoader.getUserByWechat(Constant.QUEST_TOKEN, code)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    val userinfo = it
                    Constant.CLIENT_TOKEN = userinfo.client_token
                    Constant.USER_NAME = userinfo.nickname
                    Constant.USER_ID = userinfo.id.toString()

                    mmkv?.encode("userInfo", userinfo)

                    if (userinfo.user_type == 2) {
                        ToastUtil.showShort(this@WXEntryActivity, "登录失败")
                        return@subscribe
                    }

                    //activity
//                    when (AppUtil.getChannelId()) {
//                        Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
//                            val versionCode = AppUtil.getPackageVersionCode(this@WXEntryActivity, packageName)
//                            if (versionCode == Constant.APP_VERSION.toInt()) {
//                                val times = mmkv?.decodeString("activity_times")
//                                if (times == null) {
//                                    mmkv?.encode("activity_times", "true")
//                                }
//                            }
//                        }
//                    }

                    //active upload
                    if (Constant.OCPC) {
//                        BaiduAction.logAction(ActionType.REGISTER)

//                        val actionParam = JSONObject()
//                        actionParam.put(ActionParam.Key.PURCHASE_MONEY, 100)
//                        BaiduAction.logAction(ActionType.PURCHASE, actionParam)
                    }

                    //report
                    LogReportManager.logReport("登录", "登录成功", LogReportManager.LogType.LOGIN)

                    ToastUtil.showShort(this@WXEntryActivity, "登录成功")

                    if (Constant.mHandler != null) {
                        Constant.mHandler.sendEmptyMessage(0x1000)
                    }

                    if (Constant.mSecondHandler != null) {
                        Constant.mSecondHandler.sendEmptyMessage(0x1000)
                    }

                }, {
                    JLog.i("error = ${it.message}")
                })
        }
    }
}