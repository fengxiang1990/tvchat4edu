package com.bizcom.vc.hg.web;

import android.util.Log;

import java.util.Map;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.RequestQueue;

class Query implements IQuery {
    private String url;
    private RequestQueue queue;

    public Query(String url, RequestQueue queue) {
        this.url = url;
        this.queue = queue;
    }

    public String printfUrl(String weburl, Map<String, Object> map) {
        StringBuilder url = new StringBuilder(weburl).append("?");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        url.deleteCharAt(url.length() - 1);
        return url.toString();
    }


    @Override
    public IReq queryPost(final Map<String, Object> map, final OnRespListener listener) {
        CustomListener customListener = new CustomListener() {
            @Override
            public void onResponse(int state, String body, String msg) {
                listener.onResp(state, body, msg);

                try {
                    JSONObject mJson = JSON.parseObject(body);
                    Object data = mJson.get("data");
                    Log.i("tvliao", printfUrl(url, map) + "-" + String.valueOf(data));
                } catch (Exception e) {

                }
            }
        };
        CustomRequestPost request = new CustomRequestMap(url, map, customListener);
        request.setRetryPolicy(new MyRetryPolicy());
        Req req = new Req();
        req.bindCustomRequest(request);
        queue.add(request);


        return req;
    }

    @Override
    public IReq queryPost(final int timeout, final Map<String, Object> map, final OnRespListener listener) {
        CustomListener customListener = new CustomListener() {
            @Override
            public void onResponse(int state, String body, String msg) {
                listener.onResp(state, body, msg);

                try {
                    JSONObject mJson = JSON.parseObject(body);
                    Object data = mJson.get("data");
                    Log.i("tvliao", printfUrl(url, map) + "-" + String.valueOf(data));
                } catch (Exception e) {

                }
            }
        };
        CustomRequestPost request = new CustomRequestMap(url, map, customListener);
        request.setRetryPolicy(new MyRetryPolicy(timeout, MyRetryPolicy.DEFAULT_MAX_RETRIES, MyRetryPolicy.DEFAULT_BACKOFF_MULT));
        Req req = new Req();
        req.bindCustomRequest(request);
        queue.add(request);


        return req;
    }

    @Override
    public IReq queryGet(final OnRespListener listener, String cookie) {

        CustomListener customListener = new CustomListener() {
            @Override
            public void onResponse(int state, String body, String msg) {
                listener.onResp(state, body, msg);
            }
        };
        CustomRequestGet request = new CustomRequestGet(url, customListener, null);

        request.setCookie(cookie);
        request.setRetryPolicy(new MyRetryPolicy());
        Req req = new Req();
        req.bindCustomRequest(request);
        queue.add(request);
        return req;
    }
}