package com.piceasy.tools.view.views

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import com.piceasy.tools.R
import com.piceasy.tools.callback.DialogCallback
import com.piceasy.tools.utils.AppUtil


class PaySuccessDialog(private val activity: Activity, callback: DialogCallback) : Dialog(activity, R.style.app_dialog) {
    private var mCallback = callback
    private lateinit var cancel: Button

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_pay_success, null)
        setContentView(dialogContent)
        setCancelable(false)

        cancel = dialogContent.findViewById(R.id.dialog_cancel)
        cancel.setOnClickListener {
            cancel()
        }

    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 2 / 3
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }

    override fun cancel() {
        super.cancel()
        mCallback.onCancel()
    }


}