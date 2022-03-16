package com.piceasy.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.piceasy.tools.R
import com.piceasy.tools.callback.Callback
import com.piceasy.tools.callback.HttpCallback
import com.piceasy.tools.config.Constant
import com.piceasy.tools.controller.ImageManager
import com.piceasy.tools.controller.LogReportManager
import com.piceasy.tools.controller.PayManager
import com.piceasy.tools.utils.AppUtil
import com.piceasy.tools.utils.BitmapUtil
import com.piceasy.tools.utils.FileUtil
import com.piceasy.tools.utils.ToastUtil
import com.piceasy.tools.view.base.BaseActivity
import com.piceasy.tools.view.views.SaveSuccessDialog
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.HashMap


class PhotoFormatTransActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var from: ImageView
    private lateinit var add: TextView
    private lateinit var begin: Button
    private lateinit var size: TextView
    private var mImageUri: Uri? = null
    private var value = Bitmap.CompressFormat.JPEG

    private lateinit var jpgRadioButton: RadioButton
    private lateinit var pngRadioButton: RadioButton
    private lateinit var webpRadioButton: RadioButton
    private lateinit var bmpRadioButton: RadioButton

    override fun setLayout(): Int {
        return R.layout.a_photo_format_trans
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        from = findViewById(R.id.image_from)
        size = findViewById(R.id.image_size)
        begin = findViewById(R.id.begin_fix)
        add = findViewById(R.id.image_add)

        jpgRadioButton = findViewById(R.id.radio_jpg)
        pngRadioButton = findViewById(R.id.radio_png)
        webpRadioButton = findViewById(R.id.radio_webp)
        bmpRadioButton = findViewById(R.id.radio_bmp)

        initRadioButtonCheckedListener()

        back.setOnClickListener { finish() }
        from.setOnClickListener { chooseAlbum() }
        begin.setOnClickListener { beginFix() }

    }

    override fun initData() {
        LogReportManager.logReport("图片格式转换", "访问页面", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("visit", "operation")
    }

    private fun initRadioButtonCheckedListener() {
        jpgRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                value = Bitmap.CompressFormat.JPEG
                pngRadioButton.isChecked = false
                webpRadioButton.isChecked = false
                bmpRadioButton.isChecked = false
            }
        }

        pngRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                value = Bitmap.CompressFormat.PNG
                jpgRadioButton.isChecked = false
                webpRadioButton.isChecked = false
                bmpRadioButton.isChecked = false
            }
        }

        webpRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                value = Bitmap.CompressFormat.WEBP
                jpgRadioButton.isChecked = false
                pngRadioButton.isChecked = false
                bmpRadioButton.isChecked = false
            }
        }

        bmpRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                jpgRadioButton.isChecked = false
                webpRadioButton.isChecked = false
                pngRadioButton.isChecked = false
            }
        }
    }

    private fun chooseAlbum() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, 0x1001)

        LogReportManager.logReport("图片格式转换", "打开相册", LogReportManager.LogType.OPERATION)
        firebaseAnalytics("open_album", "operation")
    }

    @SuppressLint("SetTextI18n")
    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 5 * 1024 * 1024) {
            ToastUtil.show(this, getString(R.string.exceed_5))
        } else {
            add.visibility = View.GONE
            size.text = "$length b"

            Glide.with(this).asBitmap().load(uri).into(object : CustomTarget<Bitmap>() {
                @SuppressLint("SetTextI18n")
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    from.setImageBitmap(resource)
                    val width = resource.width
                    val height = resource.height
                    size.text = "${width}px * ${height}px  ${length / 1024}KB"
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        }
    }

    private fun beginFix() {
        if (Constant.USER_NAME == "") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            return
        }

        PayManager.getInstance().checkFixPay(this) {
            if (!it) {
                toPay()
            } else {
                if (mImageUri == null) {
                    ToastUtil.showShort(this, "图片不能为空")
                    return@checkFixPay
                }

                if (!jpgRadioButton.isChecked && !pngRadioButton.isChecked && !webpRadioButton.isChecked && !bmpRadioButton.isChecked) {
                    ToastUtil.showShort(this, "选择转换格式")
                    return@checkFixPay
                }

                begin.isEnabled = false
                begin.text = getString(R.string.fix_access)
                begin.background = ContextCompat.getDrawable(this, R.drawable.shape_corner_grey)

                if (bmpRadioButton.isChecked) {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(mImageUri!!))
                    launch {
                        BitmapUtil.createBMP(this@PhotoFormatTransActivity, bitmap, object : HttpCallback {
                            override fun onSuccess() {
                                launch(Dispatchers.Main) {
                                    showSuccessDialog()
                                    resetStatus()
                                    ImageManager.report()
                                }
                            }

                            override fun onFailed(msg: String) {
                                launch(Dispatchers.Main) {
                                    ToastUtil.showShort(this@PhotoFormatTransActivity, msg)
                                }
                            }
                        })
                    }
                    return@checkFixPay
                }

                val path = FileUtil.getRealPathFromUri(this, mImageUri)
                FileUtil.saveImage(this, File(path), value, object : HttpCallback {
                    override fun onSuccess() {
                        showSuccessDialog()
                        resetStatus()
                        ImageManager.report()
                    }

                    override fun onFailed(msg: String) {

                    }
                })
            }
        }
    }

    private fun toPay() {
        val intent = Intent(this, PayActivity::class.java)
        intent.putExtra("serviceId", 7)
        startActivity(intent)
    }


    private fun resetStatus() {
        begin.isEnabled = true
        begin.text = getString(R.string.fix_begin)
        begin.background = ContextCompat.getDrawable(this, R.drawable.shape_corner_blue)
    }

    private fun showSuccessDialog() {
        var value = "相册"
        if (bmpRadioButton.isChecked) {
            value = "文件管理/Pictures"
        }

        SaveSuccessDialog(this, value, object : Callback {
            override fun onSuccess() {
            }

            override fun onCancel() {
                finish()
            }
        }).show()

        when (AppUtil.getChannelId()) {
            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                val mmkv = MMKV.defaultMMKV()
                val times = mmkv?.decodeInt("activity_times")
                if (times == 1) {
                    mmkv.encode("activity_times", 0)
                }
            }
        }

        LogReportManager.logReport("图片格式转换", "使用成功", LogReportManager.LogType.OPERATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    checkFileSize(uri)
                    mImageUri = uri
                }
            }
        }
    }

    private fun firebaseAnalytics(key: String, value: String) {
        if (!Constant.REPORT_OPENNING) return

        val bundle = Bundle()
        bundle.putString(key, value)
        Firebase.analytics.logEvent("page_format_conversion", bundle)

        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CONTENT] = "page_format_conversion"
        if (key == "visit") {
            AppsFlyerLib.getInstance().logEvent(applicationContext, "page_format_conversion", eventValues)
        } else {
            AppsFlyerLib.getInstance().logEvent(applicationContext, key, eventValues)
        }
    }

}
