package com.piceasy.tools.view.views

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import com.piceasy.tools.R
import com.piceasy.tools.utils.AppUtil


class FixTipsDialog(private val activity: Activity) : Dialog(activity, R.style.app_dialog) {

    private lateinit var ok: TextView

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_fix_tips, null)
        setContentView(dialogContent)
        setCancelable(false)

        ok = findViewById(R.id.ok)
        ok.setOnClickListener { cancel() }
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) - 50
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}