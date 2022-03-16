package com.piceasy.tools.callback

interface PayCallback {
    fun success()
    fun progress(orderId: String)
    fun failed(msg: String)
}