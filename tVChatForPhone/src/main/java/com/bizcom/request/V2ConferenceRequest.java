package com.bizcom.request;

import java.util.List;

import com.V2.jni.AudioRequest;
import com.V2.jni.ConfRequest;
import com.V2.jni.GroupRequest;
import com.V2.jni.VideoMixerRequest;
import com.V2.jni.VideoRequest;
import com.V2.jni.callbacAdapter.ConfRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.GroupRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.VideoRequestCallbackAdapter;
import com.V2.jni.callback.VideoMixerRequestCallback;
import com.V2.jni.ind.BoUserInfoShort;
import com.V2.jni.ind.V2Group;
import com.bizcom.request.jni.JNIIndication;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.PermissionRequestIndication;
import com.bizcom.request.jni.PermissionUpdateIndication;
import com.bizcom.request.jni.RequestConfChairManChange;
import com.bizcom.request.jni.RequestConfCreateResponse;
import com.bizcom.request.jni.RequestEnterConfResponse;
import com.bizcom.request.jni.RequestExitedConfResponse;
import com.bizcom.request.jni.RequestPermissionResponse;
import com.bizcom.request.jni.RequestUpdateCameraParametersResponse;
import com.bizcom.request.util.DeviceRequest;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.V2Log;
import com.bizcom.util.XmlAttributeExtractor;
import com.bizcom.vo.CameraConfiguration;
import com.bizcom.vo.Conference;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.MixVideo;
import com.bizcom.vo.User;
import com.bizcom.vo.UserDeviceConfig;
import com.bizcom.vo.enums.ConferencePermission;
import com.bizcom.vo.enums.MixVideoLayoutType;
import com.bizcom.vo.enums.PermissionState;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

/**
 * <ul>
 * This class is use to conference business.
 * </ul>
 * <ul>
 * When user entered conference room, user can use
 * {@link #requestOpenVideoDevice(Conference, UserDeviceConfig, Message)} and
 * {@link #requestCloseVideoDevice(Conference, UserDeviceConfig, Message)} to
 * open or close video include self.
 * </ul>
 * <ul>
 * <li>User request to enter conference :
 * {@link #requestEnterConference(Conference, HandlerWrap)}</li>
 * <li>User request to exit conference :
 * {@link #requestExitConference(Conference, HandlerWrap)}</li>
 * <li>User request to request speak in meeting
 * {@link #applyForControlPermission(ConferencePermission, HandlerWrap)}</li>
 * <li>User request to release speaker in meeting
 * {@link #applyForReleasePermission(ConferencePermission, HandlerWrap)}</li>
 * <li>User create conference:
 * {@link #createConference(Conference, HandlerWrap)}</li>
 * </ul>
 * 
 * @author 28851274
 * 
 */
public class V2ConferenceRequest extends DeviceRequest {

	private static final int JNI_REQUEST_ENTER_CONF = 1;
	private static final int JNI_REQUEST_EXIT_CONF = 2;
	private static final int JNI_REQUEST_SPEAK = 5;
	private static final int JNI_REQUEST_RELEASE_SPEAK = 6;
	private static final int JNI_REQUEST_CREATE_CONFERENCE = 7;
	private static final int JNI_REQUEST_QUIT_CONFERENCE = 8;
	private static final int JNI_REQUEST_INVITE_ATTENDEES = 9;
	private static final int JNI_REQUEST_GRANT_PERMISSION = 10;

	private static final int JNI_UPDATE_CAMERA_PAR = 75;

	private static final int KEY_KICKED_LISTNER = 100;
	private static final int KEY_ATTENDEE_DEVICE_LISTNER = 101;
	private static final int KEY_ATTENDEE_ENTER_OR_EXIT_LISTNER = 102;
	private static final int KEY_SYNC_STATE_LISTNER = 103;
	private static final int KEY_PERMISSION_CHANGED_LISTNER = 104;
	private static final int KEY_MIXED_VIDEO_LISTNER = 105;
	private static final int KEY_LECTURE_REQUEST_LISTNER = 106;
	private static final int KEY_CHAIRMAN_CHANGE_LISTNER = 107;
	private static final int KEY_VOICEACTIVATION_LISTNER = 207;
	private static final int KEY_INVITATION_STATE_LISTNER = 208;

	private VideoRequestCB videoCallback;
	private ConfRequestCB confCallback;
	private GroupRequestCB groupCallback;
	private MixerRequestCB mrCallback;

	private boolean mFlag = false;

	public V2ConferenceRequest() {
		this(false);
	}

	public V2ConferenceRequest(boolean flag) {
		super();
		videoCallback = new VideoRequestCB(this);
		VideoRequest.getInstance().addCallback(videoCallback);
		confCallback = new ConfRequestCB(this);
		ConfRequest.getInstance().addCallback(confCallback);
		groupCallback = new GroupRequestCB(this);
		GroupRequest.getInstance().addCallback(groupCallback);
		mrCallback = new MixerRequestCB(this);
		VideoMixerRequest.getInstance().addCallbacks(mrCallback);
		mFlag = flag;
	}

	/**
	 * User request to enter conference.<br>
	 * 
	 * @param conf
	 *            {@link Conference} object which user wants to enter
	 * @param caller
	 *            if input is null, ignore response. Message.object is
	 *            {@link com.bizcom.request.jni.RequestEnterConfResponse}
	 * 
	 * @see com.bizcom.request.jni.RequestEnterConfResponse
	 */
	public void requestEnterConference(Conference conf, HandlerWrap caller) {
		initTimeoutMessage(JNI_REQUEST_ENTER_CONF, DEFAULT_TIME_OUT_SECS, caller);
		ConfRequest.getInstance().ConfEnter(conf.getId());
	}

	/**
	 * User request to quit conference. This API just use to for quit conference
	 * this time.<br>
	 * User will receive this conference when log in next time.
	 * 
	 * @param conf
	 *            {@link Conference} object which user wants to enter
	 * @param caller
	 *            if input is null, ignore response Message. Response Message
	 *            object is
	 *            {@link com.bizcom.request.jni.RequestExitedConfResponse}
	 */
	public void requestExitConference(Conference conf, HandlerWrap caller) {
		if (conf == null) {
			if (caller != null && caller.getHandler() != null) {
				JNIResponse jniRes = new RequestConfCreateResponse(0, 0,
						RequestConfCreateResponse.Result.INCORRECT_PAR);
				callerSendMessage(caller, jniRes);
			}
			return;
		}
		initTimeoutMessage(JNI_REQUEST_EXIT_CONF, DEFAULT_TIME_OUT_SECS, caller);
		ConfRequest.getInstance().ConfExit(conf.getId());
		// send response to caller because exitConf no call back from JNI
		JNIResponse jniRes = new RequestExitedConfResponse(conf.getId(), System.currentTimeMillis() / 1000,
				JNIResponse.Result.SUCCESS);
		Message res = Message.obtain(this, JNI_REQUEST_EXIT_CONF, jniRes);
		// send delayed message for that make sure send response after JNI
		// request
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * Create conference.
	 * <ul>
	 * </ul>
	 * 
	 * @param conf
	 *            {@link Conference} object.
	 * @param caller
	 *            if input is null, ignore response Message. Response Message
	 *            object is
	 *            {@link com.bizcom.request.jni.RequestConfCreateResponse}
	 */
	public void createConference(Conference conf, HandlerWrap caller) {
		if (conf == null) {
			if (caller != null && caller.getHandler() != null) {
				JNIResponse jniRes = new RequestConfCreateResponse(0, 0, RequestConfCreateResponse.Result.FAILED);
				callerSendMessage(caller, jniRes);
			}
			return;
		}
		initTimeoutMessage(JNI_REQUEST_CREATE_CONFERENCE, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupCreate(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getConferenceConfigXml(),
				conf.getInvitedAttendeesXml());
	}

	/**
	 * User request to quit this conference for ever.<br>
	 * User never receive this conference information any more.
	 * 
	 * @param conf
	 * @param caller
	 */
	public void quitConference(Conference conf, HandlerWrap caller) {
		if (conf == null) {
			if (caller != null) {
				JNIResponse jniRes = new RequestConfCreateResponse(0, 0,
						RequestConfCreateResponse.Result.INCORRECT_PAR);
				callerSendMessage(caller, jniRes);
			}
			return;
		}
		initTimeoutMessage(JNI_REQUEST_QUIT_CONFERENCE, DEFAULT_TIME_OUT_SECS, caller);
		// If conference owner is self, then delete group
		if (conf.getCreator() == GlobalHolder.getInstance().getCurrentUserId()) {
			GroupRequest.getInstance().GroupDestroy(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId());
			// If conference owner isn't self, just leave group
		} else {
			GroupRequest.getInstance().GroupLeave(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId());
		}
	}

	/**
	 * Chair man invite extra attendee to join current conference.<br>
	 * 
	 * @param conf
	 *            conference which user current joined
	 * @param list
	 *            additional attendee
	 * @param caller
	 *            caller
	 */
	public void inviteAttendee(Conference conf, List<User> list, HandlerWrap caller) {
		if (list == null || conf == null || list.isEmpty()) {
			if (caller != null) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.INCORRECT_PAR);
				callerSendMessage(caller, jniRes);
			}
			return;
		}
		String sXml = XmlAttributeExtractor.buildAttendeeUsersXml(list);
		GroupRequest.getInstance().GroupInviteUsers(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getConferenceConfigXml(),
				sXml, "");

		// send response to caller because invite attendee no call back from JNI
		JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
		Message res = Message.obtain(this, JNI_REQUEST_INVITE_ATTENDEES, jniRes);
		// send delayed message for that make sure send response after JNI
		// request
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * 会场静音
	 */
	public void muteConf() {
		ConfRequest.getInstance().ConfMute();
	}

	public void notifyAllMessage(long nGroupID) {
		ConfRequest.getInstance().NotifyConfAllMessage(nGroupID);
	}

	public void enableVideoDev(String szDeviceID, boolean bInuse) {
		int _bInuse = 0;
		if (bInuse) {
			_bInuse = 1;
		} else {
			_bInuse = 0;
		}
		VideoRequest.getInstance().EnableVideoDev(szDeviceID, _bInuse);
	}

	public void delVideoMixerDevID(String szMediaId, long dstUserId, String dstDevId) {
		// VideoMixerRequest.getInstance().delVideoMixerDevID(szMediaId,
		// dstUserId, dstDevId);
	}

	/**
	 * User request speak permission on the conference.
	 * 
	 * @param type
	 *            speak type should be {@link ConferencePermission#SPEAKING}
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.bizcom.request.jni.RequestPermissionResponse}
	 * 
	 * @see ConferencePermission
	 */
	public void applyForControlPermission(ConferencePermission type, HandlerWrap caller) {
		initTimeoutMessage(JNI_REQUEST_SPEAK, DEFAULT_TIME_OUT_SECS, caller);

		ConfRequest.getInstance().ConfApplyPermission(type.intValue());

		JNIResponse jniRes = new RequestPermissionResponse(RequestPermissionResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_SPEAK, jniRes);
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * Request release permission on the conference.
	 * 
	 * @param type
	 *            speak type should be {@link ConferencePermission#SPEAKING}
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.bizcom.request.jni.RequestPermissionResponse}
	 * 
	 * @see ConferencePermission
	 */
	public void applyForReleasePermission(ConferencePermission type, HandlerWrap caller) {

		initTimeoutMessage(JNI_REQUEST_RELEASE_SPEAK, DEFAULT_TIME_OUT_SECS, caller);

		ConfRequest.getInstance().ConfReleasePermission(type.intValue());

		JNIResponse jniRes = new RequestPermissionResponse(RequestPermissionResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_RELEASE_SPEAK, jniRes);
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * grant user permission.
	 * 
	 * @param user
	 * @param type
	 * @param state
	 * @param caller
	 */
	public void grantPermission(User user, ConferencePermission type, PermissionState state, HandlerWrap caller) {
		if (user == null || state == null || type == null) {
			if (caller != null) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.INCORRECT_PAR);
				callerSendMessage(caller, jniRes);
			}
			return;
		}

		initTimeoutMessage(JNI_REQUEST_GRANT_PERMISSION, DEFAULT_TIME_OUT_SECS, caller);

		ConfRequest.getInstance().ConfGrantPermission(user.getmUserId(), type.intValue(), state.intValue());

		JNIResponse jniRes = new RequestPermissionResponse(RequestPermissionResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_GRANT_PERMISSION, jniRes);
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * Update conference attribute
	 * 
	 * @param conf
	 * @param isSync
	 * @param invitation
	 * @param caller
	 */
	public void updateConferenceAttribute(Conference conf, boolean isSync, boolean invitation, HandlerWrap caller) {
		if (conf == null) {
			if (caller != null) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.INCORRECT_PAR);
				callerSendMessage(caller, jniRes);
			}
			return;
		}

		GroupRequest.getInstance().GroupModify(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(),
				"<conf syncdesktop='" + (isSync ? "1" : "0") + "' inviteuser='" + (invitation ? "1" : "0") + "'/>");

		JNIResponse jniRes = new RequestPermissionResponse(RequestPermissionResponse.Result.SUCCESS);
		callerSendMessage(caller, jniRes);
	}

	public void modifyGroupLayout(Conference conf) {
		if (conf == null) {
			return;
		}
		GroupRequest.getInstance().GroupModify(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(),
				"<conf layout='67174414'/>");

	}

	/**
	 * Pause or resume audio.
	 * 
	 * @param flag
	 *            true for resume false for suspend
	 */
	public void updateAudio(boolean flag) {
		if (flag) {
			AudioRequest.getInstance().ResumePlayout();
		} else {
			AudioRequest.getInstance().PausePlayout();
		}
	}

	/**
	 * Shared image document for conference
	 * 
	 * @param conf
	 * @param file
	 * @param listener
	 */
	public void shareDoc(Conference conf, String file, HandlerWrap listener) {
		if (conf == null || file == null) {
			if (listener != null) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.INCORRECT_PAR);
				callerSendMessage(listener, jniRes);
			}
			return;
		}
        file = EscapedcharactersProcessing.convert(file);
		GroupRequest.getInstance().WBoardCreateDocShare(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(), file, false , false);
	}

	public void closeShareDoc(Conference conf, String szMediaID) {
		if (conf == null || szMediaID == null) {
			return;
		}
		GroupRequest.getInstance().WBoardDestroy(V2GlobalConstants.GROUP_TYPE_CONFERENCE, conf.getId(), szMediaID);
	}

	/**
	 * Register listener for out conference by kick.
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerKickedConfListener(Handler h, int what, Object obj) {
		registerListener(KEY_KICKED_LISTNER, h, what, obj);
	}

	public void removeRegisterOfKickedConfListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_KICKED_LISTNER, h, what, obj);

	}

	// =============================

	/**
	 * Register listener for out conference by kick.
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerAttendeeDeviceListener(Handler h, int what, Object obj) {
		registerListener(KEY_ATTENDEE_DEVICE_LISTNER, h, what, obj);
	}

	public void removeAttendeeDeviceListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_ATTENDEE_DEVICE_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for out conference by kick.
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerAttendeeEnterOrExitListener(Handler h, int what, Object obj) {
		registerListener(KEY_ATTENDEE_ENTER_OR_EXIT_LISTNER, h, what, obj);
	}

	public void removeAttendeeEnterOrExitListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_ATTENDEE_ENTER_OR_EXIT_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for chairman control or release desktop
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerSyncStateListener(Handler h, int what, Object obj) {
		registerListener(KEY_SYNC_STATE_LISTNER, h, what, obj);
	}

	public void removeSyncStateListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_SYNC_STATE_LISTNER, h, what, obj);
	}

	public void registerInvitationStateListener(Handler h, int what, Object obj) {
		registerListener(KEY_INVITATION_STATE_LISTNER, h, what, obj);
	}

	public void removeInvitationStateListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_INVITATION_STATE_LISTNER, h, what, obj);
	}

	public void registerVoiceActivationListener(Handler h, int what, Object obj) {
		registerListener(KEY_VOICEACTIVATION_LISTNER, h, what, obj);
	}

	public void removeVoiceActivationListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_VOICEACTIVATION_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for permission changed
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerPermissionUpdateListener(Handler h, int what, Object obj) {
		registerListener(KEY_PERMISSION_CHANGED_LISTNER, h, what, obj);
	}

	public void unRegisterPermissionUpdateListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_PERMISSION_CHANGED_LISTNER, h, what, obj);
	}

	public void registerVideoMixerListener(Handler h, int what, Object obj) {
		registerListener(KEY_MIXED_VIDEO_LISTNER, h, what, obj);
	}

	public void unRegisterVideoMixerListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_MIXED_VIDEO_LISTNER, h, what, obj);
	}

	public void registerLectureRequestListener(Handler h, int what, Object obj) {
		registerListener(KEY_LECTURE_REQUEST_LISTNER, h, what, obj);
	}

	public void unRegisterLectureRequestListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_LECTURE_REQUEST_LISTNER, h, what, obj);
	}

	public void registerChairManChangeListener(Handler h, int what, Object obj) {
		registerListener(KEY_CHAIRMAN_CHANGE_LISTNER, h, what, obj);
	}

	public void unregisterChairManChangeListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_CHAIRMAN_CHANGE_LISTNER, h, what, obj);
	}

	@Override
	public void clearCalledBack() {
		super.clearCalledBack();
		VideoRequest.getInstance().removeCallback(videoCallback);
		ConfRequest.getInstance().removeCallback(confCallback);
		GroupRequest.getInstance().removeCallback(groupCallback);
		VideoMixerRequest.getInstance().removeCallback(mrCallback);
	}

	@Override
	protected void notifyListenerWithPending(int key, int arg1, int arg2, Object obj) {
		if (mFlag) {
			super.notifyListenerWithPending(key, arg1, arg2, obj);
		} else {
			super.notifyListener(key, arg1, arg2, obj);
		}
	}

	class ConfRequestCB extends ConfRequestCallbackAdapter {

		private Handler mCallbackHandler;

		public ConfRequestCB(Handler mCallbackHandler) {
			this.mCallbackHandler = mCallbackHandler;
		}

		@Override
		public void OnEnterConfCallback(long nConfID, long nTime, String szConfData, int nJoinResult) {
			super.OnEnterConfCallback(nConfID, nTime, szConfData, nJoinResult);
			ConferenceGroup cache = (ConferenceGroup) GlobalHolder.getInstance().findGroupById(nConfID);
			if (cache != null) {
				ConferenceGroup.extraAttrFromXml(cache, szConfData);
			}

			JNIResponse jniConfCreateRes = new RequestConfCreateResponse(nConfID, nTime,
					JNIResponse.Result.fromInt(nJoinResult));
			Message.obtain(mCallbackHandler, JNI_REQUEST_CREATE_CONFERENCE, jniConfCreateRes).sendToTarget();

			JNIResponse jniRes = new RequestEnterConfResponse(nConfID, nTime, szConfData,
					JNIResponse.Result.fromInt(nJoinResult));
			Message.obtain(mCallbackHandler, JNI_REQUEST_ENTER_CONF, jniRes).sendToTarget();
		}

		@Override
		public void OnConfMemberEnter(long nConfID, long nUserID, long nTime, String szUserInfos) {
			super.OnConfMemberEnter(nConfID, nUserID, nTime, szUserInfos);
			BoUserInfoShort boUserInfoShort = null;
			try {
				boUserInfoShort = BoUserInfoShort.parserXml(szUserInfos);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			if (boUserInfoShort == null) {
				return;
			}

			User user = GlobalHolder.getInstance().putOrUpdateUser(boUserInfoShort);
			notifyListenerWithPending(KEY_ATTENDEE_ENTER_OR_EXIT_LISTNER, 1, 0, user);
		}

		@Override
		public void OnConfMemberExitCallback(long nConfID, long nTime, long nUserID) {
			super.OnConfMemberExitCallback(nConfID, nTime, nUserID);
			User u = GlobalHolder.getInstance().getUser(nUserID);
			// For quick logged in User.
			notifyListenerWithPending(KEY_ATTENDEE_ENTER_OR_EXIT_LISTNER, 0, 0, u);
		}

		@Override
		public void OnKickConfCallback(int nReason) {
			super.OnKickConfCallback(nReason);
			notifyListenerWithPending(KEY_KICKED_LISTNER, nReason, 0, null);
		}

		@Override
		public void OnGrantPermissionCallback(long userid, int type, int status) {
			super.OnGrantPermissionCallback(userid, type, status);
			JNIIndication jniInd = new PermissionUpdateIndication(userid, type, status);
			notifyListenerWithPending(KEY_PERMISSION_CHANGED_LISTNER, 0, 0, jniInd);
		}

		@Override
		public void OnNotifyChair(long userid, int type) {
			super.OnNotifyChair(userid, type);
			JNIIndication jniInd = new PermissionRequestIndication(userid, type, PermissionState.APPLYING.intValue());
			notifyListenerWithPending(KEY_LECTURE_REQUEST_LISTNER, 0, 0, jniInd);
		}

		@Override
		public void OnConfChairChanged(long nConfID, long nChairID) {
			super.OnConfChairChanged(nConfID, nChairID);
            V2Log.e("test" , "vvvvvvvvvvvvvvvvvv");
			JNIIndication jniInd = new RequestConfChairManChange(nConfID, nChairID);
			notifyListenerWithPending(KEY_CHAIRMAN_CHANGE_LISTNER, 0, 0, jniInd);
		}
	}

	class VideoRequestCB extends VideoRequestCallbackAdapter {

		private Handler mCallbackHandler;

		public VideoRequestCB(Handler mCallbackHandler) {
			this.mCallbackHandler = mCallbackHandler;
		}

		@Override
		public void OnRemoteUserVideoDevice(long uid, String szXmlData) {
			int inUid = (int) uid;
			List<UserDeviceConfig> ll = UserDeviceConfig.parseFromXml(uid, szXmlData);
			GlobalHolder.getInstance().updateUserDevice(uid, ll);
			notifyListenerWithPending(KEY_ATTENDEE_DEVICE_LISTNER, 0, 0, new Object[] { Integer.valueOf(inUid), ll });
		}

		@Override
		public void OnSetCapParamDone(String szDevID, int nSizeIndex, int nFrameRate, int nBitRate) {
			super.OnSetCapParamDone(szDevID, nSizeIndex, nFrameRate, nBitRate);
			JNIResponse jniRes = new RequestUpdateCameraParametersResponse(
					new CameraConfiguration(szDevID, 1, nFrameRate, nBitRate, null),
					RequestUpdateCameraParametersResponse.Result.SUCCESS);
			Message.obtain(mCallbackHandler, JNI_UPDATE_CAMERA_PAR, jniRes).sendToTarget();

		}

	}

	class GroupRequestCB extends GroupRequestCallbackAdapter {

		public GroupRequestCB(Handler mCallbackHandler) {
		}

		@Override
		public void OnModifyGroupInfo(int groupType, long nGroupID, String sXml) {
			super.OnModifyGroupInfo(groupType, nGroupID, sXml);
			V2Group group = new V2Group(nGroupID, groupType);
			if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
				String name = XmlAttributeExtractor.extractAttribute(sXml, "name");
				String announcement = XmlAttributeExtractor.extractAttribute(sXml, "announcement");
				String summary = XmlAttributeExtractor.extractAttribute(sXml, "summary");
				String authtype = XmlAttributeExtractor.extractAttribute(sXml, "authtype");
				group.setName(name);
				group.setAnnounce(announcement);
				group.setBrief(summary);
				if (authtype != null) {
					group.authType = Integer.parseInt(authtype);
				} else {
					V2Log.e("No found authtype attrbitue, use 0 as default");
				}

			} else if (groupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
				// 会议室
				group.xml = sXml;
			} else if (groupType == V2GlobalConstants.GROUP_TYPE_DISCUSSION
					|| groupType == V2GlobalConstants.GROUP_TYPE_CONTACT) {
				String name = XmlAttributeExtractor.extractAttribute(sXml, "name");
				if (!TextUtils.isEmpty(name)) {
					String newName = name.replace("&#x0D;", "").replace("&#x0A;", "").replace("&#x09;", "");
					group.setName(newName);
				} else {
					group.setName(name);
				}
			}

			if (group.type == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
				ConferenceGroup conferenceGroup = (ConferenceGroup) GlobalHolder.getInstance().findGroupById(group.id);

				if (conferenceGroup == null) {
					// if doesn't find matched group, mean this is new group
					return;
				}

				// 检测邀请
				String invite = XmlAttributeExtractor.extractAttribute(group.xml, "inviteuser");
				if (invite != null) {
					if ("0".equalsIgnoreCase(invite)) {
						group.canInvitation = false;
					} else if ("1".equalsIgnoreCase(invite)) {
						group.canInvitation = true;
					} else {
						V2Log.e("inviteuser value illegality");
						return;
					}

					conferenceGroup.setCanInvitation(group.canInvitation);
					notifyListenerWithPending(KEY_INVITATION_STATE_LISTNER, (group.canInvitation ? 1 : 0), 0, null);
				}

				// 检测同步
				String sync = XmlAttributeExtractor.extract(group.xml, " syncdesktop='", "'");
				if (sync != null) {
					if ("0".equalsIgnoreCase(sync)) {
						// 关闭了同步
						group.isSync = false;
					} else if ("1".equalsIgnoreCase(sync)) {
						// 开启了同步
						group.isSync = true;
					} else {
						V2Log.e("syncdesktop value illegality");
						return;
					}

					conferenceGroup.setSyn(group.isSync);
					notifyListenerWithPending(KEY_SYNC_STATE_LISTNER, (conferenceGroup.isSyn() ? 1 : 0), 0, null);
				}

				// 检测语音激励
				String voiceActivation = XmlAttributeExtractor.extract(group.xml, " voiceactivation='", "'");
				if (voiceActivation != null) {
					if ("0".equalsIgnoreCase(voiceActivation)) {
						// 关闭了语音激励
						group.isVoiceActivation = false;
					} else if ("1".equalsIgnoreCase(voiceActivation)) {
						// 开启了语音激励
						group.isVoiceActivation = true;
					} else {
						V2Log.e("voiceactivation value illegality");
						return;
					}

					conferenceGroup.setVoiceActivation(group.isVoiceActivation);
					notifyListenerWithPending(KEY_VOICEACTIVATION_LISTNER, (group.isVoiceActivation
							? V2GlobalConstants.CONF_VOICE_ACTIVATION_YES : V2GlobalConstants.CONF_VOICE_ACTIVATION_NO),
							0, null);
				}
			}
		}
	}

	class MixerRequestCB implements VideoMixerRequestCallback {

		public MixerRequestCB(Handler mCallbackHandler) {
		}

		@Override
		public void OnCreateVideoMixerCallback(String sMediaId, int layout, int width, int height) {
			if (sMediaId == null || sMediaId.isEmpty()) {
				V2Log.e(" OnCreateVideoMixerCallback -- > unlmatform parameter sMediaId is null ");
				return;
			}
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 1, 0,
					new MixVideo(sMediaId, MixVideoLayoutType.fromInt(layout), width, height));
		}

		@Override
		public void OnDestroyVideoMixerCallback(String sMediaId) {
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 2, 0, new MixVideo(sMediaId, MixVideoLayoutType.UNKOWN));
		}

		@Override
		public void OnAddVideoMixerCallback(String sMediaId, long nDstUserId, String sDstDevId, int pos) {
			UserDeviceConfig udc = new UserDeviceConfig(0, 0, nDstUserId, sDstDevId, null);
			MixVideo mix = new MixVideo(sMediaId);
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 3, 0, mix.createMixVideoDevice(pos, sMediaId, udc));
		}

		@Override
		public void OnDelVideoMixerCallback(String sMediaId, long nDstUserId, String sDstDevId) {
			UserDeviceConfig udc = new UserDeviceConfig(0, 0, nDstUserId, sDstDevId, null);
			MixVideo mix = new MixVideo(sMediaId);
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 4, 0, mix.createMixVideoDevice(-1, sMediaId, udc));
		}
	}
}
