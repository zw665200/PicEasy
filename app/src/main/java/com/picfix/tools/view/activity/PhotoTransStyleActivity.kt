package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        picList.clear()
        picList.add(Resource("1", R.drawable.ic_colour_after_1, "Original Style"))
        picList.add(Resource("1", R.drawable.iv_trans_qianbi, "Pencil Style"))
        picList.add(Resource("1", R.drawable.iv_trans_caiseqianbi, "Color Pencil Style "))
        picList.add(Resource("1", R.drawable.iv_trans_caisetangkuai, "Colorful Candy Style"))
        picList.add(Resource("1", R.drawable.iv_trans_shenhuchuanchonglang, "Kanagawa Surfing"))
        picList.add(Resource("1", R.drawable.iv_trans_xunyicao, "Lavender Oil Painting Style"))
        picList.add(Resource("1", R.drawable.iv_trans_qiyi, "Strange oil painting Style"))
        picList.add(Resource("1", R.drawable.iv_trans_nahan, "Shouting Oil Painting Style"))
        picList.add(Resource("1", R.drawable.iv_trans_geteyou, "Gothic Oil Painting Style"))
        picList.add(Resource("1", R.drawable.iv_trans_katong, "Cartoon Style"))

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

}