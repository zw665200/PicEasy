package com.piceasy.tools.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.baidu.mobads.action.ActionParam
import com.baidu.mobads.action.ActionType
import com.baidu.mobads.action.BaiduAction
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.piceasy.tools.R
import com.piceasy.tools.adapter.DataAdapter
import com.piceasy.tools.bean.FileBean
import com.piceasy.tools.bean.MenuResource
import com.piceasy.tools.bean.UserInfo
import com.piceasy.tools.callback.Callback
import com.piceasy.tools.callback.DialogCallback
import com.piceasy.tools.callback.PayCallback
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.LogReportManager
import com.piceasy.tools.controller.MediaPlayer
import com.piceasy.tools.controller.PayManager
import com.piceasy.tools.utils.AppUtil
import com.piceasy.tools.utils.JLog
import com.piceasy.tools.utils.ToastUtil
import com.piceasy.tools.view.base.BaseActivity
import com.piceasy.tools.view.views.PaySuccessDialog
import com.piceasy.tools.view.views.QuitDialog
import com.piceasy.tools.view.views.TermsPop
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_doc.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class PayActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var pay: Button

    private lateinit var menuBox: RecyclerView
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var bottomView: FrameLayout
    private lateinit var playBtn: ImageView
    private lateinit var terms: TextView

    private var currentProductId = ""
    private var currentProductType = "acknowledge"
    private var currentPrice = 3.99f

    private var lastClickTime: Long = 0L
    private var isShow = false

    private var orderSn = ""
    private var uri = "file:///android_asset/export_chinese.mp4"

    private lateinit var mAdapter: DataAdapter<MenuResource>
    private var mList = mutableListOf<MenuResource>()
    private var currentPos = 0

    override fun setLayout(): Int {
        return R.layout.a_fix_pay
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        back = findViewById(R.id.iv_back)
        pay = findViewById(R.id.do_pay)
        menuBox = findViewById(R.id.price_list)
        playerView = findViewById(R.id.player)
        bottomView = findViewById(R.id.bottom_view)
        playBtn = findViewById(R.id.play)
        terms = findViewById(R.id.terms)

        back.setOnClickListener { onBackPressed() }
        playBtn.setOnClickListener { MediaPlayer.play(uri) }
        pay.setOnClickListener { checkPay(this, "alipay") }
        terms.setOnClickListener { showPop() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        }

        LogReportManager.logReport("支付页", "页面访问", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("visit", "operation")

    }


    override fun initData() {

        loadMenuBox()
        getServicePriceList()

        val width = AppUtil.getScreenWidth(this)
        val layout = playerView.layoutParams
        layout.width = width
        layout.height = 640 * width / 520
        playerView.layoutParams = layout

        val bottomParam = bottomView.layoutParams as FrameLayout.LayoutParams
        bottomParam.topMargin = 640 * width / 520 - AppUtil.dp2px(this, 70f)
        bottomView.layoutParams = bottomParam

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !isShow) {
            player = MediaPlayer.getPlayer(this)
            playerView.player = player

            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE -> {
                            JLog.i("IDLE")
                        }

                        Player.STATE_BUFFERING -> {
                            JLog.i("BUFFRING")
                        }

                        Player.STATE_READY -> {
                            JLog.i("READY")
                        }

                        Player.STATE_ENDED -> {
                            JLog.i("END")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    JLog.i("error = ${error.errorCode}")
                    JLog.i("error = ${error.errorCodeName}")
                    JLog.i("error = ${error.message}")
//                MediaPlayer.release()
                }
            }

            player.addListener(listener)
            MediaPlayer.play(uri)
            isShow = true
        }
    }


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun loadMenuBox() {
        mAdapter = DataAdapter.Builder<MenuResource>()
            .setData(mList)
            .setLayoutId(R.layout.item_doc)
            .addBindView { itemView, itemData, position ->
                itemView.title.text = itemData.name
                itemView.des.text = itemData.des

                if (position == currentPos) {
                    itemView.setBackgroundResource(R.drawable.shape_rectangle_yellow)
                    itemView.point.setImageResource(R.drawable.point_orange)
                    itemView.des.setTextColor(ResourcesCompat.getColor(resources, R.color.color_orange, null))
                    currentProductId = itemData.productId
                } else {
                    itemView.setBackgroundResource(R.drawable.shape_corner_white)
                    itemView.point.setImageResource(R.drawable.point_grey)
                    itemView.des.setTextColor(ResourcesCompat.getColor(resources, R.color.color_dark_grey, null))
                }

                if (itemData.productId == Constant.PHOTO_SUBSCRIPTION || itemData.productId == Constant.PHOTO_MONTHLY_SUBSCRIPTION
                    || itemData.productCode == Constant.PHOTO_MONTHLY_SUBSCRIPTION_CHINA
                    || itemData.productCode == Constant.PHOTO_PERMANENT_SUBSCRIPTION_CHINA
                ) {
                    itemView.per.visibility = View.GONE
                }

                if (itemData.productId == Constant.PHOTO_SEASONALLY_SUBSCRIPTION) {
                    itemView.per.text = getString(R.string.pay_item_des_10)
                }

                if (itemData.productCode == Constant.PHOTO_SEASONALLY_SUBSCRIPTION_CHINA) {
                    itemView.per.text = "￥${itemData.type.toFloat().div(3).toInt()}/月"
                }

                if (itemData.productCode == Constant.PHOTO_YEARLY_SUBSCRIPTION_CHINA) {
                    itemView.per.text = "￥${itemData.type.toFloat().div(12).toInt()}/月"
                }

                if (itemData.productId == Constant.PHOTO_YEARLY_SUBSCRIPTION) {
                    itemView.per.text = getString(R.string.pay_item_des_11)
                }

                if (itemData.productId == Constant.PHOTO_SUBSCRIPTION || itemData.productId == Constant.PHOTO_MONTHLY_SUBSCRIPTION
                    || itemData.productId == Constant.PHOTO_SEASONALLY_SUBSCRIPTION || itemData.productId == Constant.PHOTO_YEARLY_SUBSCRIPTION
                ) {
                    itemView.price.text = "$${itemData.type}"
                } else {
                    itemView.price.text = "￥${itemData.type}"
                }

                itemView.setOnClickListener {
                    currentPos = position
                    mAdapter.notifyDataSetChanged()
                }

            }
            .create()

        menuBox.layoutManager = LinearLayoutManager(this)
        menuBox.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun getServicePriceList() {

        PayManager.getInstance().getServiceList(this) {

            val packDetails = arrayListOf<MenuResource>()
            for (child in it) {
                when (child.server_code) {
                    Constant.PHOTO_MONTHLY_SUBSCRIPTION_CHINA -> {
                        val menuResource = MenuResource(
                            child.server_price,
                            child.sale_price,
                            getString(R.string.pay_item_title_1),
                            "首月￥${child.sale_price}",
                            child.id.toString(),
                            child.server_code
                        )
                        packDetails.add(menuResource)
                    }

                    Constant.PHOTO_SEASONALLY_SUBSCRIPTION_CHINA -> {
                        val menuResource = MenuResource(
                            child.server_price,
                            child.sale_price,
                            getString(R.string.pay_item_title_2),
                            "首季度￥${child.sale_price}",
                            child.id.toString(),
                            child.server_code
                        )
                        packDetails.add(menuResource)
                    }

                    Constant.PHOTO_YEARLY_SUBSCRIPTION_CHINA -> {
                        val menuResource = MenuResource(
                            child.server_price,
                            child.sale_price,
                            getString(R.string.pay_item_title_3),
                            "首年￥${child.sale_price}",
                            child.id.toString(),
                            child.server_code
                        )
                        packDetails.add(menuResource)
                    }

                    Constant.PHOTO_PERMANENT_SUBSCRIPTION_CHINA -> {
                        val menuResource = MenuResource(
                            child.server_price,
                            child.sale_price,
                            "永久",
                            "￥${child.sale_price}终身",
                            child.id.toString(),
                            child.server_code
                        )
                        packDetails.add(menuResource)
                    }
                }
            }

            mList.addAll(packDetails)
            mAdapter.notifyDataSetChanged()
        }

//        PayManager.getInstance().getServiceMenuList(this) {
//            if (it.paid) {
//                return@getServiceMenuList
//            }
//
//            val packDetails = arrayListOf<MenuResource>()
//            for (child in it.serverList) {
//        when (child.server_code) {
//            Constant.PHOTO_SUBSCRIPTION -> {
//                JLog.i("0.99")
//                val menuResource = MenuResource(
//                    child.sale_price,
//                    child.sale_price,
//                    getString(R.string.pay_item_title_1),
//                    getString(R.string.pay_item_des_2),
//                    child.server_code
//                )
//                packDetails.add(menuResource)
//            }
//
//                    Constant.PHOTO_MONTHLY_SUBSCRIPTION -> {
//                        JLog.i("monthly")
//                        val menuResource = MenuResource(
//                            child.sale_price,
//                            child.sale_price,
//                            getString(R.string.pay_item_title_1),
//                            getString(R.string.pay_item_des_1),
//                            child.server_code
//                        )
//                        packDetails.add(menuResource)
//                    }
//
//                    Constant.PHOTO_SEASONALLY_SUBSCRIPTION -> {
//                        JLog.i("seasonally")
//                        val menuResource = MenuResource(
//                            child.sale_price,
//                            child.sale_price,
//                            getString(R.string.pay_item_title_2),
//                            getString(R.string.pay_item_des_3),
//                            child.server_code
//                        )
//                        packDetails.add(menuResource)
//                    }
//
//                    Constant.PHOTO_YEARLY_SUBSCRIPTION -> {
//                        JLog.i("yearly")
//                        val menuResource = MenuResource(
//                            child.sale_price,
//                            child.sale_price,
//                            getString(R.string.pay_item_title_3),
//                            getString(R.string.pay_item_des_4),
//                            child.server_code
//                        )
//                        packDetails.add(menuResource)
//                    }
//                }
//            }
//
//            mList.addAll(packDetails)
//            mAdapter.notifyDataSetChanged()
//
//        }
    }


    private fun checkPay(c: Activity, type: String) {

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            ToastUtil.showShort(c, "Please don't initiate payment frequently")
            return
        }

        lastClickTime = System.currentTimeMillis()

        val userInfo = MMKV.defaultMMKV()?.decodeParcelable("userInfo", UserInfo::class.java)
        if (userInfo != null) {
            val userType = userInfo.user_type
            if (userType == 2) {
                startActivityForResult(Intent(this, LoginActivity::class.java), 0x2000)
                return
            }
        }

        when (currentProductId) {
            "three_days_free_subscription", "piceasy_subscription" -> currentPrice = 3.99f
            "piceasy_subscription_season" -> currentPrice = 11.88f
            "piceasy_subscription_yearly" -> currentPrice = 47.88f
        }

        doPay(c, type)

        LogReportManager.logReport("支付页", "发起支付$($currentPrice)", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("start_pay", "$${currentPrice}")
    }


    private fun doPay(c: Activity, type: String) {
        when (type) {
            "google" -> {
                PayManager.getInstance().doGoogleFastPay(c, currentProductId, currentProductType, object : PayCallback {
                    override fun success() {
                        openPaySuccessDialog()

                        if (!Constant.REPORT_OPENNING) {
                            //firebase pay
                            val bundle = Bundle()
                            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
                            bundle.putFloat(FirebaseAnalytics.Param.VALUE, currentPrice)
                            bundle.putString(FirebaseAnalytics.Param.AFFILIATION, "Google Play")
                            Firebase.analytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)

                            //appsFlyer pay
                            val eventValues = HashMap<String, Any>()
                            eventValues[AFInAppEventParameterName.PURCHASE_CURRENCY] = "USD"
                            eventValues[AFInAppEventParameterName.REVENUE] = currentPrice
                            eventValues[AFInAppEventParameterName.CUSTOMER_USER_ID] = Constant.USER_ID
                            eventValues[AFInAppEventParameterName.CONTENT_ID] = "Google Play"
                            eventValues[AFInAppEventParameterName.CONTENT_TYPE] = "in_app_purchase"
                            AppsFlyerLib.getInstance().logEvent(applicationContext, AFInAppEventType.PURCHASE, eventValues)
                        }
                    }

                    override fun progress(orderId: String) {
                        orderSn = orderId
                    }

                    override fun failed(msg: String) {
                        launch(Dispatchers.Main) {
                            ToastUtil.showShort(c, msg)
                            firebaseAnalytics("pay_cancel", "operation")
                        }
                    }
                })
            }

            "alipay" -> {
                PayManager.getInstance().doAliPay(c, currentProductId.toInt(), object : PayCallback {
                    override fun success() {
                        launch(Dispatchers.Main) {

                            //支付成功
                            ToastUtil.showShort(c, "支付成功")
                            openPaySuccessDialog()

                            if (Constant.OCPC) {
                                val actionParam = JSONObject()
                                actionParam.put(ActionParam.Key.PURCHASE_MONEY, currentPrice * 100)
                                BaiduAction.logAction(ActionType.PURCHASE, actionParam)
                            }

                            //根据套餐判断是否跳转到补价页面
//                            if (currentServiceId == secondServiceId) {
//                                toPaySuccessPage()
//                            } else {
//                                openPaySuccessDialog()
//                            }

                        }
                    }

                    override fun progress(orderId: String) {
                        orderSn = orderId
                    }

                    override fun failed(msg: String) {
                        launch(Dispatchers.Main) {
                            ToastUtil.showShort(c, msg)
                        }
                    }
                })
            }
        }

    }


    private fun openPaySuccessDialog() {
        PaySuccessDialog(this@PayActivity, object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                setResult(0x100)
                finish()
            }

            override fun onCancel() {
                setResult(0x100)
                finish()
            }
        }).show()
    }

    private fun showPop() {
        TermsPop(this, object : Callback {
            override fun onSuccess() {
                val intent = Intent(this@PayActivity, AgreementActivity::class.java)
                intent.putExtra("index", 1)
                startActivity(intent)
            }

            override fun onCancel() {
            }
        }).showPopupWindow(bottomView)
    }


    override fun onBackPressed() {
        QuitDialog(this, getString(R.string.quite_title), object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                LogReportManager.logReport("支付页", "退出页面", LogReportManager.LogType.OPERATION)
                firebaseAnalytics("quit_pay", "operation")
                finish()
            }

            override fun onCancel() {
            }
        }).show()
    }

    override fun onStop() {
        super.onStop()
        MediaPlayer.stop()
    }


    private fun firebaseAnalytics(key: String, value: String) {
        if (!Constant.REPORT_OPENNING) return

        val bundle = Bundle()
        bundle.putString(key, value)
        Firebase.analytics.logEvent("page_payment", bundle)

        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CONTENT] = "page_payment"
        if (key == "visit") {
            AppsFlyerLib.getInstance().logEvent(applicationContext, "page_payment", eventValues)
        } else {
            AppsFlyerLib.getInstance().logEvent(applicationContext, key, eventValues)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x2000) {
            PayManager.getInstance().checkPay(this) {
                if (it) {
                    ToastUtil.showShort(this, "检测到您是付费用户")
                    finish()
                }
            }
        }
    }

}