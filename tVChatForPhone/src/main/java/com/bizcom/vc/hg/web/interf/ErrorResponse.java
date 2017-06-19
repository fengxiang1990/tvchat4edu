package com.bizcom.vc.hg.web.interf;

/**
 * Created by fengxiang on 2016/12/17.
 */

public class ErrorResponse extends Exception {
    public String message;


    public ErrorResponse(String message) {
        super(message);
        this.message = message;
    }

}
