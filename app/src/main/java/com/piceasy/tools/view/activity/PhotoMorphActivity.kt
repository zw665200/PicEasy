package com.piceasy.tools.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.bumptech.glide.Glide
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.piceasy.tools.R
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.ImageManager
import com.piceasy.tools.controller.LogReportManager
import com.piceasy.tools.utils.ToastUtil
import com.piceasy.tools.view.base.BaseActivity
import java.util.HashMap


class PhotoMorphActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var bigPic: ImageView
    private lateinit var camera: Button
    private lateinit var album: Button
    private var value = ""
    private var mCameraUri: Uri? = null

    override fun setLayout(): Int {
        return R.layout.a_photo_morph
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        bigPic = findViewById(R.id.big_pic)

        back.setOnClickListener { finish() }

        camera = findViewById(R.id.open_camera)
        album = findViewById(R.id.open_album)

        camera.setOnClickListener { takePhoto() }
        album.setOnClickListener { chooseAlbum() }

    }

    override fun initData() {
        Glide.with(this).load(R.drawable.iv_sample_morph).into(bigPic)

        LogReportManager.logReport("人像渐变", "访问页面", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("visit", "operation")
    }

    private fun takePhoto() {
        ImageManager.checkPermission(this) { that ->
            if (that) {
                ImageManager.openCamera(this) {
                    mCameraUri = it
                }
            } else {
                ToastUtil.showShort(this, "请允许打开相机权限用于拍照")
            }
        }
    }

    private fun chooseAlbum() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, 0x1001)

        LogReportManager.logReport("人像渐变", "打开相册", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("open_album", "operation")
    }

    private fun toImagePage(uri: Uri) {
        val intent = Intent(this, ImageActivity::class.java)
        intent.putExtra("from", "morph")
        intent.putExtra("uri", uri.toString())
        startActivity(intent)
    }

    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 5 * 1024 * 1024) {
            ToastUtil.show(this, getString(R.string.exceed_5))
        } else {
            toImagePage(uri)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    checkFileSize(uri)
                }
            }
        }

        if (requestCode == Constant.CAMERA_REQUEST_CODE) {
            if (mCameraUri != null) {
                checkFileSize(mCameraUri!!)
            }
        }
    }

    private fun firebaseAnalytics(key: String, value: String) {
        if (!Constant.REPORT_OPENNING) return

        val bundle = Bundle()
        bundle.putString(key, value)
        Firebase.analytics.logEvent("page_portrait_gradient", bundle)

        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CONTENT] = "page_portrait_gradient"
        if (key == "visit") {
            AppsFlyerLib.getInstance().logEvent(applicationContext, "page_portrait_gradient", eventValues)
        } else {
            AppsFlyerLib.getInstance().logEvent(applicationContext, key, eventValues)
        }
    }


}