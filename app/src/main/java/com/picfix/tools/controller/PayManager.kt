package com.picfix.tools.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.alipay.sdk.app.PayTask
import com.android.billingclient.api.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.picfix.tools.R
import com.picfix.tools.bean.*
import com.picfix.tools.callback.PayCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.config.WalletConstant.LOAD_PAYMENT_DATA_REQUEST_CODE
import com.picfix.tools.config.WalletConstant.SHIPPING_COST_CENTS
import com.picfix.tools.http.loader.*
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.*
import com.picfix.tools.view.activity.LoginActivity
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class PayManager private constructor() : CoroutineScope by MainScope() {
    private lateinit var garmentList: JSONArray
    private lateinit var selectedGarment: JSONObject
    private lateinit var billingClient: BillingClient

    companion object {

        @Volatile
        private var instance: PayManager? = null

        fun getInstance(): PayManager {
            if (instance == null) {
                synchronized(PayManager::class) {
                    if (instance == null) {
                        instance = PayManager()
                    }
                }
            }

            return instance!!
        }
    }

    /**
     * 检查修复套餐
     * @param activity
     * @param result
     */
    fun checkFixPay(activity: Activity, result: (Boolean) -> Unit) {

        val mmkv = MMKV.defaultMMKV()
        when (AppUtil.getChannelId()) {
            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                val times = mmkv?.decodeInt("activity_times")
                if (times == 1) {
                    result(true)
                    return
                }
            }
        }

        if (Constant.CLIENT_TOKEN == "") {
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        getPayStatus(activity, Constant.PHOTO_FIX + Constant.EXPIRE_TYPE_FOREVER) {
            if (it.serverExpire == 0) {
                result(true)
            } else {
                result(false)
            }
        }
    }

    /**
     * 检查套餐
     * @param context
     * @param result
     */
    fun checkPay(activity: Activity, result: (Boolean) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        getPayStatus { result(it) }
    }


    fun getPayList(result: (List<Order>) -> Unit) {

        if (Constant.CLIENT_TOKEN == "") return

        launch(Dispatchers.IO) {
            OrderLoader.getOrders()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it != null) {
                        result(it)
                    }
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun getServiceList(activity: Activity, result: (List<Price>) -> Unit) {
        ServiceListLoader.getServiceList()
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({
                if (it.isNotEmpty()) {
                    result(it)
                }
            }, {
                ToastUtil.show(activity, "获取服务列表失败")
            })
    }

    private fun getPayStatus(result: (Boolean) -> Unit) {
        launch(Dispatchers.IO) {
            OrderLoader.getOrders()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (!it.isNullOrEmpty()) {
                        result(true)
                    } else {
                        result(false)
                    }
                }, {
                    JLog.i("getPayStatus error")
                })
        }
    }

    @SuppressLint("CheckResult")
    fun getPayStatus(activity: Activity, serviceCode: String, success: (PayStatus) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        val service = MMKV.defaultMMKV()?.decodeParcelable(serviceCode, Price::class.java)
        if (service != null) {
            thread {
                PayStatusLoader.getPayStatus(service.id, Constant.CLIENT_TOKEN)
                    .compose(ResponseTransformer.handleResult())
                    .compose(SchedulerProvider.getInstance().applySchedulers())
                    .subscribe({
                        success(it)
                    }, {
                    })
            }
        }
    }

    fun doGooglePay(activity: Activity, callback: PayCallback) {
        selectedGarment = fetchRandomGarment(activity)

        val paymentsClient = PaymentsUtil.createPaymentsClient(activity)
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val req = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(req)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                // Process error
                JLog.i(exception.toString())
                callback.failed(exception.toString())
            }
        }

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val garmentPrice = selectedGarment.getDouble("price")
        val priceCents = (garmentPrice * PaymentsUtil.CENTS.toLong()).roundToInt() + SHIPPING_COST_CENTS

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents)
        if (paymentDataRequestJson == null) {
            JLog.i("Can't fetch payment data request")
            callback.failed("Can't fetch payment data request")
            return
        }

        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(request), activity, LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    fun doGoogleFastPay(activity: Activity, productId: String, callback: PayCallback) {
        val listener = PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    JLog.i("pay success")
                    if (purchases != null) {
                        for (purchase in purchases) {

                            //consume purchase
                            val consumeParams =
                                ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()

                            billingClient.consumeAsync(
                                consumeParams
                            ) { BillingResult, _ ->
                                when (BillingResult.responseCode) {
                                    BillingClient.BillingResponseCode.OK -> {
                                        val packName = purchase.packageName
                                        val purchaseToken = purchase.purchaseToken

                                        JLog.i("packName = $packName")
                                        JLog.i("purchaseToken = $purchaseToken")

                                        googlePayOrderValidate(packName, productId, purchaseToken, callback)
                                    }
                                }
                            }
                        }
                    }
                }

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    callback.failed("user cancel")
                }

                else -> {
                    JLog.i("unknown message")
                }
            }
        }

        billingClient = BillingClient.newBuilder(activity)
            .setListener(listener)
            .enablePendingPurchases()
            .build()


        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                JLog.i("onBillingServiceDisconnected")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    JLog.i("onBillingSetupFinished")

                    val skuList = ArrayList<String>()
                    skuList.add(productId)

                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

                    // leverage querySkuDetails Kotlin extension function
                    billingClient.querySkuDetailsAsync(
                        params.build()
                    ) { _, skuDetailsList ->
                        if (skuDetailsList.isNullOrEmpty()) {
                            callback.failed("no goods found")
                            return@querySkuDetailsAsync
                        }

                        for (skuDetails in skuDetailsList) {

                            //purse
                            activity.runOnUiThread {
                                val flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()

                                val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
                                if (responseCode == BillingClient.BillingResponseCode.OK) {
                                    JLog.i("openPay success")
                                }
                            }
                        }
                    }
                } else {
                    JLog.i("error code = ${p0.responseCode},${p0.debugMessage}")
                    when (p0.responseCode) {
                        3 -> callback.failed("Unsupported country or region")
                    }
                }
            }
        })
    }


    /**
     * 支付宝支付
     */
    @SuppressLint("CheckResult")
    fun doAliPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            AliPayLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkOrderStatus(activity, it, callback)
                }, {
//                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    /**
     * 人工修图支付宝支付
     */
    @SuppressLint("CheckResult")
    fun doAtfPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            AtfPayLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkOrderStatus(activity, it, callback)
                }, {
//                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    private fun checkOrderStatus(activity: Activity, order: AlipayParam, callback: PayCallback) {
        launch(Dispatchers.IO) {
            JLog.i("param = ${order.body}")
            JLog.i("orderSn = ${order.orderSn}")

            val task = PayTask(activity)
            val result = task.payV2(order.body, true)
            val res = PayResult(result)
            val resultStatus = res.resultStatus

            if (resultStatus == "9000") {
                JLog.i("alipay success")

                callback.progress(order.orderSn)
                callback.success()

            } else {
                //支付失败，也需要发起服务端校验
                JLog.i("alipay failed")

                callback.failed("已取消")
            }
        }

    }


    private fun fetchRandomGarment(activity: Activity): JSONObject {

        if (!::garmentList.isInitialized) {
            garmentList = Json.readFromResources(activity, R.raw.tshirts)
        }

        val randomIndex: Int = (Math.random() * (garmentList.length() - 1)).roundToInt()
        return garmentList.getJSONObject(randomIndex)
    }

    /**
     * 支付宝支付
     */
    @SuppressLint("CheckResult")
    fun googlePayOrderValidate(packName: String, productId: String, purchaseToken: String, callback: PayCallback) {
        thread {
            GooglePayLoader.googlePayValidate(packName, productId, purchaseToken)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.paied) {
                        callback.success()
                    }
                }, {
                })
        }
    }

}