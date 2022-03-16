package com.piceasy.tools.bean

data class WechatPayParam(
    var body: String,
    var noncestr: String,
    var timestamp: Long,
    var sign: String,
    var orderSn: String
)
