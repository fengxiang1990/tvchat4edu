package com.bizcom.vc.hg.web;

import java.util.Map;


/**
 * 请求代理类
 * 
 * @author zhoukang
 * 
 */
public interface IQuery {
	/**
	 * post请求
	 */
	public IReq queryPost(Map<String,Object>param, OnRespListener listener);

	/**
	 * post请求
	 */
	public IReq queryPost(int timeout,Map<String,Object>param, OnRespListener listener);
	/**
	 * get请求
	 */
	public IReq queryGet(OnRespListener listener,String cookie);
}