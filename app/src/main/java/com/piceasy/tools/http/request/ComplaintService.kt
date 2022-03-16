package com.piceasy.tools.http.request

import com.piceasy.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ComplaintService {

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
}