package com.piceasy.tools.callback

import com.piceasy.tools.bean.FileBean

interface DialogCallback {
    fun onSuccess(file: FileBean)
    fun onCancel()
}