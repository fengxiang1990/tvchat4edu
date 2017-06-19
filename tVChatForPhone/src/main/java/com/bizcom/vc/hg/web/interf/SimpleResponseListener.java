package com.bizcom.vc.hg.web.interf;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;

/**
 * Created by admin on 2016/12/17.
 */

public abstract class SimpleResponseListener<BaseResponse> implements Response.Listener<BaseResponse>, Response.ErrorListener {


    @Override
    public void onErrorResponse(VolleyError volleyError) {
        if (volleyError.getCause() instanceof ConnectException) {
            onError(new ErrorResponse("错误的请求"));
            return;
        } else if (volleyError.getCause() instanceof SocketTimeoutException) {
            onError(new ErrorResponse("请求超时"));
            return;
        } else if (volleyError.getCause() instanceof IOException) {
            onError(new ErrorResponse("请检查网络连接"));
            return;
        } else if (volleyError.getCause() instanceof ProtocolException) {
            onError(new ErrorResponse(volleyError.getMessage()));
            return;
        }
    }

    @Override
    public void onResponse(BaseResponse response) {
        onSuccess(response);
    }

    protected abstract void onSuccess(BaseResponse t);

    protected abstract void onError(ErrorResponse response);
}
