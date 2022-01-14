package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.callback.Callback
import com.picfix.tools.callback.HttpCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.controller.LogReportManager
import com.picfix.tools.controller.PayManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.FileUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.CropCanvas
import com.picfix.tools.view.views.SaveSuccessDialog
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.a_agreement.*
import kotlinx.android.synthetic.main.item_pic_feedback.view.*
import kotlinx.android.synthetic.main.rv_choose_account_item.view.*
import kotlinx.coroutines.*
import java.io.File

class ImageActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var before: ImageView
    private lateinit var select: CropCanvas
    private lateinit var title: TextView
    private lateinit var fix: Button
    private lateinit var spinner: AppCompatSpinner
    private lateinit var age: AppCompatSeekBar
    private lateinit var ageText: TextView
    private lateinit var maleToFemale: AppCompatRadioButton
    private lateinit var femaleToMale: AppCompatRadioButton
    private lateinit var firstFace: ImageView
    private lateinit var secondFace: ImageView

    private lateinit var styleTransLayout: LinearLayout
    private lateinit var ageTransLayout: LinearLayout
    private lateinit var genderTransLayout: LinearLayout
    private lateinit var morphLayout: LinearLayout
    private lateinit var faceMergeLayout: LinearLayout
    private lateinit var commonLayout: FrameLayout

    private lateinit var picRv: RecyclerView
    private lateinit var mAdapter: DataAdapter<Bitmap>
    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<Uri>()

    private var uriStr: String? = null
    private var firstFacePath: String? = null
    private var secondFacePath: String? = null
    private var from: String? = null
    private var type = ""
    private var text = ""
    private var reportTitle = ""
    private var progressValue = 50

    override fun setLayout(): Int {
        return R.layout.a_image
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        title = findViewById(R.id.tv_title)
        before = findViewById(R.id.image_edit_before)
        select = findViewById(R.id.image_edit_area_select)
        fix = findViewById(R.id.begin_fix)
        spinner = findViewById(R.id.spinner_style)
        firstFace = findViewById(R.id.image_face_1)
        secondFace = findViewById(R.id.image_face_2)

        age = findViewById(R.id.age)
        ageText = findViewById(R.id.age_text)
        maleToFemale = findViewById(R.id.male_to_female)
        femaleToMale = findViewById(R.id.female_to_male)

        commonLayout = findViewById(R.id.layout_common)
        styleTransLayout = findViewById(R.id.ll_style_trans)
        ageTransLayout = findViewById(R.id.ll_age_trans)
        genderTransLayout = findViewById(R.id.ll_gender_trans)
        morphLayout = findViewById(R.id.ll_morph)
        faceMergeLayout = findViewById(R.id.layout_face_merge)

        picRv = findViewById(R.id.pics_recyclerview)

        back.setOnClickListener { finish() }
        fix.setOnClickListener { beginFix() }
    }

    override fun initData() {
        from = intent.getStringExtra("from")
        uriStr = intent.getStringExtra("uri")
        if (uriStr != null) {
            val uri = Uri.parse(uriStr)
            firstFacePath = FileUtil.getRealPathFromUri(this, uri)

            val needSelect = intent.getBooleanExtra("select", false)
            if (needSelect) {
                before.visibility = View.GONE
                select.visibility = View.VISIBLE
                select.setBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(uri)))
            } else {
                before.visibility = View.VISIBLE
                select.visibility = View.GONE
                before.setImageURI(uri)
            }
        }

        if (from != null) {
            when (from!!) {
                "definition" -> {
                    reportTitle = getString(R.string.title_definition_)
                    text = "AI Enhance Photo"
                }
                "cartoon" -> {
                    reportTitle = getString(R.string.title_cartoon_)
                    text = "Cartoon Selfie"
                }
                "defogging" -> {
                    reportTitle = getString(R.string.title_defogging_)
                    text = "Defogging"
                }
                "constrast" -> {
                    reportTitle = getString(R.string.title_contrast_)
                    text = "Enhance"
                }
                "colour" -> {
                    reportTitle = getString(R.string.title_colour_)
                    text = "Photo Colorizer"
                }
                "watermark" -> {
                    reportTitle = getString(R.string.title_watermark_)
                    text = "Photo Retouch"
                }
                "resize" -> {
                    reportTitle = getString(R.string.title_resize_)
                    text = "Amplification"
                }
                "colorful" -> {
                    reportTitle = getString(R.string.title_colorful_)
                    text = "Enhanced"
                }
                "stretch" -> {
                    reportTitle = getString(R.string.title_stretch_fix_)
                    text = "Repair Stretch"
                }
                "matting" -> {
                    reportTitle = getString(R.string.title_matting_)
                    text = "Cutout"
                }
                "style_trans" -> {
                    reportTitle = getString(R.string.title_trans_style_)
                    text = "Style Shift"
                    styleTransLayout.visibility = View.VISIBLE
                    spinnerListener()
                }

                "age_trans" -> {
                    reportTitle = getString(R.string.title_age_trans_)
                    text = "Age Conversion"
                    ageTransLayout.visibility = View.VISIBLE
                    initAgeTrans()
                }
                "gender_trans" -> {
                    reportTitle = getString(R.string.title_gender_trans_)
                    text = "Gender Conversion"
                    genderTransLayout.visibility = View.VISIBLE
                    initGenderTrans()
                }
                "morph" -> {
                    reportTitle = getString(R.string.title_morph_)
                    text = "Portrait Gradient"
                    morphLayout.visibility = View.VISIBLE
                    initRecyclerView()
                }
                "face_merge" -> {
                    reportTitle = getString(R.string.title_face_merge_)
                    text = "Face Fusion"
                    commonLayout.visibility = View.GONE
                    faceMergeLayout.visibility = View.VISIBLE
                    initFaceMerge()
                }
            }

            title.text = text
            LogReportManager.logReport(reportTitle, "访问了", LogReportManager.LogType.OPERATION)

        }
    }


    private fun spinnerListener() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> type = ""
                    1 -> type = "pencil"
                    2 -> type = "color_pencil"
                    3 -> type = "warm"
                    4 -> type = "wave"
                    5 -> type = "lavender"
                    6 -> type = "mononoke"
                    7 -> type = "scream"
                    8 -> type = "gothic"
                    9 -> type = "cartoon"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun initGenderTrans() {
        maleToFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                femaleToMale.isChecked = false
            }
        }

        femaleToMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                maleToFemale.isChecked = false
            }
        }
    }

    private fun initAgeTrans() {
        age.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 10) {
                    progressValue = 10
                    ageText.text = progressValue.toString()
                    return
                }

                if (progress > 80) {
                    progressValue = 80
                    ageText.text = progressValue.toString()
                    return
                }

                progressValue = progress
                ageText.text = progressValue.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun initFaceMerge() {
        if (uriStr != null) {
            val uri = Uri.parse(uriStr)
            Glide.with(this).load(uri).into(firstFace)

            secondFace.setOnClickListener {
                chooseAlbum(0x1002)
            }
        }
    }

    private fun initRecyclerView() {
        mList.add(BitmapFactory.decodeFile(firstFacePath))
        mList.add(BitmapFactory.decodeResource(resources, R.drawable.ic_add_pic))
        uploadList.add(Uri.fromFile(File(firstFacePath!!)))

        val width = AppUtil.getScreenWidth(this)
        mAdapter = DataAdapter.Builder<Bitmap>()
            .setData(mList)
            .setLayoutId(R.layout.item_pic_feedback)
            .addBindView { itemView, itemData, position ->
                val layout = itemView.layoutParams
                layout.width = width / 6
                layout.height = width / 6
                itemView.layoutParams = layout

                when (from!!) {
                    "morph" -> {
                        if (position == 5) {
                            itemView.visibility = View.GONE
                        }
                    }

                    "face_merge" -> {
                        if (position == 2) {
                            itemView.visibility = View.GONE
                        }
                    }
                }


                Glide.with(this).load(itemData).into(itemView.rv_pic)

                itemView.setOnClickListener {
                    if (position == mList.size - 1) {
                        chooseAlbum(0x1001)
                    }
                }
            }
            .create()

        picRv.layoutManager = GridLayoutManager(this, 5)
        picRv.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, mList.size)
    }

    private fun chooseAlbum(requestCode: Int) {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
    }

    private fun checkPay(pay: () -> Unit, notPay: () -> Unit) {
        PayManager.getInstance().checkFixPay(this) {
            if (it) {
                pay()
            } else {
                notPay()
            }
        }
    }

    private fun beginFix() {
        if (Constant.USER_NAME == "") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            return
        }


        checkPay({
            if (from != null) {
                when (from!!) {
                    "definition" -> definition()
                    "cartoon" -> cartoon()
                    "style_trans" -> styleTrans()
                    "defogging" -> dehaze()
                    "constrast" -> contrastEnhance()
                    "colour" -> colourize()
                    "watermark" -> inPainting()
                    "stretch" -> stretch()
                    "colorful" -> colorEnhance()
                    "resize" -> enlarge()
                    "matting" -> bodySeg()
                    "age_trans" -> ageTrans(progressValue)
                    "gender_trans" -> genderTrans()
                    "morph" -> morph()
                    "face_merge" -> faceMerge()
                }

                fix.isEnabled = false
                fix.text = getString(R.string.fix_access)
                fix.background = ContextCompat.getDrawable(this, R.drawable.shape_corner_grey)

            }

        }) {
            val intent = Intent(this, PayActivity::class.java)
            intent.putExtra("serviceId", 7)
            startActivity(intent)
        }

    }


    /**
     * 人物动漫化
     */
    private fun cartoon() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.cartoon(this@ImageActivity, Uri.parse(uriStr), object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }

                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        }
    }

    /**
     * 风格转换
     */
    private fun styleTrans() {
        if (type == "") {
            ToastUtil.showShort(this, "Please choose a transform style")
            return
        }

        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.styleTrans(this@ImageActivity, firstFacePath!!, type, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        }
    }

    /**
     * 图像去雾
     */
    private fun dehaze() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.dehaze(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        }
    }

    /**
     * 增加对比度
     */
    private fun contrastEnhance() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.contrastEnhance(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        }
    }

    /**
     * 黑白照片上色
     */
    private fun colourize() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.colourize(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 增强清晰度
     */
    private fun definition() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.definition(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 图像色彩增强
     */
    private fun colorEnhance() {
        LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
        if (firstFacePath != null) {
            launch(Dispatchers.IO) {
                ImageManager.colorEnhance(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }

    }

    /**
     * 拉伸图像恢复
     */
    private fun enlarge() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.enlargeImage(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 拉伸图像恢复
     */
    private fun stretch() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.stretch(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 图片修复
     */
    private fun inPainting() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.inPainting(this@ImageActivity, firstFacePath!!, select.selectAreaMap, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 人像分割
     */
    private fun bodySeg() {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.bodySeg(this@ImageActivity, firstFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 年龄转换
     */
    private fun ageTrans(number: Int) {
        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.ageTrans(this@ImageActivity, Uri.fromFile(File(firstFacePath!!)), number, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 性别转换
     * 转换方向，0：男变女，1：女变男
     */
    private fun genderTrans() {
        if (!maleToFemale.isChecked && !femaleToMale.isChecked) {
            ToastUtil.showShort(this, "Please choose a transform style")
            return
        }

        var value = 0
        if (femaleToMale.isChecked) {
            value = 1
        }

        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.genderTrans(this@ImageActivity, Uri.fromFile(File(firstFacePath!!)), value, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }


    /**
     * 人脸渐变
     */
    private fun morph() {
        if (uploadList.size < 2) {
            ToastUtil.showShort(this, "Please choose two face pictures at lease")
            return
        }

        if (firstFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.morph(this@ImageActivity, uploadList, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    /**
     * 人脸融合
     */
    private fun faceMerge() {
        if (firstFacePath != null && secondFacePath != null) {
            LogReportManager.logReport(text, "使用成功", LogReportManager.LogType.OPERATION)
            launch(Dispatchers.IO) {
                ImageManager.faceMerge(this@ImageActivity, firstFacePath!!, secondFacePath!!, object : HttpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            showSuccessDialog()
                            ToastUtil.showShort(this@ImageActivity, getString(R.string.fix_save_to_album))
                            resetStatus()
                        }
                    }

                    override fun onFailed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showLong(this@ImageActivity, "error：$msg")
                            resetStatus()
                        }
                    }
                })
            }
        } else {
            JLog.i("image bitmap is null")
        }
    }

    private fun resetStatus() {
        fix.isEnabled = true
        fix.text = getString(R.string.fix_begin)
        fix.background = ContextCompat.getDrawable(this@ImageActivity, R.drawable.shape_corner_blue)
    }

    private fun showSuccessDialog() {
        var value = getString(R.string.title_album)
        if (from == "morph") {
            value = "/Pictures/"
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
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            val uri = data.data
            if (uri != null) {
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
                if (length == 0L || length > 3 * 1024 * 1024) {
                    ToastUtil.show(this, "Less than 3MB")
                    return
                }

                if (requestCode == 0x1001) {
                    mList.removeAt(mList.size - 1)
                    mList.add(bitmap)
                    uploadList.add(uri)
                    if (mList.size < 5) {
                        mList.add(BitmapFactory.decodeResource(resources, R.drawable.ic_add_pic))
                    }
                    mAdapter.notifyDataSetChanged()
                }

                if (requestCode == 0x1002) {
                    Glide.with(this).load(uri).into(secondFace)
                    secondFacePath = FileUtil.getRealPathFromUri(this, uri)
                }
            }
        }
    }


}