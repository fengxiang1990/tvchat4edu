package com.bizcom.request.util;

import android.os.Message;

import com.V2.jni.VideoRequest;
import com.bizcom.request.V2AbstractHandler;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestCloseUserVideoDeviceResponse;
import com.bizcom.request.jni.RequestConfCreateResponse;
import com.bizcom.request.jni.RequestOpenUserVideoDeviceResponse;
import com.bizcom.request.jni.RequestUpdateCameraParametersResponse;
import com.bizcom.util.V2Log;
import com.bizcom.vo.CameraConfiguration;
import com.bizcom.vo.Group;
import com.bizcom.vo.UserDeviceConfig;

/**
 * Device function class.
 * <ul>
 * </ul>
 * 
 * @author 28851274
 * 
 */
public abstract class DeviceRequest extends V2AbstractHandler {

	private static final int JNI_REQUEST_OPEN_VIDEO = 0x00100001;
	private static final int JNI_REQUEST_CLOSE_VIDEO = 0x00100002;
	private static final int JNI_UPDATE_CAMERA_PAR = 0x00100003;

	/**
	 * User request to open video device.
	 * 
	 * @param userDevice
	 *            {@link UserDeviceConfig} if want to open local video,
	 *            {@link UserDeviceConfig#getVp()} should be null and
	 *            {@link UserDeviceConfig#getDeviceID()} should be ""
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.bizcom.request.jni.RequestOpenUserVideoDeviceResponse}
	 * 
	 * @see UserDeviceConfig
	 */
	public void requestOpenVideoDevice(UserDeviceConfig userDevice, HandlerWrap caller) {
		if (userDevice == null) {
			JNIResponse jniRes = new RequestOpenUserVideoDeviceResponse(0,
					RequestConfCreateResponse.Result.INCORRECT_PAR);
			callerSendMessage(caller, jniRes);
			return;
		}
		initTimeoutMessage(JNI_REQUEST_OPEN_VIDEO, DEFAULT_TIME_OUT_SECS, caller);
		V2Log.i(" request open video   UID:" + userDevice.getUserID() + " deviceid:" + userDevice.getDeviceID()
				+ "   videoplayer:" + userDevice.getVp());
		VideoRequest.getInstance().VideoOpenDevice(0, 0, userDevice.getType(), userDevice.getUserID(),
				userDevice.getDeviceID(), userDevice.getVp());
		JNIResponse jniRes = new RequestOpenUserVideoDeviceResponse(System.currentTimeMillis() / 1000,
				RequestOpenUserVideoDeviceResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_OPEN_VIDEO, jniRes);
		this.sendMessageDelayed(res, 300);

	}

	/**
	 * User request to close video device.
	 * 
	 * @param userDevice
	 *            {@link UserDeviceConfig} if want to open local video,
	 *            {@link UserDeviceConfig#getVp()} should be null and
	 *            {@link UserDeviceConfig#getDeviceID()} should be ""
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.bizcom.request.jni.RequestCloseUserVideoDeviceResponse}
	 * 
	 * @see UserDeviceConfig
	 */
	public void requestCloseVideoDevice(UserDeviceConfig userDevice, HandlerWrap caller) {
		if (userDevice == null) {
			JNIResponse jniRes = new RequestCloseUserVideoDeviceResponse(System.currentTimeMillis() / 1000,
					RequestCloseUserVideoDeviceResponse.Result.INCORRECT_PAR);
			callerSendMessage(caller, jniRes);
			return;
		}
		initTimeoutMessage(JNI_REQUEST_CLOSE_VIDEO, DEFAULT_TIME_OUT_SECS, caller);

		V2Log.i("nDeviceType : " + userDevice.getType() + " - nUserID : " + userDevice.getUserID()
				+ " - DeviceID : " + userDevice.getDeviceID() + " - videoplay : " + userDevice.getVp());
		VideoRequest.getInstance().VideoCloseDevice(0, 0, userDevice.getType(), userDevice.getUserID(),
				userDevice.getDeviceID() == null ? "" : userDevice.getDeviceID(), userDevice.getVp());
		JNIResponse jniRes = new RequestCloseUserVideoDeviceResponse(System.currentTimeMillis() / 1000,
				RequestCloseUserVideoDeviceResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_CLOSE_VIDEO, jniRes);
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * User request to open video device.
	 * 
	 * @param group
	 *            If user not IM, use this
	 * @param userDevice
	 *            {@link UserDeviceConfig} if want to open local video,
	 *            {@link UserDeviceConfig#getVp()} should be null and
	 *            {@link UserDeviceConfig#getDeviceID()} should be ""
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.bizcom.request.jni.RequestOpenUserVideoDeviceResponse}
	 * 
	 * @see UserDeviceConfig
	 */
	public void requestOpenVideoDevice(Group group, UserDeviceConfig userDevice, HandlerWrap caller) {
		if (userDevice == null || group == null) {
			JNIResponse jniRes = new RequestOpenUserVideoDeviceResponse(0,
					RequestConfCreateResponse.Result.INCORRECT_PAR);
			callerSendMessage(caller, jniRes);
			return;
		}
		initTimeoutMessage(JNI_REQUEST_OPEN_VIDEO, DEFAULT_TIME_OUT_SECS, caller);
		V2Log.i(V2Log.JNI_REQUEST, " request open video   UID:" + userDevice.getUserID() + " deviceid:"
				+ userDevice.getDeviceID() + "   videoplayer:" + userDevice.getVp());
		VideoRequest.getInstance().VideoOpenDevice(group.getGroupType(), group.getGroupID(), userDevice.getType(),
				userDevice.getUserID(), userDevice.getDeviceID(), userDevice.getVp());
		JNIResponse jniRes = new RequestOpenUserVideoDeviceResponse(System.currentTimeMillis() / 1000,
				RequestOpenUserVideoDeviceResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_OPEN_VIDEO, jniRes);
		this.sendMessageDelayed(res, 300);

	}

	/**
	 * User request to close video device.
	 * 
	 * @param group
	 *            if user not for IM, use this API
	 * 
	 * @param userDevice
	 *            {@link UserDeviceConfig} if want to open local video,
	 *            {@link UserDeviceConfig#getVp()} should be null and
	 *            {@link UserDeviceConfig#getDeviceID()} should be ""
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.bizcom.request.jni.RequestCloseUserVideoDeviceResponse}
	 * 
	 * @see UserDeviceConfig
	 */
	public void requestCloseVideoDevice(Group group, UserDeviceConfig userDevice, HandlerWrap caller) {
		if (userDevice == null || group == null) {
			JNIResponse jniRes = new RequestCloseUserVideoDeviceResponse(System.currentTimeMillis() / 1000,
					RequestCloseUserVideoDeviceResponse.Result.INCORRECT_PAR);
			callerSendMessage(caller, jniRes);
			return;
		}
		initTimeoutMessage(JNI_REQUEST_CLOSE_VIDEO, DEFAULT_TIME_OUT_SECS, caller);

		VideoRequest.getInstance().VideoCloseDevice(group.getGroupType(), group.getGroupID(), userDevice.getType(),
				userDevice.getUserID(), userDevice.getDeviceID(), userDevice.getVp());
		JNIResponse jniRes = new RequestCloseUserVideoDeviceResponse(System.currentTimeMillis() / 1000,
				RequestCloseUserVideoDeviceResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_CLOSE_VIDEO, jniRes);
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * Update current user's camera. Including front-side or back-side camera
	 * switch.
	 * 
	 * @param cc
	 *            {@link CameraConfiguration}
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.bizcom.request.jni.RequestUpdateCameraParametersResponse}
	 */
	public void updateCameraParameters(CameraConfiguration cc, HandlerWrap caller) {
		if (cc == null) {
			JNIResponse jniRes = new RequestUpdateCameraParametersResponse(null,
					RequestCloseUserVideoDeviceResponse.Result.INCORRECT_PAR);
			callerSendMessage(caller, jniRes);
			return;
		}
		initTimeoutMessage(JNI_UPDATE_CAMERA_PAR, DEFAULT_TIME_OUT_SECS, caller);
		VideoRequest.getInstance().VideoSwitchCamera(cc.getPlayer());
	}

	public void requestPausePlayout(UserDeviceConfig userDevice) {
		if (userDevice == null) {
			return;
		}

		VideoRequest.getInstance().PausePlayout(userDevice.getDeviceID());
	}

	public void requestResumePlayout(UserDeviceConfig userDevice) {
		if (userDevice == null) {
			return;
		}

		VideoRequest.getInstance().ResumePlayout(userDevice.getDeviceID());
	}

	@Override
	public void clearCalledBack() {

	}

}
