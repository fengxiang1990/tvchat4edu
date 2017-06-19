package com.bizcom.request;

import android.os.Message;
import android.util.Log;

import com.V2.jni.ImRequest;
import com.V2.jni.callbacAdapter.ImRequestCallbackAdapter;
import com.V2.jni.ind.V2ClientType;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestLogInResponse;
import com.bizcom.request.jni.RequestUserUpdateResponse;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.V2Log;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class V2ImRequest extends V2AbstractHandler {

	public static final int NATIVE_GET_USER_INFO = 1;
	public static final int NATIVE_GET_USER_AVATAR = 2;
	public static final int NATIVE_CHANGE_OWNER_AVATAR = 3;
	public static final int NATIVE_CANNEL_LOGIN = 4;

	// 此处的消息类型只与AbstractHandler的REQUEST_TIME_OUT消息并列。
	// 与参数caller中的消息类型what完全没有关系，上层传什么消息what回调就是什么消息what
	private static final int JNI_REQUEST_LOG_IN = 1;
	private static final int JNI_REQUEST_UPDAE_USER = 2;
	public static final int JNI_REQUEST_CREATE_VALIDATE = 3;
	public static final int JNI_REQUEST_REGIST_RETURN = 4;
	public static final int JNI_REQUEST_UPDATE_PASSWORD = 5;

	private ImRequestCB imCB = null;

	public V2ImRequest() {
		super();
		imCB = new ImRequestCB();
		ImRequest.getInstance().addCallback(imCB);
	}

	public static void invokeNative(int nativeMethod, Object... obj) {
		switch (nativeMethod) {
		// V2ImRequest.invokeNative(V2ImRequest.NATIVE_GET_USER_INFO ,
		case NATIVE_GET_USER_INFO:
			ImRequest.getInstance().ImGetUserBaseInfo((Long) obj[0]);
			break;
		case NATIVE_GET_USER_AVATAR:
			ImRequest.getInstance().ImGetAvatar((Long) obj[0], (String) obj[1], (String) obj[2]);
			break;
		case NATIVE_CHANGE_OWNER_AVATAR:
			Log.d("icon","调用jni上传图片");
			ImRequest.getInstance().ImChangeCustomAvatar((byte[]) obj[0], (Integer) obj[1], (String) obj[2]);
			break;
		case NATIVE_CANNEL_LOGIN:
			ImRequest.getInstance().ImLogout();
			break;
		default:
			break;
		}
	}

	public void login(String mail, String passwd, String registrationID , HandlerWrap caller) {
		initTimeoutMessage(JNI_REQUEST_LOG_IN, DEFAULT_TIME_OUT_SECS, caller);
		ImRequest.getInstance().ImLogin(mail, passwd, V2GlobalConstants.USER_STATUS_ONLINE, V2ClientType.ANDROID, registrationID,
				false);
	}

	public void updateUserInfo(User user, HandlerWrap caller) {
		if (user == null) {
			if (caller != null && caller.getHandler() != null) {
				callerSendMessage(caller,
						new RequestLogInResponse(null, RequestLogInResponse.Result.INCORRECT_PAR, caller.getObject()));
			}
			return;
		}
		initTimeoutMessage(JNI_REQUEST_UPDAE_USER, DEFAULT_TIME_OUT_SECS, caller);
		if (user.getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			ImRequest.getInstance().ImModifyBaseInfo(user.toXml());
		} else {
			ImRequest.getInstance().ImChangeFriendMemoName(user.getmUserId(),
					EscapedcharactersProcessing.convert(user.getCommentName() == null ? "" : user.getCommentName()));
		}
	}

	public void imRequestCreateValidate(String mobileNumber, HandlerWrap caller , int timeOut) {
		if (!checkParamNull(caller, new Object[] { mobileNumber })) {
			return;
		}

		initTimeoutMessage(JNI_REQUEST_CREATE_VALIDATE, timeOut, caller);
		ImRequest.getInstance().ImCreateValidateCode(mobileNumber);
	}

	public void imRequestRegistUser(String mobileNumber, int validateCode, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { mobileNumber, validateCode })) {
			return;
		}

		initTimeoutMessage(JNI_REQUEST_REGIST_RETURN, DEFAULT_TIME_OUT_SECS, caller);
		ImRequest.getInstance().ImRegisterPhoneUser(mobileNumber, validateCode);
	}

	public void imRequestUpdatePassWord(String oldPwd, String newPwd, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { oldPwd, newPwd })) {
			return;
		}

		initTimeoutMessage(JNI_REQUEST_UPDATE_PASSWORD, DEFAULT_TIME_OUT_SECS, caller);
		ImRequest.getInstance().ImUpdatePhoneUserPwd(oldPwd, newPwd);
	}

    public void imRequestRegistGuestUser(String mNickName, HandlerWrap caller) {
        if (!checkParamNull(caller, new Object[] { mNickName })) {
            return;
        }

        initTimeoutMessage(JNI_REQUEST_REGIST_RETURN, DEFAULT_TIME_OUT_SECS, caller);
        ImRequest.getInstance().ImRegisterGuest(mNickName);
    }

    @Override
	public void clearCalledBack() {
		ImRequest.getInstance().removeCallback(imCB);
	}

	private class ImRequestCB extends ImRequestCallbackAdapter {

		@Override
		public void OnLoginCallback(long nUserID, int nStatus, int nResult, long serverTime, String sDBID) {
			if (!GlobalConfig.isLogined) {
				// 获取系统时间
				GlobalConfig.recordLoginTime(serverTime);
				SimpleDateFormat fromat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = fromat.format(new Date(GlobalConfig.LONGIN_SERVER_TIME * 1000));
				V2Log.d("get server time ：" + date);
				GlobalHolder.getInstance().mCurrentUserId = nUserID;
				RequestLogInResponse.Result res = RequestLogInResponse.Result.fromInt(nResult);
				Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_LOG_IN,
						new RequestLogInResponse(new User(nUserID), sDBID, res));
				dispatchMessage(m);
			}
		}

		@Override
		public void OnConnectResponseCallback(int nResult) {
			RequestLogInResponse.Result res = RequestLogInResponse.Result.fromInt(nResult);
			if (res != RequestLogInResponse.Result.SUCCESS) {
				Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_LOG_IN, new RequestLogInResponse(null, res));
				dispatchMessage(m);
			}
		}

		@Override
		public void OnModifyCommentNameCallback(long nUserId, String sCommmentName) {
			super.OnModifyCommentNameCallback(nUserId, sCommmentName);
			User u = GlobalHolder.getInstance().getUser(nUserId);
			Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_UPDAE_USER,
                    new RequestUserUpdateResponse(u, JNIResponse.Result.SUCCESS));
			dispatchMessage(m);
		}

		@Override
		public void OnUpdateBaseInfoCallback(long nUserID, String updatexml) {
			if (nUserID != GlobalHolder.getInstance().getCurrentUserId()) {
				return;
			}
			Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_UPDAE_USER,
					new JNIResponse(JNIResponse.Result.SUCCESS));
			dispatchMessage(m);
		}

		@Override
		public void OnImUserCreateValidateCode(int ret) {
			super.OnImUserCreateValidateCode(ret);
			JNIResponse jniResponse = new JNIResponse(JNIResponse.Result.SUCCESS);
			jniResponse.resObj = ret;
			Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_CREATE_VALIDATE, jniResponse);
			dispatchMessage(m);
		}

		@Override
		public void OnImRegisterPhoneUser(int ret) {
			super.OnImRegisterPhoneUser(ret);
			JNIResponse jniResponse = new JNIResponse(JNIResponse.Result.SUCCESS);
			jniResponse.resObj = ret;
			Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_REGIST_RETURN, jniResponse);
			dispatchMessage(m);
		}

        @Override
		public void OnImUpdateUserPwd(int ret) {
			super.OnImUpdateUserPwd(ret);
			JNIResponse jniResponse = new JNIResponse(JNIResponse.Result.SUCCESS);
			jniResponse.resObj = ret;
			Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_UPDATE_PASSWORD, jniResponse);
			dispatchMessage(m);
		}

        @Override
        public void OnImRegisterGuest(String sAccount, String sPwd, int ret) {
            super.OnImRegisterGuest(sAccount, sPwd, ret);
            JNIResponse jniResponse = new JNIResponse(JNIResponse.Result.SUCCESS);
            jniResponse.resObj = new String[]{sAccount , sPwd , String.valueOf(ret)};
            Message m = Message.obtain(V2ImRequest.this, JNI_REQUEST_REGIST_RETURN, jniResponse);
            dispatchMessage(m);
        }

		@Override
		public void OnChangeAvatarCallback(int nAvatarType, long nUserID, String AvatarName) {
			super.OnChangeAvatarCallback(nAvatarType, nUserID, AvatarName);
			Log.d("","");

		}
	}
}
