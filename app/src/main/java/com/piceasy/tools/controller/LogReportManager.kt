package com.piceasy.tools.controller

import com.piceasy.tools.config.Constant
import com.piceasy.tools.http.loader.UserReportLoader
import com.piceasy.tools.http.response.ResponseTransformer
import com.piceasy.tools.http.schedulers.SchedulerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/8/18 10:35
 */
object LogReportManager : CoroutineScope by MainScope() {

    fun logReport(name: String, content: String, type: LogType) {
        if (Constant.CLIENT_TOKEN == "") return

        val t = when (type) {
            LogType.LOGIN -> "login"
            LogType.ORDER -> "order"
            LogType.OPERATION -> "operation"
        }

        launch(Dispatchers.IO) {
            UserReportLoader.report(name, content, t)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({

                }, {})
        }
    }

    enum class LogType {
        LOGIN, OPERATION, ORDER
    }
}