package com.picfix.tools.view.activity

import android.widget.ImageView
import android.widget.TextView
import com.picfix.tools.R
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.view.base.BaseActivity


class FeedbackActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var versionInfo: TextView

    override fun setLayout(): Int {
        return R.layout.a_feedback
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        versionInfo = findViewById(R.id.version)

        back.setOnClickListener { finish() }

    }

    override fun initData() {
    }

}