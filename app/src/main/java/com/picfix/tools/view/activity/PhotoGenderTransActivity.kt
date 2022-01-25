package com.picfix.tools.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.bumptech.glide.Glide
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.picfix.tools.R
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.controller.LogReportManager
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import java.util.HashMap


class PhotoGenderTransActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var bigPic: ImageView
    private lateinit var firstPic: ImageView
    private lateinit var secondPic: ImageView
    private lateinit var camera: Button
    private lateinit var album: Button
    private var value = ""
    private var mCameraUri: Uri? = null

    private lateinit var firstLayout: FrameLayout
    private lateinit var secondLayout: FrameLayout

    override fun setLayout(): Int {
        return R.layout.a_photo_gender_trans
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        bigPic = findViewById(R.id.big_pic)
        firstPic = findViewById(R.id.first_pic)
        secondPic = findViewById(R.id.second_pic)

        firstLayout = findViewById(R.id.before_first_check)
        secondLayout = findViewById(R.id.before_second_check)

        back.setOnClickListener { finish() }

        camera = findViewById(R.id.open_camera)
        album = findViewById(R.id.open_album)

        camera.setOnClickListener { takePhoto() }
        album.setOnClickListener { chooseAlbum() }
        firstPic.setOnClickListener { choosePic(0) }
        secondPic.setOnClickListener { choosePic(1) }

    }

    override fun initData() {
        choosePic(0)

        LogReportManager.logReport("性别转换", "访问页面", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("visit", "operation")
    }

    private fun choosePic(index: Int) {
        when (index) {
            0 -> {
                firstLayout.setBackgroundResource(R.drawable.shape_rectangle_orange)
                secondLayout.setBackgroundResource(R.drawable.shape_corner_white)
                bigPic.setImageResource(R.drawable.iv_gender_trans_after_1)
            }
            1 -> {
                firstLayout.setBackgroundResource(R.drawable.shape_corner_white)
                secondLayout.setBackgroundResource(R.drawable.shape_rectangle_orange)
                bigPic.setImageResource(R.drawable.iv_gender_trans_after_2)
            }
        }
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

        LogReportManager.logReport("性别转换", "打开相册", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("open_album", "operation")
    }

    private fun toImagePage(uri: Uri) {
        val intent = Intent(this, ImageActivity::class.java)
        intent.putExtra("from", "gender_trans")
        intent.putExtra("uri", uri.toString())
        startActivity(intent)
    }

    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 5 * 1024 * 1024) {
            ToastUtil.show(this, "don't exceed 5MB")
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


    override fun onDestroy() {
        super.onDestroy()

    }

    private fun firebaseAnalytics(key: String, value: String) {
        val bundle = Bundle()
        bundle.putString(key, value)
        Firebase.analytics.logEvent("page_gender_conversion", bundle)

        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CONTENT] = "page_gender_conversion"
        if (key == "visit") {
            AppsFlyerLib.getInstance().logEvent(applicationContext, "page_gender_conversion", eventValues)
        } else {
            AppsFlyerLib.getInstance().logEvent(applicationContext, key, eventValues)
        }
    }

}