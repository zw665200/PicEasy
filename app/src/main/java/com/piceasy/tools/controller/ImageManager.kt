package com.piceasy.tools.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.piceasy.tools.bean.*
import com.piceasy.tools.callback.FileCallback
import com.piceasy.tools.callback.HttpCallback
import com.piceasy.tools.callback.UploadCallback
import com.piceasy.tools.config.Constant
import com.piceasy.tools.http.loader.*
import com.piceasy.tools.http.request.AuthService
import com.piceasy.tools.http.response.ResponseTransformer
import com.piceasy.tools.http.schedulers.SchedulerProvider
import com.piceasy.tools.utils.*
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread


/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 14:59
 */
object ImageManager : CoroutineScope by MainScope() {
    private const val APP_ID = "24596362"
    private const val APP_KEY = "ox0Uz65dzs60GHqIloRYcxyL"
    private const val SECRET_KEY = "5OUUiU62kaR6jujt1d5me9kVTV7DuC9v"

    //    private const val APP_ID = "24433333"
//    private const val APP_KEY = "BZznWwsm4PxCOoGY5V8SDuUa"
//    private const val SECRET_KEY = "mpnDji1vfDqsj3ffDGp5jnNXZhQ3rETe"

    // ????????????????????????????????????Android 10????????????????????????????????????
    private var mCameraImagePath: String? = null


    fun checkPermission(activity: Activity, result: (Boolean) -> Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        )

        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //????????????????????????
            result(true)
        } else {
            result(false)

            //??????????????????????????????
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.CAMERA),
                Constant.PERMISSION_CAMERA_REQUEST_CODE
            )
        }
    }

    fun openCamera(activity: Activity, result: (Uri) -> Unit) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // ?????????????????????
        if (captureIntent.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null
            var photoUri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // ??????android 10
                photoUri = createImageUri(activity)
            } else {
                try {
                    photoFile = createImageFile(activity)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.absolutePath
                    photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //??????Android 7.0?????????????????????FileProvider????????????content?????????Uri
                        FileProvider.getUriForFile(activity, activity.packageName.toString() + ".fileprovider", photoFile)
                    } else {
                        Uri.fromFile(photoFile)
                    }
                }
            }

            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                activity.startActivityForResult(captureIntent, Constant.CAMERA_REQUEST_CODE)

                result(photoUri)
            }
        }
    }

    /**
     * ??????????????????uri,?????????????????????????????? Android 10????????????????????????
     */
    public fun createImageUri(activity: Activity): Uri? {
        val status: String = Environment.getExternalStorageState()
        // ???????????????SD???,????????????SD?????????,?????????SD????????????????????????
        return if (status == Environment.MEDIA_MOUNTED) {
            activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        } else {
            activity.contentResolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, ContentValues())
        }
    }

    /**
     * ???????????????????????????
     */
    @Throws(IOException::class)
    private fun createImageFile(activity: Activity): File? {
        val imageName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdir()
        }

        val tempFile = File(storageDir, imageName)
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????4M????????????????????????1600p x 1600px
     */

    fun enlargeImage(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/image_quality_enhance"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            // ????????????????????????????????????????????????????????????access_token???????????????access_token?????????????????? ???????????????????????????????????????????????????
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * ????????????
     */
    fun dehaze(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/dehaze"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)


            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * ?????????????????????
     */
    fun contrastEnhance(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/contrast_enhance"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * ??????????????????
     */

    fun colourize(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/colourize"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            // ????????????????????????????????????????????????????????????access_token???????????????access_token?????????????????? ???????????????????????????????????????????????????
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * ???????????????
     */
    @SuppressLint("CheckResult")
    fun cartoon(activity: Activity, filePath: Uri, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                OSSManager.get().uploadFileToFix(activity, ossParam, filePath, object : UploadCallback {
                    override fun onSuccess(path: String) {

                        CartoonLoader.cartoonTrans(path)
                            .compose(ResponseTransformer.handleResult())
                            .compose(SchedulerProvider.getInstance().applySchedulers())
                            .subscribe({
                                saveImage(activity, it, callback)
                            }, {})

                    }

                    override fun onFailed(msg: String) {
                    }
                })

            }, {
                ToastUtil.show(activity, "call failed")
            })
    }

    /**
     * ??????????????????
     */
    fun styleTrans(activity: Activity, filePath: String, type: String, callback: HttpCallback) {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/style_trans"
        try {
            // ??????????????????
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam&option=$type"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ??????????????????
     */
    fun colorEnhance(activity: Activity, filePath: String, callback: HttpCallback) {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/selfie_anime"
        try {
            // ??????????????????
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ??????????????????
     */
    fun stretch(activity: Activity, filePath: String, callback: HttpCallback) {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/stretch_restore"
        try {
            // ??????????????????
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            // ????????????????????????????????????????????????????????????access_token???????????????access_token?????????????????? ???????????????????????????????????????????????????
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ?????????????????????
     */
    fun definition(activity: Activity, filePath: String, callback: HttpCallback) {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/image_definition_enhance"
        try {
            // ??????????????????
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
//                blockPrint(result)
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("call failed")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ????????????
     */
    fun inPainting(activity: Activity, filePath: String, rectangleMap: HashMap<String, Any>, callback: HttpCallback) {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/inpainting"
        try {
            // ??????????????????
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)

            val rectangle = arrayListOf<Any>()
            rectangle.add(rectangleMap)

            val map = HashMap<String, Any>()
            map["image"] = imgStr
            map["rectangle"] = rectangle

            val param = GsonUtils.toJson(map)

            JLog.i("path = $filePath")
            JLog.i("rectangle = ${map["rectangle"]}")

            //??????access_token
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, "application/json", param)

            if (result != null) {
//                blockPrint(result)
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ????????????
     */
    fun faceMerge(activity: Activity, filePath1: String, filePath2: String, callback: HttpCallback) {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/face/v1/merge"
        try {
            val paramMap = HashMap<String, Any>()
            val templateMap = HashMap<String, Any>()
            templateMap["image"] = Base64Util.encode(FileUtils.readFileByBytes(filePath1))
            templateMap["image_type"] = "BASE64"
            templateMap["quality_control"] = "NONE"
            paramMap["image_template"] = templateMap

            val targetMap = HashMap<String, Any>()
            targetMap["image"] = Base64Util.encode(FileUtils.readFileByBytes(filePath2))
            targetMap["image_type"] = "BASE64"
            targetMap["quality_control"] = "NONE"
            paramMap["image_target"] = targetMap

            val param = GsonUtils.toJson(paramMap)

            //??????access_token
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, "application/json", param)

            if (result != null) {
                saveImageFromBaiduFace(activity, result, Bitmap.CompressFormat.JPEG, callback)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * ????????????
     */
    fun bodySeg(activity: Activity, filePath: String, callback: HttpCallback) {
        // ??????url
        val url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/body_seg"
        try {
            // ??????????????????
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam&type=foreground"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.PNG, callback)
            } else {
                callback.onFailed("call failed")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    fun ageTrans(activity: Activity, filePath: Uri, age: Int, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                OSSManager.get().uploadFileToFix(activity, ossParam, filePath, object : UploadCallback {
                    override fun onSuccess(path: String) {

                        AgeTransLoader.ageTrans(path, age)
                            .compose(ResponseTransformer.handleResult())
                            .compose(SchedulerProvider.getInstance().applySchedulers())
                            .subscribe({
                                saveImage(activity, it, callback)
                            }, {})

                    }

                    override fun onFailed(msg: String) {
                    }
                })

            }, {
                ToastUtil.show(activity, "call failed")
            })
    }

    @SuppressLint("CheckResult")
    fun genderTrans(activity: Activity, filePath: Uri, gender: Int, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                OSSManager.get().uploadFileToFix(activity, ossParam, filePath, object : UploadCallback {
                    override fun onSuccess(path: String) {

                        GenderTransLoader.genderTrans(path, gender)
                            .compose(ResponseTransformer.handleResult())
                            .compose(SchedulerProvider.getInstance().applySchedulers())
                            .subscribe({
                                saveImage(activity, it, callback)
                            }, {})

                    }

                    override fun onFailed(msg: String) {
                    }
                })

            }, {
                ToastUtil.show(activity, "call failed")
            })
    }

    @SuppressLint("CheckResult")
    fun morph(activity: Activity, uploadList: ArrayList<Uri>, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                val list = arrayListOf<String>()
                for ((position, item) in uploadList.withIndex()) {
                    OSSManager.get().uploadFileToFix(activity, ossParam, item, object : UploadCallback {
                        override fun onSuccess(path: String) {
                            list.add(path)
                            if (list.size > 0 && position == uploadList.size - 1) {
                                val gson = Gson()
                                val json = gson.toJson(list)
                                JLog.i("json = $json")
                                MorphLoader.morph(json)
                                    .compose(ResponseTransformer.handleResult())
                                    .compose(SchedulerProvider.getInstance().applySchedulers())
                                    .subscribe({
                                        if (!it.JobId.isNullOrEmpty()) {
                                            val time = System.currentTimeMillis()
                                            getMorphUrl(activity, it.JobId!!, time, callback)
                                        }
                                    }, {})
                            }
                        }

                        override fun onFailed(msg: String) {
                            callback.onFailed("update failed")
                        }
                    })
                }
            }, {
                ToastUtil.show(activity, "call failed")
            })
    }

    @SuppressLint("CheckResult")
    private fun getMorphUrl(context: Context, jobId: String, time: Long, callback: HttpCallback) {
        Thread.sleep(5000)
        GetMorphUrlLoader.getMorphUrl(jobId)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({
                if (!it.Url.isNullOrEmpty()) {
                    JLog.i("url = ${it.Url}")
                    launch(Dispatchers.IO) {
                        FileUtil.downloadVideo(context, it.Url!!, callback)
                        report()
                    }
                } else {
                    if (System.currentTimeMillis() - time < 3 * 60 * 1000L) {
                        getMorphUrl(context, jobId, time, callback)
                    } else {
                        callback.onFailed("timeout")
                    }
                }
            }, {})
    }

    /**
     * ??????????????????
     */
    private fun blockPrint(result: String) {
        val r = result.length / 4096
        if (r < 1) {
            JLog.i("result = $result")
        } else {
            for (index in 0..(r + 1)) {
                if (index < r) {
                    JLog.i("result $index = ${result.substring(index * 4096, (index + 1) * 4096)}")
                } else {
                    JLog.i("result $index = ${result.substring(index * 4096)}")
                }
            }
        }
    }

    private fun saveImage(activity: Activity, res: String, type: Bitmap.CompressFormat, callback: HttpCallback) {
        val gson = Gson()
        if (res.contains("error_msg")) {
            JLog.i("????????????")
            val result = gson.fromJson(res, ImageErrorResult::class.java)

            when (result.error_code) {
//                1 -> callback.onFailed("?????????????????????????????????")
//                4 -> callback.onFailed("?????????????????????")
//                13 -> callback.onFailed("????????????")
//                17, 18, 19 -> callback.onFailed("???????????????")
//                100, 110, 111 -> callback.onFailed("??????????????????")
//                216100, 282004 -> callback.onFailed("????????????")
//                216101 -> callback.onFailed("??????????????????")
//                216102 -> callback.onFailed("?????????????????????")
//                216103 -> callback.onFailed("??????????????????")
//                216200 -> callback.onFailed("????????????")
//                216201 -> callback.onFailed("???????????????????????????")
//                216202 -> callback.onFailed("???????????????????????????")
//                216203 -> callback.onFailed("??????????????????????????????????????????")
//                216204 -> callback.onFailed("????????????????????????")
//                216630 -> callback.onFailed("????????????????????????")
                else -> callback.onFailed(result.error_msg)
            }
            return
        }

        if (res.contains("foreground")) {
            val result = gson.fromJson(res, BodyResult::class.java)
            if (result != null) {

                val data = base64ToByteArray(result.foreground)
                val outPath = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".png"
                FileUtil.byteToFile(data, outPath)

                JLog.i("result logId = ${result.log_id}")
                JLog.i("outPath = $outPath")

                val file = File(outPath)
                if (file.exists()) {
                    if (type == Bitmap.CompressFormat.PNG) {
                        FileUtil.savePNGImage(activity, file)
                    } else {
                        FileUtil.saveImage(activity, file)
                    }
                    callback.onSuccess()
                    JLog.i("save successfully")
                }

            } else {
                callback.onFailed("unknown error")
            }
            return
        }


        if (res.contains("image")) {
            val result = gson.fromJson(res, ImageResult::class.java)
            if (result != null) {
                val data = base64ToByteArray(result.image)
                val outPath = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".png"
                FileUtil.byteToFile(data, outPath)

                JLog.i("result logId = ${result.log_id}")
                JLog.i("outPath = $outPath")

                val file = File(outPath)
                if (file.exists()) {
                    if (type == Bitmap.CompressFormat.PNG) {
                        FileUtil.savePNGImage(activity, file)
                    } else {
                        FileUtil.saveImage(activity, file)
                    }
                    report()
                    callback.onSuccess()
                    JLog.i("save successfully")
                }

            } else {
                callback.onFailed("unknown error")
            }
        }
    }

    private fun saveImageFromBaiduFace(activity: Activity, res: String, type: Bitmap.CompressFormat, callback: HttpCallback) {
        val gson = Gson()
        if (res.contains("error_msg")) {
            val result = gson.fromJson(res, BaiduFaceResult::class.java)
            when (result.error_code) {
                0 -> {
                    val dataBase64 = result.result.merge_image
                    if (dataBase64 != "") {
                        val data = base64ToByteArray(dataBase64)
                        val outPath = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".png"
                        FileUtil.byteToFile(data, outPath)

                        JLog.i("result logId = ${result.log_id}")
                        JLog.i("outPath = $outPath")

                        val file = File(outPath)
                        if (file.exists()) {
                            if (type == Bitmap.CompressFormat.PNG) {
                                FileUtil.savePNGImage(activity, file)
                            } else {
                                FileUtil.saveImage(activity, file)
                            }
                            report()
                            callback.onSuccess()
                            JLog.i("save successfully")
                        }
                    }
                }
//                1 -> callback.onFailed("?????????????????????????????????")
//                4 -> callback.onFailed("?????????????????????")
//                13 -> callback.onFailed("????????????")
//                17, 18, 19 -> callback.onFailed("???????????????")
//                100, 110, 111 -> callback.onFailed("??????????????????")
//                216100, 282004 -> callback.onFailed("????????????")
//                216101 -> callback.onFailed("??????????????????")
//                216102 -> callback.onFailed("?????????????????????")
//                216103 -> callback.onFailed("??????????????????")
//                216200 -> callback.onFailed("????????????")
//                216201 -> callback.onFailed("???????????????????????????")
//                216202 -> callback.onFailed("???????????????????????????")
//                216203 -> callback.onFailed("??????????????????????????????????????????")
//                216204 -> callback.onFailed("????????????????????????")
//                216630 -> callback.onFailed("????????????????????????")
                else -> callback.onFailed(result.error_msg)
            }
        }
    }

    /**
     * ?????????????????????
     */
    private fun saveImage(activity: Activity, result: TencentCloudResult, callback: HttpCallback) {
        if (!result.ResultUrl.isNullOrEmpty()) {
            //???Glide????????????
            Glide.with(activity).asBitmap().load(result.ResultUrl).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    //???????????????
                    FileUtil.savePNGImage(activity, resource, callback)
                    report()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
            return
        }

        if (!result.hint.isNullOrEmpty()) {
            callback.onFailed(result.hint!!)
            return
        }

        if (!result.msg.isNullOrEmpty()) {
            callback.onFailed(result.msg!!)
            return
        }

    }

    /**
     * ??????GIF?????????Picture???
     */
    private fun saveGifImage(activity: Activity, result: TencentCloudResult, callback: HttpCallback) {
        if (!result.ResultUrl.isNullOrEmpty()) {
            //???Glide????????????
            Glide.with(activity).asFile().load(result.ResultUrl).into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    //?????????Picture???
                    val target = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".gif"
                    FileUtil.copyFile(resource.absolutePath, target, object : FileCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, index: Int) {
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
            return
        }

        if (!result.hint.isNullOrEmpty()) {
            callback.onFailed(result.hint!!)
        }

    }

    private fun base64ToByteArray(imageBase64: String): ByteArray? {
        var bytes: ByteArray? = null
        try {

            bytes = if (imageBase64.indexOf("data:image/jpeg;base64,") != -1) {
                Base64Util.decode(imageBase64.replace("data:image/jpeg;base64,".toRegex(), ""))
            } else {
                Base64Util.decode(imageBase64.replace("data:image/jpg;base64,".toRegex(), ""))
            }

            for (i in bytes.indices) {
                if (bytes[i] < 0) { // ??????????????????
                    (bytes[i].plus(256)).toByte()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
    }

    @SuppressLint("CheckResult")
    fun report() {
        val mmkv = MMKV.defaultMMKV()
        when (AppUtil.getChannelId()) {
            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                val times = mmkv?.decodeInt("activity_times")
                if (times == 1) {
                    return
                }
            }
        }

        thread {
            ReportLoader.report()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({

                }, {

                })
        }
    }

}