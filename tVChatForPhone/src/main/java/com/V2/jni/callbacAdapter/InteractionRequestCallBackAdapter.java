package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.InteractionRequestCallBack;

public abstract class InteractionRequestCallBackAdapter implements InteractionRequestCallBack {

	@Override
	public void OnStartLive(long nUserID, String url) {

	}

	@Override
	public void OnStopLive(long nUserID) {

	}

	@Override
	public void OnGPSUpdated() {

	}

	@Override
	public void OnGetNeiborhood(String resultXml) {

	}

	@Override
	public void OnCommentVideo(long nUserID, String szCommentXml) {

	}

	@Override
	public void OnAddConcern(long nSrcUserID, long nDstUserID) {

	}

	@Override
	public void OnCancelConcernl(long nSrcUserID, long nDstUserID) {

	}

	@Override
	public void OnMyConcerns(String szConcernsXml) {

	}

	@Override
	public void OnMyFans(String szFansXml) {

	}

	@Override
	public void OnFansCount(String szFansXml) {

	}

}
