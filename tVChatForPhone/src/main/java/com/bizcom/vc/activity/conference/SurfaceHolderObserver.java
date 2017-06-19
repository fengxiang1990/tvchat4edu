package com.bizcom.vc.activity.conference;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.DeviceRequest;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.V2Log;
import com.bizcom.vo.Group;
import com.bizcom.vo.UserDeviceConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import v2av.VideoPlayer;

public class SurfaceHolderObserver implements SurfaceHolder.Callback {

	private final static int OPEN_DEVICE_DONE = 1;
	private final static int CLOSE_DEVICE_DONE = 2;

	private DeviceRequest service;
	private Handler mLocalHandler;

	public void setUdc(UserDeviceConfig udc) {
		this.udc = udc;
	}

	private UserDeviceConfig udc;
	// FIXME this observer should not care this
	private Group g;
	private State state;
	private Lock mLock;
	public boolean isCreate;

	public SurfaceHolderObserver(Group g, DeviceRequest service,
			UserDeviceConfig udc) {
		super();
		this.g = g;
		this.service = service;
		this.udc = udc;
		state = State.CLOSED;
		mLock = new ReentrantLock();
		mLocalHandler = new LocalHandler(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mLock.lock();
		V2Log.d("VideoPlayer", " surfaceChanged : " + this);
		try {
			if (!isCreate || udc == null || udc.getVp() == null)
				return;

			if (state == State.CLOSED || state == State.CLOSING) {
				state = State.SHOWING;
				udc.getVp().setSurfaceViewSize(width, height);
				V2Log.d("VideoPlayer", "updateDeviceState false!");
				udc.getVp().setSurfaceHolder(holder);
				updateDeviceState(false);
				service.requestOpenVideoDevice(g, udc, new HandlerWrap(
						mLocalHandler, OPEN_DEVICE_DONE, null));
			}
		} finally {
			mLock.unlock();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mLock.lock();
		V2Log.d("VideoPlayer", " surfaceCreated : " + this);
		try {
			if (state == State.CLOSED || state == State.CLOSING) {
				isCreate = true;
				state = State.SHOWING;
				udc.getSVHolder().setZOrderMediaOverlay(false);
				if(udc.getVp() == null){
					udc.setVp(new VideoPlayer());
				}
				udc.getVp().setSurfaceHolder(holder);
				updateDeviceState(false);
				service.requestOpenVideoDevice(g, udc, new HandlerWrap(
						mLocalHandler, OPEN_DEVICE_DONE, null));
			}
		} finally {
			mLock.unlock();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		 mLock.lock();
		V2Log.d("VideoPlayer", " surfaceDestroyed : " + this);
		 try{
		 if (state == State.SHOWED || state == State.SHOWING) {
		  isCreate = false;
		  state = State.CLOSING;
		  updateDeviceState(true);
		  Log.d("VideoPlayer" , "surfaceDestroyed invoke jni stop video!");
		  service.requestCloseVideoDevice(g, udc, new HandlerWrap(
				  mLocalHandler, CLOSE_DEVICE_DONE, null));
		 }
		 }finally {
		 mLock.unlock();
		 }
	}

	private void updateDeviceState(boolean suspend) {
		if (udc.getVp() != null) {
			udc.getVp().setSuspended(suspend);
		}
	}

	private void receiveMessage(Message msg) {
		JNIResponse.Result res = ((JNIResponse) msg.obj).getResult();
		switch (msg.what) {
		case OPEN_DEVICE_DONE:
			if (res == JNIResponse.Result.SUCCESS) {
				state = State.SHOWED;
			} else {
				state = State.CLOSED;
			}
			break;
		case CLOSE_DEVICE_DONE:
			if (res == JNIResponse.Result.SUCCESS) {
				state = State.CLOSED;
			}
			break;
		}
	}

	enum State {
		SHOWED, SHOWING, CLOSED, CLOSING
	}

	private static class LocalHandler extends Handler {
		private final WeakReference<SurfaceHolderObserver> mActivity;

		public LocalHandler(SurfaceHolderObserver activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mActivity.get() == null) {
				return;
			}
			mActivity.get().receiveMessage(msg);
		}
	}
}
