package com.bizcom.vc.hg.web;


/**
 * 请求返回句柄实现类
 * 
 * @author zzh
 * 
 */
final class Req implements IReq {

	private CustomRequestPost req;

	protected void bindCustomRequest(CustomRequestPost req) {
		this.req = req;
	}
	private CustomRequestGet req2;
	
	protected void bindCustomRequest(CustomRequestGet req2) {
		this.req2 = req2;
	}

	public void cancel() {
		req.cancel();
	}
}