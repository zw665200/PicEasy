package com.piceasy.tools.callback

interface HttpCallback {
    fun onSuccess()
    fun onFailed(msg: String)
}