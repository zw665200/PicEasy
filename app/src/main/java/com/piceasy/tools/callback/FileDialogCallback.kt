package com.piceasy.tools.callback

interface FileDialogCallback {
    fun onSuccess(str: String)
    fun onCancel()
}