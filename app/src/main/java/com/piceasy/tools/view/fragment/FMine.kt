package com.piceasy.tools.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.piceasy.tools.R
import com.piceasy.tools.adapter.DataAdapter
import com.piceasy.tools.bean.Resource
import com.piceasy.tools.bean.UserInfo
import com.piceasy.tools.callback.Callback
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.PayManager
import com.piceasy.tools.http.loader.AccountLoader
import com.piceasy.tools.http.response.ResponseTransformer
import com.piceasy.tools.http.schedulers.SchedulerProvider
import com.piceasy.tools.utils.FileUtil
import com.piceasy.tools.utils.JLog
import com.piceasy.tools.utils.ToastUtil
import com.piceasy.tools.view.activity.*
import com.piceasy.tools.view.base.BaseFragment
import com.piceasy.tools.view.views.AccountDeleteDialog
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_function.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class FMine : BaseFragment() {
    private lateinit var title: TextView
    private lateinit var level: TextView
    private lateinit var avatar: ImageView
    private lateinit var vipTitle: TextView
    private lateinit var vipDes: TextView
    private lateinit var logout: Button
    private lateinit var customer: RecyclerView
    private lateinit var buy: Button

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.a_mine, container, false)
        title = rootView.findViewById(R.id.tv_mine_nick)
        level = rootView.findViewById(R.id.tv_mine_vip)
        customer = rootView.findViewById(R.id.function)
        logout = rootView.findViewById(R.id.logout)
        buy = rootView.findViewById(R.id.buy)
        vipTitle = rootView.findViewById(R.id.vip_title)
        vipDes = rootView.findViewById(R.id.vip_des)
        avatar = rootView.findViewById(R.id.user_avatar)

        title.setOnClickListener { checkLogin() }
        logout.setOnClickListener { logOut() }
        buy.setOnClickListener { buy() }
        return rootView
    }

    override fun initData() {
        loadFunction()
        initHandler()
        loadUserInfo()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.color_light_white)
        }
    }

    override fun click(v: View?) {
    }

    private fun initHandler() {
        Constant.mSecondHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x1000 -> {
                        loadUserInfo()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserInfo() {
        if (Constant.USER_NAME != "") {
            title.text = Constant.USER_NAME
            level.text = "USER ID: ${Constant.USER_ID}"
            level.visibility = View.VISIBLE
            logout.visibility = View.VISIBLE
            if (Constant.USER_ICON != "") {
                Glide.with(this).load(Constant.USER_ICON).apply(RequestOptions.bitmapTransform(CircleCrop())).into(avatar)
            }
            PayManager.getInstance().getPayList {
                if (it.isNotEmpty()) {
                    for (order in it) {
                        if (order.server_code == Constant.PHOTO_FIX_TIMES) {
                            if (order.times != null) {
                                val times = order.times
                                if (times!! < 20) {
                                    vipTitle.text = getString(R.string.mine_dear_member)
                                    vipDes.text = "${20 - order.times!!} ${getString(R.string.mine_enjoy_times)}"
                                    buy.text = "VIP"
                                    level.visibility = View.VISIBLE
                                    return@getPayList
                                }
                            }
                        }

                        if (order.server_code == Constant.PHOTO_FIX || order.server_code.contains("subscription")) {
                            when (order.expire_type) {
                                else -> {
                                    vipTitle.text = getString(R.string.mine_dear_member)
                                    vipDes.text = getString(R.string.mine_unlimited_times)
                                    buy.text = "VIP"
                                    level.visibility = View.VISIBLE
                                    return@getPayList
                                }
                            }
                        }

                        if (order.server_code == Constant.PHOTO_MONTHLY_SUBSCRIPTION_CHINA
                            || order.server_code == Constant.PHOTO_SEASONALLY_SUBSCRIPTION_CHINA
                            || order.server_code == Constant.PHOTO_YEARLY_SUBSCRIPTION_CHINA
                            || order.server_code == Constant.PHOTO_PERMANENT_SUBSCRIPTION_CHINA
                        ) {
                            when (order.expire_type) {
                                else -> {
                                    vipTitle.text = order.server_name
                                    vipDes.text = getString(R.string.mine_unlimited_times)
                                    buy.text = "VIP"
                                    level.visibility = View.VISIBLE
                                    return@getPayList
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val userInfo = MMKV.defaultMMKV()?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.USER_NAME = userInfo.nickname
                level.text = "USER ID: ${Constant.USER_ID}"
                level.visibility = View.VISIBLE
                title.text = Constant.USER_NAME
                level.text = Constant.USER_ID
            }
        }
    }

    private fun checkLogin() {
        if (Constant.USER_NAME == "") {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        } else {
            title.text = Constant.USER_NAME
        }
    }

    private fun loadFunction() {
        val list = arrayListOf<Resource>()
        list.add(Resource("service", R.drawable.mine_help, getString(R.string.mine_service)))
        list.add(Resource("privacy", R.drawable.mine_privacy, getString(R.string.mine_privacy)))
        list.add(Resource("feedback", R.drawable.feedback, getString(R.string.mine_help)))
        list.add(Resource("clear", R.drawable.clear_cache, getString(R.string.setting_clear_cache)))
        list.add(Resource("about", R.drawable.about_us, getString(R.string.setting_about_us)))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_function)
            .addBindView { itemView, itemData, position ->
                itemView.function_icon.setImageResource(itemData.icon)
                itemView.function_name.text = itemData.name

                itemView.setOnClickListener {
                    when (position) {
//                        0 -> openWebsite()
                        0 -> openUserAgreement()
                        1 -> openPrivacyAgreement()
                        2 -> openFeedback()
                        3 -> clearCache()
                        4 -> aboutUs()
                    }
                }
            }
            .create()

        customer.layoutManager = LinearLayoutManager(requireActivity())
        customer.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, list.size)
    }

    private fun buy() {
        PayManager.getInstance().checkPay(requireActivity()) {
            if (!it) {
                startActivity(Intent(activity, PayActivity::class.java))
            }
        }
    }

    private fun openWebsite() {
        if (Constant.WEBSITE == "") return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constant.WEBSITE))
        startActivity(intent)
    }

    private fun openFeedback() {
        val intent = Intent(requireActivity(), FeedbackActivity::class.java)
        startActivity(intent)
    }

    private fun openUserAgreement() {
        val intent = Intent(requireActivity(), AgreementActivity::class.java)
        intent.putExtra("index", 0)
        startActivity(intent)
    }

    private fun openPrivacyAgreement() {
        val intent = Intent(requireActivity(), AgreementActivity::class.java)
        intent.putExtra("index", 1)
        startActivity(intent)
    }


    private fun clearCache() {
        launch(Dispatchers.IO) {
            FileUtil.clearAllCache(requireActivity())
        }

        launch(Dispatchers.Main) {
            ToastUtil.showShort(requireActivity(), "clear success")
        }

    }

    private fun aboutUs() {
        val intent = Intent(requireActivity(), AboutUsActivity::class.java)
        startActivity(intent)
    }

    private fun accountDelete() {
        if (Constant.USER_NAME != "") {
            AccountDeleteDialog(requireActivity(), object : Callback {
                override fun onSuccess() {
                    delete()
                }

                override fun onCancel() {
                }
            }).show()
        } else {
            checkLogin()
        }
    }

    private fun delete() {
        launch {
            AccountLoader.delete()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    logOut()
                }, {
                })
        }
    }

    private fun logOut() {
        if (Constant.USER_NAME != "") {
            Constant.USER_NAME = ""
            Constant.CLIENT_TOKEN = ""
            title.text = getString(R.string.mine_login)
            level.visibility = View.GONE
            logout.visibility = View.GONE
            avatar.setImageResource(R.drawable.mine_icon)

            vipTitle.text = getString(R.string.mine_update_to_member)
            vipDes.text = getString(R.string.mine_enjoy_more_benefits)
            buy.text = getString(R.string.mine_buy)

            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                mmkv.remove("userInfo")
            }
        }

        if (Constant.PRODUCT_ID != "3") {
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.your_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
            googleSignInClient.signOut().addOnCompleteListener {
                JLog.i("logOut success")
            }
        }
    }


}