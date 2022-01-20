package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AFInAppEventType.CONTENT_VIEW
import com.appsflyer.AppsFlyerLib
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.FileBean
import com.picfix.tools.bean.MenuResource
import com.picfix.tools.callback.DialogCallback
import com.picfix.tools.callback.PayCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.LogReportManager
import com.picfix.tools.controller.MediaPlayer
import com.picfix.tools.controller.PayManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.PaySuccessDialog
import com.picfix.tools.view.views.QuitDialog
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.heart_small.view.*
import kotlinx.android.synthetic.main.item_doc.view.*
import kotlinx.coroutines.*
import java.util.*

class PayActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var pay: Button

    private lateinit var menuBox: RecyclerView
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var bottomView: FrameLayout
    private lateinit var playBtn: ImageView

    private var currentproductId = ""
    private var currentPrice = 3.99f

    private var lastClickTime: Long = 0L

    private var kv: MMKV? = MMKV.defaultMMKV()
    private var orderSn = ""
    private var uri = "file:///android_asset/export.mp4"

    private lateinit var mAdapter: DataAdapter<MenuResource>
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

        back.setOnClickListener { onBackPressed() }
        playBtn.setOnClickListener { MediaPlayer.play(uri) }
        pay.setOnClickListener { checkPay(this) }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        }

        LogReportManager.logReport("支付页", "页面访问", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("visit", "operation")

    }


    override fun initData() {

        loadMenuBox()

        val width = AppUtil.getScreenWidth(this)
        val layout = playerView.layoutParams
        layout.width = width
        layout.height = 640 * width / 544
        playerView.layoutParams = layout

        val bottomParam = bottomView.layoutParams as FrameLayout.LayoutParams
        bottomParam.topMargin = 640 * width / 544 - AppUtil.dp2px(this, 50f)
        bottomView.layoutParams = bottomParam

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

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMenuBox() {
        val list = arrayListOf<MenuResource>()
        list.add(MenuResource("3.99USD", R.drawable.iv_definition, "Times", "use 10 times", "piceasy_ten_times"))
        list.add(MenuResource("28.99USD", R.drawable.iv_watermark, "Yearly", "use one year", "piceasy_one_year"))
        list.add(MenuResource("48.88USD", R.drawable.iv_cartoon, "Permanent", "use permanent", "piceasy_permanent"))

        mAdapter = DataAdapter.Builder<MenuResource>()
            .setData(list)
            .setLayoutId(R.layout.item_doc)
            .addBindView { itemView, itemData, position ->
                itemView.title.text = itemData.name
                itemView.price.text = itemData.type
                itemView.des.text = itemData.des

                if (position == currentPos) {
                    itemView.setBackgroundResource(R.drawable.shape_corner_blue)
                    itemView.point.setImageResource(R.drawable.point_orange)
                    itemView.title.setTextColor(Color.WHITE)
                    itemView.des.setTextColor(Color.WHITE)
                    itemView.price.setTextColor(Color.WHITE)
                    currentproductId = itemData.productId
                } else {
                    itemView.setBackgroundResource(R.drawable.shape_corner_white)
                    itemView.point.setImageResource(R.drawable.point_grey)
                    itemView.title.setTextColor(Color.BLACK)
                    itemView.des.setTextColor(Color.BLACK)
                    itemView.price.setTextColor(Color.BLACK)
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


    private fun checkPay(c: Activity) {

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            ToastUtil.showShort(c, "Please don't initiate payment frequently")
            return
        }

        lastClickTime = System.currentTimeMillis()

        when (currentproductId) {
            "piceasy_ten_times" -> currentPrice = 3.99f
            "piceasy_one_year" -> currentPrice = 28.99f
            "piceasy_permanent" -> currentPrice = 48.88f
        }

        doPay(c)

        LogReportManager.logReport("支付页", "发起支付$($currentPrice)", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("start_pay", "$${currentPrice}")
    }


    private fun doPay(c: Activity) {


        PayManager.getInstance().doGoogleFastPay(c, currentproductId, object : PayCallback {
            override fun success() {
                openPaySuccessDialog()
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


    private fun openPaySuccessDialog() {
        PaySuccessDialog(this@PayActivity, object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                setResult(0x100)
                finish()
            }

            override fun onCancel() {
            }
        }).show()
    }


    override fun onBackPressed() {
        QuitDialog(this, getString(R.string.quite_title), object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                LogReportManager.logReport("支付页", "退出页面", LogReportManager.LogType.OPERATION)
                firebaseAnalytics("quit_page", "operation")
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
        val bundle = Bundle()
        bundle.putString(key, value)
        Firebase.analytics.logEvent("page_payment", bundle)

        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CONTENT] = "page_payment"
        eventValues[AFInAppEventParameterName.CONTENT_ID] = key
        eventValues[AFInAppEventParameterName.CONTENT_TYPE] = value
        AppsFlyerLib.getInstance().logEvent(applicationContext, CONTENT_VIEW, eventValues)
    }


}