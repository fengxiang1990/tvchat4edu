package com.bizcom.vc.hg.web;
/** Callback interface for delivering parsed responses. */
public interface CustomListener {
	/**
	 * response响应时返回
	 */
	public void onResponse(int state, String body, String msg);
}