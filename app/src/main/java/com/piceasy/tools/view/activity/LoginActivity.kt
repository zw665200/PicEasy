package com.piceasy.tools.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.piceasy.tools.R
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.LogReportManager
import com.piceasy.tools.http.loader.TokenLoader
import com.piceasy.tools.http.loader.UserInfoLoader
import com.piceasy.tools.http.response.ResponseTransformer
import com.piceasy.tools.http.schedulers.SchedulerProvider
import com.piceasy.tools.utils.*
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.tencent.mm.opensdk.modelmsg.SendAuth


class LoginActivity : FragmentActivity(), CoroutineScope by MainScope() {
    private lateinit var back: ImageView
    private lateinit var userAgreement: TextView
    private lateinit var privacyAgreement: TextView
    private lateinit var loginByGoogle: FrameLayout
    private lateinit var loginByWechat: FrameLayout
    private lateinit var agree: AppCompatCheckBox
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private var mmkv = MMKV.defaultMMKV()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_login)
        back = findViewById(R.id.iv_back)
        userAgreement = findViewById(R.id.user_agreement)
        privacyAgreement = findViewById(R.id.privacy_agreement)
        loginByGoogle = findViewById(R.id.sign_in_button)
        loginByWechat = findViewById(R.id.sign_in_wechat)
        agree = findViewById(R.id.agreement_check)

        back.setOnClickListener { finish() }
        loginByGoogle.setOnClickListener { signInByGoogle() }
        loginByWechat.setOnClickListener { signInByWechat() }
        userAgreement.setOnClickListener { toAgreementPage() }
        privacyAgreement.setOnClickListener { toAgreementPage() }

        initHandler()
        initGoogleLoginService()
    }

    private fun initHandler() {
        Constant.mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x1000 -> {
                        finish()
                    }
                }
            }
        }
    }

    private fun initGoogleLoginService() {
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()
    }

    private fun signInByWechat() {
        if (agree.isChecked) {
            openWechat()
        }
    }

    private fun openWechat() {
        if (AppUtil.checkPackageInfo(this, Constant.WX_PACK_NAME)) {
            val req = SendAuth.Req()
            req.scope = "snsapi_userinfo"
            req.state = "wechat_login"
            if (Constant.api != null) {
                Constant.api.sendReq(req)
            }
        } else {
            ToastUtil.showShort(this, "请安装微信")
        }
    }


    private fun signInByGoogle() {
        if (agree.isChecked) {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account == null) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, 0x1001)

//            oneTapClient.beginSignIn(signInRequest)
//                .addOnSuccessListener(this) {
//                    try {
//                        startIntentSenderForResult(
//                            it.pendingIntent.intentSender, 0x1001,
//                            null, 0, 0, 0
//                        )
//                    } catch (e: SendIntentException) {
//                        JLog.i("Couldn't start One Tap UI: " + e.localizedMessage)
//                    }
//                }.addOnFailureListener {
//                    ToastUtil.showShort(this, it.localizedMessage)
//                    JLog.i(it.localizedMessage)
//                }
//
            } else {
                val email = account.email
                val token = account.idToken
                val icon = account.photoUrl

                JLog.i("email = $email")
                JLog.i("token = $token")


//                if (token == null) {
                signOut()
//                } else {
//                    getAccessToken(token)
//                }
            }

//
//            oneTapClient.beginSignIn(signInRequest)
//                .addOnSuccessListener(this) { result ->
//                    try {
//                        startIntentSenderForResult(
//                            result.pendingIntent.intentSender, 0x1001,
//                            null, 0, 0, 0, null
//                        )
//                    } catch (e: IntentSender.SendIntentException) {
//                        JLog.i("Couldn't start One Tap UI: ${e.localizedMessage}")
//                    }
//                }
//                .addOnFailureListener(this) { e ->
//                    // No saved credentials found. Launch the One Tap sign-up flow, or
//                    // do nothing and continue presenting the signed-out UI.
//                    JLog.i(e.localizedMessage)
//                }
        } else {
            ToastUtil.showShort(this, getString(R.string.other_agree_privacy))
        }
    }


    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            JLog.i("logOut success")
            signInByGoogle()
        }
    }

    private fun toAgreementPage() {
        val intent = Intent(this, AgreementActivity::class.java)
        intent.putExtra("index", 2)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0x1001 -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        val email = account.email
                        val token = account.idToken
                        val icon = account.photoUrl

                        JLog.i("get email = $email")
                        JLog.i("get token = $token")

                        if (token != null) {
                            JLog.i("token = $token")
                            getAccessToken(token)
                        }
                    }

//                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
//                    val idToken = credential.googleIdToken
//                    val username = credential.id
//                    val password = credential.password
//                    when {
//                        idToken != null -> {
//                            // Got an ID token from Google. Use it to authenticate
//                            // with your backend.
//                            JLog.i("Got username.")
//                            JLog.i("Got ID token.")
//                            JLog.i("username = $username")
//                            JLog.i("token = $idToken")
//                            getAccessToken(idToken)
//                        }
//                        password != null -> {
//                            // Got a saved username and password. Use them to authenticate
//                            // with your backend.
//                            JLog.i("Got password.")
//                        }
//                        else -> {
//                            // Shouldn't happen.
//                            JLog.i("No ID token or password!")
//                        }
//                    }
                } catch (e: ApiException) {
                    JLog.i("signInResult:failed code = ${e.statusCode} , message = ${e.status}")
                    ToastUtil.showShort(this, getString(R.string.login_failed))
                }
            }
        }
    }

    private fun getAccessToken(code: String) {
        if (Constant.QUEST_TOKEN != "") {
            getUserInfo(code)
            return
        }

        launch(Dispatchers.IO) {
            TokenLoader.getToken(this@LoginActivity)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.QUEST_TOKEN = it.questToken
                    getUserInfo(code)
                }, {
                    ToastUtil.show(this@LoginActivity, "connection error")
                })
        }
    }

    @SuppressLint("CheckResult")
    private fun getUserInfo(code: String) {
        launch(Dispatchers.IO) {
            UserInfoLoader.getUser(Constant.QUEST_TOKEN, code)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.CLIENT_TOKEN = it.client_token
                    Constant.USER_NAME = it.nickname
                    Constant.USER_ID = it.id.toString()

                    mmkv?.encode("userInfo", it)

                    //report
                    LogReportManager.logReport("登录", "登录成功", LogReportManager.LogType.LOGIN)

                    if (Constant.REPORT_OPENNING) {
                        //firebase login
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.METHOD, "Google")
                        Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)

                        //appsflyer login
                        val eventValues = HashMap<String, Any>()
                        eventValues[AFInAppEventParameterName.CUSTOMER_USER_ID] = it.id
                        eventValues[AFInAppEventParameterName.CONTENT] = "Google"
                        AppsFlyerLib.getInstance().logEvent(applicationContext, AFInAppEventType.LOGIN, eventValues)
                    }

                    ToastUtil.showShort(this@LoginActivity, getString(R.string.login_success))

                    if (Constant.mHandler != null) {
                        Constant.mHandler.sendEmptyMessage(0x1000)
                    }

                    if (Constant.mSecondHandler != null) {
                        Constant.mSecondHandler.sendEmptyMessage(0x1000)
                    }

                    setResult(0x2000)
                    finish()

                }, {
                    JLog.i("error = ${it.message}")
                    signOut()
                })
        }
    }

}