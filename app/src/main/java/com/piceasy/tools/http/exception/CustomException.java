package com.piceasy.tools.http.exception;

import com.google.gson.JsonParseException;
import com.piceasy.tools.utils.JLog;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;

public class CustomException {
    /**
     * 未知错误
     */
    public static final int UNKNOWN = 1000;

    /**
     * 解析错误
     */
    public static final int PARSE_ERROR = 1001;

    /**
     * 网络错误
     */
    public static final int NETWORK_ERROR = 1002;

    /**
     * 协议错误
     */
    public static final int HTTP_ERROR = 1003;

    public static ApiException handleException(Throwable e) {
        ApiException ex;
        if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            JLog.i("1");
            //解析错误
            ex = new ApiException(PARSE_ERROR, e.getMessage());
            return ex;
        } else if (e instanceof ConnectException) {
            JLog.i("2");
            //网络错误
            ex = new ApiException(NETWORK_ERROR, e.getMessage());
            return ex;
        } else if (e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
            JLog.i("3");
            //连接错误
            ex = new ApiException(NETWORK_ERROR, e.getMessage());
            return ex;
        } else {
            JLog.i("4");
            //未知错误
            ex = new ApiException(UNKNOWN, e.getMessage());
            return ex;
        }
    }
}
