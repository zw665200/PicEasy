package com.piceasy.tools.callback

import com.piceasy.tools.bean.FileStatus
import com.piceasy.tools.bean.FileWithType

interface FileWithTypeCallback {
    fun onSuccess(step: Enum<FileStatus>)
    fun onProgress(step: Enum<FileStatus>, file: FileWithType)
    fun onFailed(step: Enum<FileStatus>, message: String)
}