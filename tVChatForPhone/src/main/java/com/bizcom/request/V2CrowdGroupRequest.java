package com.bizcom.request;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.V2.jni.FileRequest;
import com.V2.jni.GroupRequest;
import com.V2.jni.callbacAdapter.FileRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.GroupRequestCallbackAdapter;
import com.V2.jni.ind.BoUserInfoShort;
import com.V2.jni.ind.FileJNIObject;
import com.V2.jni.ind.GroupAddUserJNIObject;
import com.V2.jni.ind.V2Group;
import com.bizcom.db.provider.VerificationProvider;
import com.bizcom.request.jni.CreateGroupResponse;
import com.bizcom.request.jni.FileTransStatusIndication;
import com.bizcom.request.jni.FileTransStatusIndication.FileTransProgressStatusIndication;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.RequestFetchGroupFilesResponse;
import com.bizcom.request.util.FileOperationEnum;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.V2Log;
import com.bizcom.util.XmlAttributeExtractor;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vo.Crowd;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.CrowdGroup.AuthType;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.VCrowdFile;
import com.bizcom.vo.enums.GroupQualicationState;
import com.bizcom.vo.meesage.VMessageQualification.QualificationState;
import com.bizcom.vo.meesage.VMessageQualification.ReadState;
import com.bizcom.vo.meesage.VMessageQualification.Type;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//组别统一名称
//1:部门, organizationGroup
//2:好友组, contactGroup
//3:群, crowdGroup
//4:会议, conferenceGroup
//5:讨论组,discussionGroup
/**
 * Crowd group service, used to create crowd and remove crowd
 * 
 * @author 28851274
 * 
 */
public class V2CrowdGroupRequest extends V2AbstractHandler {

	private static final int ACCEPT_JOIN_CROWD = 0x0002;
	private static final int UPDATE_CROWD = 0x0004;
	private static final int QUIT_CROWD = 0x0005;
	private static final int ACCEPT_APPLICATION_CROWD = 0x0006;
	private static final int REFUSE_APPLICATION_CROWD = 0x0007;
	private static final int FETCH_FILES_CROWD = 0x0008;
	private static final int REMOVE_FILES_CROWD = 0x0009;

	private static final int QUIT_DISCUSSION_BOARD = 0x000A;
	private static final int CREATE_DISCUSSION_BOARD = 0x000B;
	private static final int UPDATE_DISCUSSION_BOARD = 0x000C;

	private static final int KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER = 2;
	private static final int KEY_FILE_REMOVED_NOTIFICATION_LISTNER = 3;
	private static final int KEY_FILE_NEW_NOTIFICATION_LISTNER = 4;

	private GroupRequestCB grCB;
	private FileRequestCB frCB;

	public V2CrowdGroupRequest() {
		grCB = new GroupRequestCB(this);
		GroupRequest.getInstance().addCallback(grCB);
		frCB = new FileRequestCB(this);
		FileRequest.getInstance().addCallback(frCB);
	}

	/**
	 * Create crowd function, it's asynchronization request. response will be
	 * send by caller.
	 * 
	 * @param crowd
	 * @param invationUserList
	 *            be invite user list
	 * @param caller
	 *            if input is null, ignore response Message. Response Message
	 *            object is {@link CreateGroupResponse}
	 */
	public void createCrowdGroup(CrowdGroup crowd, List<User> invationUserList, HandlerWrap caller) {
		String sXml = XmlAttributeExtractor.buildAttendeeUsersXml(invationUserList);

		this.initTimeoutMessage(CREATE_DISCUSSION_BOARD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupCreate(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.toXml(), sXml);

	}

	/**
	 * 
	 * @param discussion
	 * @param invationUserList
	 * @param caller
	 */
	public void createDiscussionBoard(DiscussionGroup discussion, List<User> invationUserList, HandlerWrap caller) {
        if (!checkParamNull(caller, discussion)) {
            return;
        }
		String sXml = XmlAttributeExtractor.buildAttendeeUsersXml(invationUserList);
		this.initTimeoutMessage(CREATE_DISCUSSION_BOARD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupCreate(V2GlobalConstants.GROUP_TYPE_DISCUSSION, discussion.toXml(), sXml);
	}

	/**
	 * Accept invitation
	 * 
	 * @param crowd
	 * @param caller
	 *            if input is null, ignore response Message. Response Message
	 *            object is {@link com.bizcom.request.jni.JNIResponse}
	 */
	public void acceptApplication(CrowdGroup crowd, User applicant, HandlerWrap caller) {
		if (!checkParamNull(caller, crowd, applicant)) {
			return;
		}

		initTimeoutMessage(ACCEPT_APPLICATION_CROWD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupAcceptApplyJoin(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getGroupID(),
				applicant.getmUserId());
	}

	/**
	 * Decline applicant who want to join crowd
	 * 
	 * @param crowd
	 * @param applicant
	 * @param reason
	 * @param caller
	 */
	public void refuseApplication(CrowdGroup crowd, User applicant, String reason, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd, applicant })) {
			return;
		}

		initTimeoutMessage(REFUSE_APPLICATION_CROWD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupRefuseApplyJoin(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getGroupID(),
				applicant.getmUserId(), reason);
	}

	/**
	 * Accept invitation
	 * 
	 * @param crowd
	 * @param caller
	 *            if input is null, ignore response Message. Response Message
	 *            object is {@link com.bizcom.request.jni.JNIResponse}
	 */
	public void acceptInvitation(Crowd crowd, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd })) {
			return;
		}

		initTimeoutMessage(ACCEPT_JOIN_CROWD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupAcceptInvite(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getId(),
				crowd.getCreator().getmUserId());
	}

	/**
	 * Decline join crowd invitation
	 * 
	 * @param crowd
	 * @param reason
	 * @param caller
	 */
	public void refuseInvitation(Crowd crowd, String reason, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd })) {
			return;
		}

		// FIXME concurrency problem, if user use one crowdgroupservice instance
		// to
		// accept mulit-invitation, then maybe call back will notify incorrect

		GroupRequest.getInstance().GroupRefuseInvite(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getId(),
				crowd.getCreator().getmUserId(), reason == null ? "" : reason);
		callerSendMessage(caller, new JNIResponse(JNIResponse.Result.SUCCESS));
	}

	/**
	 * Apply join crowd
	 * 
	 * @param crowd
	 * @param additional
	 * @param caller
	 */
	public void applyCrowd(Crowd crowd, String additional, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd, additional })) {
			return;
		}

		GroupRequest.getInstance().GroupApplyJoin(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getId(),
				additional == null ? "" : additional);
		callerSendMessage(caller, new JNIResponse(JNIResponse.Result.SUCCESS));
	}

	/**
	 * Update crowd data, like brief, announcement or member joined rules
	 * 
	 * @param crowd
	 * @param caller
	 */
	public void updateCrowd(CrowdGroup crowd, HandlerWrap caller) {
		if (!checkParamNull(caller, crowd)) {
			return;
		}

		initTimeoutMessage(UPDATE_CROWD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupModify(crowd.getGroupType(), crowd.getGroupID(), crowd.toXml());
	}

	/**
	 * Update crowd data, like brief, announcement or member joined rules
	 * 
	 * @param discussion
	 * @param caller
	 */
	public void updateDiscussion(DiscussionGroup discussion, HandlerWrap caller) {
		if (!checkParamNull(caller, discussion)) {
			return;
		}

		initTimeoutMessage(UPDATE_DISCUSSION_BOARD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupModify(discussion.getGroupType(), discussion.getGroupID(),
				discussion.toXml());
	}

	/**
	 * Quit crowd. <br>
	 * If current user is administrator, then will dismiss crowd.<br>
	 * If current user is member, just quit this crowd.
	 * 
	 * @param crowd
	 * @param caller
	 */
	public void quitCrowd(CrowdGroup crowd, HandlerWrap caller) {
		if (!checkParamNull(caller, crowd)) {
			return;
		}

		initTimeoutMessage(QUIT_CROWD, DEFAULT_TIME_OUT_SECS, caller);
		if (crowd.getOwnerUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			GroupRequest.getInstance().GroupDestroy(crowd.getGroupType(), crowd.getGroupID());
		} else {
			GroupRequest.getInstance().GroupLeave(crowd.getGroupType(), crowd.getGroupID());
		}
	}

	/**
	 * Quit from discussion board. <br>
	 * 
	 * @param discussion
	 * @param caller
	 */
	public void quitDiscussionBoard(DiscussionGroup discussion, HandlerWrap caller) {
		if (!checkParamNull(caller, discussion)) {
			return;
		}

		initTimeoutMessage(QUIT_DISCUSSION_BOARD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupLeave(discussion.getGroupType(), discussion.getGroupID());
	}

	/**
	 * Invite new member to join crowd or discussion board.<br>
	 * Notice: call this API after group is created.
	 * 
	 * @param crowd
	 * @param newMembers
	 * @param caller
	 */
	public void inviteMember(Group crowd, List<User> newMembers, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd, newMembers })) {
			return;
		}
		if (newMembers.size() <= 0) {
			if (caller != null) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				callerSendMessage(caller, jniRes);
			}
			return;
		}

		if (crowd == null) {
			V2Log.e("CrowdGroupService inviteMember --> INVITE MEMBER FAILED ... Because crowd Object is null!");
			return;
		}

		String sXml = XmlAttributeExtractor.buildAttendeeUsersXml(newMembers);
		GroupRequest.getInstance().GroupInviteUsers(crowd.getGroupType(), crowd.toXml(), sXml, "");
		if (caller != null) {
			JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
			callerSendMessage(caller, jniRes);
		}
	}

	/**
	 * Remove member from crowd
	 * 
	 * @param crowd
	 * @param member
	 * @param caller
	 */
	public void removeMember(Group crowd, User member, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd, member })) {
			return;
		}
		GroupRequest.getInstance().GroupKickUser(crowd.getGroupType(), crowd.getGroupID(),
				member.getmUserId());
	}

	@Override
	public void clearCalledBack() {
		GroupRequest.getInstance().removeCallback(grCB);
		FileRequest.getInstance().removeCallback(frCB);
	}

	/**
	 * fetch files from server
	 * 
	 * @param crowd
	 * @param caller
	 *            return List<VFile>
	 */
	public void fetchGroupFiles(CrowdGroup crowd, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd })) {
			return;
		}

		this.initTimeoutMessage(FETCH_FILES_CROWD, DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().FileTransEnumGroupFiles(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getGroupID());

	}

	/**
	 * Remove files from crowd.
	 * 
	 * @param crowd
	 * @param files
	 * @param caller
	 */
	public void removeGroupFiles(CrowdGroup crowd, List<VCrowdFile> files, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { crowd, files })) {
			return;
		}
		if (files.size() <= 0) {
			JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
			super.callerSendMessage(caller, jniRes);
			return;
		}

		this.initTimeoutMessage(REMOVE_FILES_CROWD, DEFAULT_TIME_OUT_SECS, caller);
		for (VCrowdFile f : files) {
			GroupRequest.getInstance().FileTransDeleteGroupFile(crowd.getGroupType(), crowd.getGroupID(),
					f.getId());
		}

	}

	/**
	 * 
	 * @param vf
	 * @param opt
	 *            {@link FileOperationEnum}
	 * @param caller
	 */
	public void handleCrowdFile(VCrowdFile vf, FileOperationEnum opt, HandlerWrap caller) {
		if (!checkParamNull(caller, new Object[] { vf })) {
			return;
		}

		switch (opt) {
		case OPERATION_PAUSE_SENDING:
			FileRequest.getInstance().FileTransPauseUploadFile(vf.getId());
			break;
		case OPERATION_RESUME_SEND:
			FileRequest.getInstance().FileTransResumeUploadFile(vf.getId());
			break;
		case OPERATION_PAUSE_DOWNLOADING:
			FileRequest.getInstance().FileTransPauseDownloadFile(vf.getId());
			break;
		case OPERATION_RESUME_DOWNLOAD:
			FileRequest.getInstance().FileTransResumeDownloadFile(vf.getId());
			break;
		case OPERATION_CANCEL_SENDING:
			FileRequest.getInstance().FileTransCloseSendFile(vf.getId());
			break;
		case OPERATION_CANCEL_DOWNLOADING:
			FileRequest.getInstance().FileTransCloseRecvFile(vf.getId());
			break;
		case OPERATION_START_DOWNLOAD:
			FileRequest.getInstance().FileTransDownloadFile(vf.getUrl(), vf.getId(), vf.getPath(),
					V2GlobalConstants.FILE_ENCRYPT_TYPE);
			break;
		case OPERATION_START_SEND: {
			GroupRequest.getInstance().FileTransUploadGroupFile(vf.getCrowd().getGroupType(),
					vf.getCrowd().getGroupID(), vf.toXml());
		}
			break;
		default:
			break;
		}
	}

	/**
	 * Register listener for file transport status
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerFileTransStatusListener(Handler h, int what, Object obj) {
		registerListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, h, what, obj);
	}

	public void unRegisterFileTransStatusListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for group file is removed.<br>
	 * Notice: If current user send remove file command by
	 * {@link #removeGroupFiles(CrowdGroup, List, HandlerWrap)}, will not
	 * notification
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerFileRemovedNotification(Handler h, int what, Object obj) {
		registerListener(KEY_FILE_REMOVED_NOTIFICATION_LISTNER, h, what, obj);
	}

	public void unRegisterFileRemovedNotification(Handler h, int what, Object obj) {
		unRegisterListener(KEY_FILE_REMOVED_NOTIFICATION_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for group new file notification.<br>
	 * 
	 * @param h
	 * @param what
	 * @param obj
	 */
	public void registerNewFileNotification(Handler h, int what, Object obj) {
		registerListener(KEY_FILE_NEW_NOTIFICATION_LISTNER, h, what, obj);
	}

	public void unRegisterNewFileNotification(Handler h, int what, Object obj) {
		unRegisterListener(KEY_FILE_NEW_NOTIFICATION_LISTNER, h, what, obj);
	}

	class GroupRequestCB extends GroupRequestCallbackAdapter {
		private Handler mCallbackHandler;

		public GroupRequestCB(Handler mCallbackHandler) {
			this.mCallbackHandler = mCallbackHandler;
		}

		@Override
		public void OnModifyGroupInfo(int groupType, long nGroupID, String sXml) {
			if (TextUtils.isEmpty(sXml)) {
				return;
			}

			String target = XmlAttributeExtractor.extract(sXml, " id='", "'");
			if (TextUtils.isEmpty(target)) {
				V2Log.e(V2Log.JNISERVICE_CALLBACK,
						"V2CrowdGroupRequest OnModifyGroupInfo -> 解析xml失败，没有获取到id, xml : " + sXml);
				return;
			}

			if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				Message.obtain(mCallbackHandler, UPDATE_CROWD, jniRes).sendToTarget();
			} else if (groupType == 	V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				Message.obtain(mCallbackHandler, UPDATE_DISCUSSION_BOARD, jniRes).sendToTarget();

			}
		}

		@Override
		public void OnDelGroupCallback(int groupType, long nGroupID, boolean bMovetoRoot) {
			if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				Message.obtain(mCallbackHandler, QUIT_CROWD, jniRes).sendToTarget();
			} else if (groupType == 	V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				Message.obtain(mCallbackHandler, QUIT_DISCUSSION_BOARD, jniRes).sendToTarget();
			}
		}

		/**
		 * Used to as callback of accept join crowd group <br />
		 * ===OnAcceptInviteJoinGroup never called===
		 * 
		 */

		@Override
		public void onAddGroupInfo(int groupType, long nParentID, long nGroupID, String sXml) {
			if (TextUtils.isEmpty(sXml)) {
				return;
			}

			String target = XmlAttributeExtractor.extract(sXml, " creatoruserid='", "'");
			if (TextUtils.isEmpty(target)) {
				V2Log.e(V2Log.SERVICE_CALLBACK,
						"onAddGroupInfo -> 解析xml失败，没有获取到creatoruserid, xml : " + sXml);
				return;
			}

			long createUesrID = Long.valueOf(target);
			if (groupType == V2Group.TYPE_CROWD) {
				if (GlobalHolder.getInstance().getCurrentUserId() == createUesrID) {
					JNIResponse jniRes = new CreateGroupResponse(nGroupID, CreateGroupResponse.Result.SUCCESS);
					Message.obtain(mCallbackHandler, CREATE_DISCUSSION_BOARD, jniRes).sendToTarget();
				} else {
					JNIResponse jniRes = new JNIResponse(CreateGroupResponse.Result.SUCCESS);
					Message.obtain(mCallbackHandler, ACCEPT_JOIN_CROWD, jniRes).sendToTarget();
				}
			} else if (groupType == V2Group.TYPE_DISCUSSION_BOARD) {
				if (GlobalHolder.getInstance().getCurrentUserId() == createUesrID) {
                    // 将讨论组添加到全局里面
                    String groupName = XmlAttributeExtractor.extract(sXml, " name='", "'");
                    User creatorUser = GlobalHolder.getInstance().getUser(createUesrID);
                    DiscussionGroup g = new DiscussionGroup(nGroupID, groupName, creatorUser, new Date(GlobalConfig.getGlobalServerTime()));
                    GlobalHolder.getInstance().addGroupToList(V2GlobalConstants.GROUP_TYPE_DISCUSSION, g);
					JNIResponse jniRes = new CreateGroupResponse(nGroupID , JNIResponse.Result.SUCCESS);
					Message.obtain(mCallbackHandler, CREATE_DISCUSSION_BOARD, jniRes).sendToTarget();
				}
			}
		}

		@Override
		public void OnAddGroupUserInfoCallback(int groupType, long nGroupID, String sXml) {
			BoUserInfoShort boUserInfoShort = null;
			try {
				boUserInfoShort = BoUserInfoShort.parserXml(sXml);
			} catch (Exception e) {
				e.printStackTrace();
				V2Log.e("OnAddGroupUserInfo -> parse xml failed ...get null user : " + sXml);
				return;
			}
			if (boUserInfoShort == null) {
				V2Log.e("OnAddGroupUserInfo -> parse xml failed ...get null user : " + sXml);
				return;
			}

			if (groupType == V2Group.TYPE_CROWD
					&& boUserInfoShort.mId != GlobalHolder.getInstance().getCurrentUserId()) {
				CrowdGroup group = (CrowdGroup) GlobalHolder.getInstance().getGroupById(groupType, nGroupID);
				if (group == null) {
					V2Log.serviceCall("OnAddGroupUserInfoCallback",
							"update crowd qualication message failed..group is null");
				} else {
					if (group.getOwnerUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()
							&& group.getAuthType() == AuthType.QULIFICATION) {
						GroupQualicationState state = new GroupQualicationState(Type.CROWD_APPLICATION,
								QualificationState.ACCEPTED, null, ReadState.READ, false);
						state.isUpdateTime = false;
						long msgID = VerificationProvider.updateCrowdQualicationMessageState(nGroupID,
								boUserInfoShort.mId, state);
						CommonCallBack.getInstance().executeCrowdRequestNotifyJniService(msgID);
					}
				}

				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				jniRes.resObj = new GroupAddUserJNIObject(groupType, nGroupID, boUserInfoShort.mId, "");
				Message.obtain(mCallbackHandler, ACCEPT_APPLICATION_CROWD, jniRes).sendToTarget();
			}
		}

		/**
		 * Used to as callback of leave crowd group
		 */
		public void OnDelGroupUserCallback(int groupType, long nGroupID, long nUserID) {
			if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				Message.obtain(mCallbackHandler, QUIT_CROWD, jniRes).sendToTarget();
			} else if (groupType == 	V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
				JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
				Message.obtain(mCallbackHandler, QUIT_DISCUSSION_BOARD, jniRes).sendToTarget();
			}
		}

		@Override
		public void OnGetGroupFileInfo(int groupType, long nGroupId, String sXml) {
			if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
				List<FileJNIObject> list = XmlAttributeExtractor.parseFiles(sXml);
				V2Group group = new V2Group(nGroupId, groupType);
				RequestFetchGroupFilesResponse jniRes = new RequestFetchGroupFilesResponse(JNIResponse.Result.SUCCESS);
				jniRes.setList(convertList(group, list));
				Message.obtain(mCallbackHandler, FETCH_FILES_CROWD, jniRes).sendToTarget();
			}
		}

		@Override
		public void OnDelGroupFile(int type, long nGroupId, String fileId) {
			if (type == V2GlobalConstants.GROUP_TYPE_CROWD) {
				List<FileJNIObject> list = new ArrayList<FileJNIObject>();
				list.add(new FileJNIObject(null, fileId, null, 0, 0, ""));
				V2Group group = new V2Group(nGroupId, type);
				// Use fetch group file object as result
				RequestFetchGroupFilesResponse jniRes = new RequestFetchGroupFilesResponse(JNIResponse.Result.SUCCESS);
				jniRes.setList(convertList(group, list));
				notifyListener(KEY_FILE_REMOVED_NOTIFICATION_LISTNER, 0, 0, jniRes);
			}
		}

		@Override
		public void OnJoinGroupError(int eGroupType, long nGroupID, int nErrorNo) {
			JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SERVER_REJECT);
			Message.obtain(mCallbackHandler, ACCEPT_JOIN_CROWD, jniRes).sendToTarget();
		}

		private List<VCrowdFile> convertList(V2Group group, List<FileJNIObject> list) {
			List<VCrowdFile> vfList = null;
			vfList = new ArrayList<VCrowdFile>();
			if (list == null) {
				return vfList;
			}
			for (FileJNIObject f : list) {
				VCrowdFile vcf = new VCrowdFile();
				vcf.setCrowd(
						(CrowdGroup) GlobalHolder.getInstance().getGroupById(V2GlobalConstants.GROUP_TYPE_CROWD, group.id));

				vcf.setId(f.fileId);
				vcf.setName(f.fileName);
				vcf.setSize(f.fileSize);
				vcf.setUrl(f.url);
				// If event is removed file, then user is null
				if (f.user != null) {
					vcf.setUploader(GlobalHolder.getInstance().getUser(f.user.mId));
				}
				vcf.setPath(GlobalConfig.getGlobalFilePath() + "/" + f.fileName);
				vfList.add(vcf);
			}
			return vfList;
		}

		@Override
		public void OnAddGroupFile(int eGroupType, long nGroupId, String sXml) {
			if (eGroupType == V2GlobalConstants.GROUP_TYPE_CONFERENCE) {
				V2Log.e("CrowdGroupService OnAddGroupFile--> sendFriendToTv a new group file failed , Group Type is Conference!");
				return;
			}
			List<FileJNIObject> list = XmlAttributeExtractor.parseFiles(sXml);
			V2Group group = new V2Group(nGroupId, eGroupType);
			if (list == null) {
				V2Log.e("CrowdGroupService OnAddGroupFile--> sendFriendToTv a new group file failed , FileJNIObject List is null");
				return;
			}

			// Use fetch group file object as result
			RequestFetchGroupFilesResponse jniRes = new RequestFetchGroupFilesResponse(JNIResponse.Result.SUCCESS);
			jniRes.setList(convertList(group, list));
			jniRes.setGroupID(group.id);
			notifyListener(KEY_FILE_NEW_NOTIFICATION_LISTNER, 0, 0, jniRes);
		}
	}

	class FileRequestCB extends FileRequestCallbackAdapter {

		public FileRequestCB(Handler mCallbackHandler) {
		}

		@Override
		public void OnFileTransProgress(String szFileID, long nBytesTransed, int nTransType) {
			notifyListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, 0, 0, new FileTransProgressStatusIndication(
					nTransType, szFileID, nBytesTransed, FileTransStatusIndication.IND_TYPE_PROGRESS_TRANSING));
		}

		@Override
		public void OnFileTransEnd(String szFileID, String szFileName, long nFileSize, int nTransType) {
			notifyListener(KEY_FILE_TRANS_STATUS_NOTIFICATION_LISTNER, 0, 0, new FileTransProgressStatusIndication(
					nTransType, szFileID, nFileSize, FileTransStatusIndication.IND_TYPE_PROGRESS_END));

		}

		// @Override
		// public void OnFileDeleted(FileJNIObject file) {
		// if (file instanceof GroupFileJNIObject) {
		// GroupFileJNIObject gfile = (GroupFileJNIObject) file;
		//
		// RequestFetchGroupFilesResponse jniRes = new
		// RequestFetchGroupFilesResponse(
		// JNIResponse.Result.SUCCESS);
		// List<VCrowdFile> list = new ArrayList<VCrowdFile>(1);
		//
		// VCrowdFile vcf = new VCrowdFile();
		// vcf.setCrowd((CrowdGroup) GlobalHolder.getInstance()
		// .getGroupById(V2GlobalConstants.GROUP_TYPE_CROWD,
		// gfile.group.id));
		// vcf.setId(gfile.fileId);
		// list.sendFriendToTv(vcf);
		//
		// jniRes.setList(list);
		//
		// notifyListener(KEY_FILE_REMOVED_NOTIFICATION_LISTNER, 0, 0,
		// jniRes);
		// }
		// }

	}

}
