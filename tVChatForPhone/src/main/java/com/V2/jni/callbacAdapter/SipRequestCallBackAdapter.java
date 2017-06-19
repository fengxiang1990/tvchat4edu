package com.V2.jni.callbacAdapter;

import com.V2.jni.callback.SipRequestCallback;
import com.bizcom.util.V2Log;

public abstract class SipRequestCallBackAdapter implements SipRequestCallback {
	@Override
	public void OnAcceptSipCall(String szURI , boolean isVideoCall) {
		V2Log.jniCall("OnAcceptSipCall", " szURI = " + szURI + " | isVideoCall : " + isVideoCall);
	}

	@Override
	public void OnInviteSipCall(String szURI) {
		V2Log.jniCall("OnInviteSipCall", " szURI = " + szURI);
	}

	@Override
	public void OnFailureSipCall(String szURI, int nErrorCode) {
		V2Log.jniCall("OnFailureSipCall", " szURI = " + szURI + " | nErrorCode = " + nErrorCode);
	}

	@Override
	public void OnCloseSipCall(String szURI) {
		V2Log.jniCall("OnCloseSipCall", " szURI = " + szURI);
	}

	@Override
	public void OnSipMicMaxVolume(int nVolume) {
		V2Log.jniCall("OnSipMicMaxVolume", " nVolume = " + nVolume);
	}

	@Override
	public void OnSipSpeakerVolum(int nVolume) {
		V2Log.jniCall("OnSipSpeakerVolum", " nVolume = " + nVolume);
	}

	@Override
	public void OnSipMuteMic(boolean bMute) {
		V2Log.jniCall("OnSipMuteMic", " bMute = " + bMute);
	}

	@Override
	public void OnSipMuteSpeaker(boolean bMute) {
		V2Log.jniCall("OnSipMuteSpeaker", " bMute = " + bMute);
	}

	@Override
	public void OnSipSipCurrentLevel(int nSpeakerLevel, int nMicLevel) {
		V2Log.jniCall("OnSipSipCurrentLevel", " nSpeakerLevel = " + nSpeakerLevel + " | nMicLevel = " + nMicLevel);
	}
}
