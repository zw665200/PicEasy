package com.picfix.tools.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.controller.LogReportManager
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.MoveViewByViewDragHelper
import com.picfix.tools.view.views.TestView


class PhotoColourActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var bigPic: ImageView
    private lateinit var bigPicBefore: ImageView
    private lateinit var firstPic: ImageView
    private lateinit var secondPic: ImageView
    private lateinit var thirdPic: ImageView
    private lateinit var camera: Button
    private lateinit var album: Button
    private lateinit var pointer: MoveViewByViewDragHelper
    private lateinit var dynamicLayout: FrameLayout
    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<Uri>()
    private var value = ""
    private var mCameraUri: Uri? = null

    private lateinit var firstLayout: FrameLayout
    private lateinit var secondLayout: FrameLayout
    private lateinit var thirdLayout: FrameLayout

    override fun setLayout(): Int {
        return R.layout.a_photo_colour
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        bigPic = findViewById(R.id.big_pic)
        bigPicBefore = findViewById(R.id.big_pic_before)
        firstPic = findViewById(R.id.first_pic)
        secondPic = findViewById(R.id.second_pic)
        thirdPic = findViewById(R.id.third_pic)
        dynamicLayout = findViewById(R.id.dynamic_layout)
        pointer = findViewById(R.id.point_move)

        firstLayout = findViewById(R.id.before_first_check)
        secondLayout = findViewById(R.id.before_second_check)
        thirdLayout = findViewById(R.id.before_third_check)

        back.setOnClickListener { finish() }

        camera = findViewById(R.id.open_camera)
        album = findViewById(R.id.open_album)

        camera.setOnClickListener { takePhoto() }
        album.setOnClickListener { chooseAlbum() }
        firstPic.setOnClickListener { choosePic(0) }
        secondPic.setOnClickListener { choosePic(1) }
        thirdPic.setOnClickListener { choosePic(2) }

    }

    override fun initData() {
        choosePic(0)

        LogReportManager.logReport("图片上色", "访问页面", LogReportManager.LogType.OPERATION)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val width = bigPic.width
            val height = bigPic.height

            val layoutParam = bigPicBefore.layoutParams
            layoutParam.width = width
            layoutParam.height = height
            bigPicBefore.layoutParams = layoutParam

            val dynamicLayoutParam = dynamicLayout.layoutParams
            dynamicLayoutParam.width = width / 2
            dynamicLayoutParam.height = height
            dynamicLayout.layoutParams = dynamicLayoutParam

            pointer.setLayout(dynamicLayout, width / 2)
        }

    }

    private fun choosePic(index: Int) {
        when (index) {
            0 -> {
                firstLayout.setBackgroundResource(R.drawable.shape_rectangle_orange)
                secondLayout.setBackgroundResource(R.drawable.shape_corner_white)
                thirdLayout.setBackgroundResource(R.drawable.shape_corner_white)
                bigPic.setImageResource(R.drawable.ic_colour_after_1)
                bigPicBefore.setImageResource(R.drawable.ic_colour_before_1)
            }
            1 -> {
                firstLayout.setBackgroundResource(R.drawable.shape_corner_white)
                secondLayout.setBackgroundResource(R.drawable.shape_rectangle_orange)
                thirdLayout.setBackgroundResource(R.drawable.shape_corner_white)
                bigPic.setImageResource(R.drawable.ic_colour_after_2)
                bigPicBefore.setImageResource(R.drawable.ic_colour_before_2)
            }
            2 -> {
                firstLayout.setBackgroundResource(R.drawable.shape_corner_white)
                secondLayout.setBackgroundResource(R.drawable.shape_corner_white)
                thirdLayout.setBackgroundResource(R.drawable.shape_rectangle_orange)
                bigPic.setImageResource(R.drawable.ic_colour_after_3)
                bigPicBefore.setImageResource(R.drawable.ic_colour_after_3)
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

        LogReportManager.logReport("图片上色", "打开相册", LogReportManager.LogType.OPERATION)
    }

    private fun toImagePage(uri: Uri) {
        val intent = Intent(this, ImageActivity::class.java)
        intent.putExtra("from", "colour")
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

        mList.clear()
        uploadList.clear()

    }

}