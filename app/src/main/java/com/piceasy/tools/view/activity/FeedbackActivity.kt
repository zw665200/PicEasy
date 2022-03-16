package com.piceasy.tools.view.activity

import android.widget.ImageView
import android.widget.TextView
import com.piceasy.tools.R
import com.piceasy.tools.view.base.BaseActivity


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