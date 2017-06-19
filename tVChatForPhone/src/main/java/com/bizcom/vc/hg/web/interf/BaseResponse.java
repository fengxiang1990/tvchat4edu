package com.bizcom.vc.hg.web.interf;

/**
 * Created by admin on 2016/12/17.
 */

public class BaseResponse<T> {


    public String code;

    public String msg;

    public T data;

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
