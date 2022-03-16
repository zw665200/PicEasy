package com.piceasy.tools.view.activity

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import com.piceasy.tools.R
import com.piceasy.tools.utils.AppUtil
import com.piceasy.tools.view.base.BaseActivity

class AboutUsActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var versionInfo: TextView


    override fun setLayout(): Int {
        return R.layout.a_about_us
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        versionInfo = findViewById(R.id.version)

        back.setOnClickListener { finish() }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        val versionName = AppUtil.getPackageVersionName(this, packageName)
        val name = getString(R.string.app_name)
        versionInfo.text = "$name  $versionName"
    }
}