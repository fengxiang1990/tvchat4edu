package com.bizcom.vc.hg.web;

import java.util.Map;

/**
 * 服务器响应监听器
 * 
 * @author chentangzheng
 * 
 */
public interface OnRespListener {
	/**
	 * 服务器响应回调
	 * 
	 * @param status
	 *            返回状态
	 * @param body
	 *            响应内容
	 */
	public void onResp(int status, String result, String msg);

	public void onResp(int status, Map<String, Object> result, String msg);
}