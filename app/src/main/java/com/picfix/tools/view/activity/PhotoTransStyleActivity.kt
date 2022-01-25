package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.controller.LogReportManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_pic_with_shadow.view.*
import java.util.*


class PhotoTransStyleActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var bigPic: ImageView
    private lateinit var camera: Button
    private lateinit var album: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var styleName: TextView

    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<Uri>()
    private var value = ""
    private var mCameraUri: Uri? = null
    private var picList = arrayListOf<Resource>()
    private lateinit var adapter: DataAdapter<Resource>
    private var currentPosition = 0

    override fun setLayout(): Int {
        return R.layout.a_photo_trans_style
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        bigPic = findViewById(R.id.big_pic)
        recyclerView = findViewById(R.id.rv_pic_style)
        styleName = findViewById(R.id.style_name)

        back.setOnClickListener { finish() }

        camera = findViewById(R.id.open_camera)
        album = findViewById(R.id.open_album)

        camera.setOnClickListener { takePhoto() }
        album.setOnClickListener { chooseAlbum() }

    }

    override fun initData() {
        initRecyclerView()

        LogReportManager.logReport("图片风格转换", "访问页面", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("visit", "operation")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        picList.clear()
        picList.add(Resource("1", R.drawable.ic_colour_after_1, getString(R.string.style_type_10)))
        picList.add(Resource("1", R.drawable.iv_trans_qianbi, getString(R.string.style_type_1)))
        picList.add(Resource("1", R.drawable.iv_trans_caiseqianbi, getString(R.string.style_type_2)))
        picList.add(Resource("1", R.drawable.iv_trans_caisetangkuai, getString(R.string.style_type_3)))
        picList.add(Resource("1", R.drawable.iv_trans_shenhuchuanchonglang, getString(R.string.style_type_4)))
        picList.add(Resource("1", R.drawable.iv_trans_xunyicao, getString(R.string.style_type_5)))
        picList.add(Resource("1", R.drawable.iv_trans_qiyi, getString(R.string.style_type_6)))
        picList.add(Resource("1", R.drawable.iv_trans_nahan, getString(R.string.style_type_7)))
        picList.add(Resource("1", R.drawable.iv_trans_geteyou, getString(R.string.style_type_8)))
        picList.add(Resource("1", R.drawable.iv_trans_katong, getString(R.string.style_type_9)))

        val width = AppUtil.getScreenWidth(this)

        adapter = DataAdapter.Builder<Resource>()
            .setData(picList)
            .setLayoutId(R.layout.item_pic_with_shadow)
            .addBindView { itemView, itemData, position ->

                val layoutParam = itemView.layoutParams
                layoutParam.width = width / 4
                itemView.layoutParams = layoutParam

                itemView.img.setImageResource(itemData.icon)

                if (currentPosition == position) {
                    itemView.setBackgroundResource(R.drawable.shape_rectangle_orange)
                } else {
                    itemView.setBackgroundResource(R.drawable.shape_corner_white)
                }

                itemView.setOnClickListener {
                    bigPic.setImageResource(itemData.icon)
                    styleName.text = itemData.name
                    currentPosition = position
                    adapter.notifyDataSetChanged()
                }
            }
            .create()

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter
        adapter.notifyItemRangeChanged(0, picList.size)
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

        LogReportManager.logReport("图片风格转换", "打开相册", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("open_album", "operation")
    }

    private fun toImagePage(uri: Uri) {
        val intent = Intent(this, ImageActivity::class.java)
        intent.putExtra("from", "style_trans")
        intent.putExtra("uri", uri.toString())
        startActivity(intent)
    }

    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 5 * 1024 * 1024) {
            ToastUtil.show(this, "less than 5MB")
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

    private fun firebaseAnalytics(key: String, value: String) {
        val bundle = Bundle()
        bundle.putString(key, value)
        Firebase.analytics.logEvent("page_style_shift", bundle)

        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CONTENT] = "page_style_shift"
        if (key == "visit") {
            AppsFlyerLib.getInstance().logEvent(applicationContext, "page_style_shift", eventValues)
        } else {
            AppsFlyerLib.getInstance().logEvent(applicationContext, key, eventValues)
        }
    }

}