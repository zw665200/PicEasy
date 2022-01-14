package com.picfix.tools.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.picfix.tools.R
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.MoveViewByViewDragHelper


class PhotoDefinitionActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var bigPic: ImageView
    private lateinit var bigPicBefore: ImageView
    private lateinit var firstPic: ImageView
    private lateinit var secondPic: ImageView
    private lateinit var camera: Button
    private lateinit var album: Button
    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<Uri>()
    private lateinit var dynamicLayout: FrameLayout
    private lateinit var pointer: MoveViewByViewDragHelper
    private var value = ""
    private var mCameraUri: Uri? = null
    private var mCreateUri: Uri? = null

    private lateinit var firstLayout: FrameLayout
    private lateinit var secondLayout: FrameLayout

    override fun setLayout(): Int {
        return R.layout.a_photo_definition
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        bigPic = findViewById(R.id.big_pic)
        bigPicBefore = findViewById(R.id.big_pic_before)
        firstPic = findViewById(R.id.first_pic)
        secondPic = findViewById(R.id.second_pic)
        dynamicLayout = findViewById(R.id.dynamic_layout)
        pointer = findViewById(R.id.point_move)

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
                bigPic.setImageResource(R.drawable.iv_definition_after_1)
                bigPicBefore.setImageResource(R.drawable.iv_definition_before_1)
            }
            1 -> {
                firstLayout.setBackgroundResource(R.drawable.shape_corner_white)
                secondLayout.setBackgroundResource(R.drawable.shape_rectangle_orange)
                bigPic.setImageResource(R.drawable.iv_definition_after_2)
                bigPicBefore.setImageResource(R.drawable.iv_definition_before_2)
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
    }

    private fun toImagePage(uri: Uri) {
        val intent = Intent(this, ImageActivity::class.java)
        intent.putExtra("from", "definition")
        intent.putExtra("uri", uri.toString())
        startActivity(intent)
    }

    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length < 5 * 1024 * 1024) {
            toImagePage(uri)
        } else {
            ImageManager.createImageUri(this)
            mCreateUri = ImageManager.createImageUri(this)
            var intent = Intent("com.android.camera.action.CROP")
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("crop", "true")
//            intent.putExtra("aspectX", 4)
//            intent.putExtra("aspectY", 3)
//            intent.putExtra("outputX", 800)
//            intent.putExtra("outputY", 600)
            intent.putExtra("scale", true)
            intent.putExtra("circleCrop", false)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCreateUri)
            intent.putExtra("return-data", false)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            intent.putExtra("noFaceDetection", true)
            intent = Intent.createChooser(intent, "Crop")
            startActivityForResult(intent, 0x1002)
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

        if (requestCode == 0x1002) {
            if (mCreateUri != null) {
                toImagePage(mCreateUri!!)
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