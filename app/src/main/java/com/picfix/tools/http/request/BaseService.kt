package com.picfix.tools.http.request

import com.picfix.tools.bean.*
import com.picfix.tools.config.Constant
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/22 15:11
 */
interface BaseService {

    /***************common request****************/
    /***************common request****************/
    /***************common request****************/

    @GET("siteInfo")
    fun getConfig(@Query("serverCode") serviceCode: String): Observable<Response<Config>>

    @GET("serverList/${Constant.PRODUCT_ID}")
    fun getServiceList(): Observable<Response<List<Price>>>

    @POST("grantAKToken")
    @FormUrlEncoded
    fun getOssToken(@Field("clientToken") clientToken: String): Observable<Response<OssParam>>


    @POST("third/googleGrantLogin")
    @FormUrlEncoded
    fun getUser(
        @Field("questToken") questToken: String,
        @Field("googleToken") googleToken: String,
        @Field("osType") osType: String
    ): Observable<Response<UserInfo>>

    @POST("visit")
    @FormUrlEncoded
    fun visit(
        @Field("questToken") questToken: String,
        @Field("osType") osType: String,
    ): Observable<Response<List<UserInfo>>>

    /***************functions request****************/
    /***************functions request****************/
    /***************functions request****************/

    @POST("getGoogleQuestToken")
    fun getToken(@Body getToken: GetToken): Observable<Response<Token>>

    @POST("cancel")
    @FormUrlEncoded
    fun delete(
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String
    ): Observable<Response<String>>


    /***************functions request****************/
    /***************functions request****************/
    /***************functions request****************/

    @POST("agePic")
    @FormUrlEncoded
    fun ageTrans(
        @Field("clientToken") clientToken: String,
        @Field("imageUrl") imageUrl: String,
        @Field("age") age: Int
    ): Observable<Response<TencentCloudResult>>

    @POST("cartoonPic")
    @FormUrlEncoded
    fun cartoon(
        @Field("clientToken") clientToken: String,
        @Field("imageUrl") imageUrl: String
    ): Observable<Response<TencentCloudResult>>

    @POST("sexPic")
    @FormUrlEncoded
    fun genderTrans(
        @Field("clientToken") clientToken: String,
        @Field("imageUrl") imageUrl: String,
        @Field("gender") gender: Int
    ): Observable<Response<TencentCloudResult>>


    @POST("getMorphUrl")
    @FormUrlEncoded
    fun getMorphUrl(
        @Field("clientToken") clientToken: String,
        @Field("jobId") jobId: String
    ): Observable<Response<MorphResult>>

    @POST("morphPic")
    @FormUrlEncoded
    fun morph(
        @Field("clientToken") clientToken: String,
        @Field("urls") urls: String
    ): Observable<Response<TencentCloudMorphResult>>


    /***************payment request****************/
    /***************payment request****************/
    /***************payment request****************/

    @POST("orderPay")
    @FormUrlEncoded
    fun getOrderParam(
        @Field("serviceId") serviceId: Int,
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String,
        @Field("channelCode") channelCode: String
    ): Observable<Response<AlipayParam>>

    @POST("wechatPay")
    @FormUrlEncoded
    fun getWechatOrderParam(
        @Field("serviceId") serviceId: Int,
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String,
        @Field("channelCode") channelCode: String
    ): Observable<Response<WechatPayParam>>

    @POST("fourthPay")
    @FormUrlEncoded
    fun getFastPayOrderParam(
        @Field("serviceId") serviceId: Int,
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String,
        @Field("channelCode") channelCode: String
    ): Observable<Response<FastPayParam>>


    @POST("atfOrder")
    @FormUrlEncoded
    fun getAtfOrderParam(
        @Field("serverId") serverId: Int,
        @Field("clientToken") token: String,
        @Field("channelCode") channelCode: String,
        @Field("unit") unit: String
    ): Observable<Response<AlipayParam>>

    @POST("cspayStatus")
    @FormUrlEncoded
    fun checkPay(
        @Field("clientToken") clientToken: String,
    ): Observable<Response<CheckPayParam>>


    @POST("cspayStatusV2")
    @FormUrlEncoded
    fun checkPay(
        @Field("productId") productId: String,
        @Field("clientToken") clientToken: String
    ): Observable<Response<CheckPayParam>>

    @POST("notify/googlePayBack")
    @FormUrlEncoded
    fun orderValidate(
        @Field("clientToken") token: String,
        @Field("packageName") packageName: String,
        @Field("productId") productId: String,
        @Field("purchaseToken") purchaseToken: String
    ): Observable<Response<GooglePayResult>>

    @POST("orderCancel")
    @FormUrlEncoded
    fun orderCancel(@Field("orderSn") orderSn: String, @Field("clientToken") token: String): Observable<Response<String?>>

    @POST("orderDetail")
    @FormUrlEncoded
    fun getOrderDetail(@Field("orderSn") orderSn: String, @Field("clientToken") token: String): Observable<Response<OrderDetail>>

    @POST("orderList")
    @FormUrlEncoded
    fun getOrders(
        @Field("clientToken") token: String,
        @Field("productId") productId: String
    ): Observable<Response<List<Order>>>

    @POST("serverStatus")
    @FormUrlEncoded
    fun getPayStatus(
        @Field("serverId") serverId: Int,
        @Field("clientToken") token: String
    ): Observable<Response<PayStatus>>

    /***************report request****************/
    /***************report request****************/
    /***************report request****************/

    @POST("userComplaint")
    @FormUrlEncoded
    fun reportComplaint(
        @Field("uid") uid: String,
        @Field("username") username: String,
        @Field("clientToken") clientToken: String,
        @Field("complaintType") complaintType: String,
        @Field("phone") phone: String,
        @Field("payAccount") payAccount: String,
        @Field("problemDesc") problemDesc: String,
        @Field("pic") pic: String,
        @Field("productId") productId: String
    ): Observable<Response<List<String?>?>>

    @POST("useTimesReport")
    @FormUrlEncoded
    fun report(@Field("clientToken") token: String): Observable<Response<String?>>

    @POST("addUserLog")
    @FormUrlEncoded
    fun report(
        @Field("clientToken") token: String,
        @Field("path") path: String,
        @Field("content") content: String,
        @Field("logType") logType: String,
    ): Observable<Response<String?>>


}