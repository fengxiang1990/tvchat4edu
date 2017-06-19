package com.bizcom.vc.listener;

import com.bizcom.vc.activity.conversation.MessageBodyView;
import com.bizcom.vo.meesage.VMessage;

/**
 * 回调基类
 * 
 * @author
 * 
 */
public class CommonCallBack {

	private CommonCallBack() {
	};

	private static CommonCallBack callback = new CommonCallBack();

	public static CommonCallBack getInstance() {
		return callback;
	}

	private CommonUpdateConversationStateInterface conversationStateInterface;
	private CommonUpdateMessageBodyPopupWindowInterface messageBodyPopup;
	private CommonUpdateCrowdFileStateInterface crowdFileState;
	private CommonNotifyCrowdDetailNewMessage notifyCrowdDetailActivity;
	private CommonNotifyChatInterToReplace notifyChatInterToReplace;
	private CommonNotifyV2ImageUpdate notifyV2ImageUpdate;
	private CommonCrowdRequestNotifyJniService crowdRequestNotifyJniService;

	public void setCrowdRequestNotifyJniService(CommonCrowdRequestNotifyJniService crowdRequestNotifyJniService) {
		this.crowdRequestNotifyJniService = crowdRequestNotifyJniService;
	}

	public void setNotifyV2ImageUpdate(CommonNotifyV2ImageUpdate notifyV2ImageUpdate) {
		this.notifyV2ImageUpdate = notifyV2ImageUpdate;
	}

	public void setNotifyChatInterToReplace(CommonNotifyChatInterToReplace notifyChatInterToReplace) {
		this.notifyChatInterToReplace = notifyChatInterToReplace;
	}

	public void setNotifyCrowdDetailActivity(CommonNotifyCrowdDetailNewMessage notifyCrowdDetailActivity) {
		this.notifyCrowdDetailActivity = notifyCrowdDetailActivity;
	}

	public void setCrowdFileState(CommonUpdateCrowdFileStateInterface crowdFileState) {
		this.crowdFileState = crowdFileState;
	}

	public void setMessageBodyPopup(CommonUpdateMessageBodyPopupWindowInterface messageBodyPopup) {
		this.messageBodyPopup = messageBodyPopup;
	}

	public void setConversationStateInterface(CommonUpdateConversationStateInterface conversationStateInterface) {
		this.conversationStateInterface = conversationStateInterface;
	}

	public void executeUpdateConversationState() {
		if (conversationStateInterface != null)
			conversationStateInterface.updateConversationState();
	}

	public void executeUpdatePopupWindowState(MessageBodyView view) {
		if (messageBodyPopup != null)
			messageBodyPopup.updateMessageBodyPopupWindow(view);
	}

	public void executeUpdateCrowdFileState(String fileID, VMessage vm, CrowdFileExeType type) {
		if (crowdFileState != null)
			crowdFileState.updateCrowdFileState(fileID, vm, type);
	}

	public void executeNotifyCrowdDetailActivity() {
		if (notifyCrowdDetailActivity != null)
			notifyCrowdDetailActivity.notifyCrowdDetailNewMessage();
	}

	public void executeNotifyChatInterToReplace(VMessage vm) {
		if (notifyChatInterToReplace != null)
			notifyChatInterToReplace.notifyChatInterToReplace(vm);
	}

	public void executeNotifyV2ImageUpdate() {
		if (notifyV2ImageUpdate != null)
			notifyV2ImageUpdate.notifyV2ImageUpdate();
	}

	public void executeCrowdRequestNotifyJniService(long msgID) {
		if (crowdRequestNotifyJniService != null)
			crowdRequestNotifyJniService.crowdRequestNotifyJniService(msgID);
	}

	/**
	 * 该回调用于，登录时ConversationTabFragment构建完毕后，会回调JNIService，让其存储的延迟广播立刻发送。
	 * 延迟广播为服务器发送的所有组织信息和用户信息
	 * 
	 * @author Administrator
	 * 
	 */
	public static interface CommonUpdateConversationStateInterface {

		public void updateConversationState();
	}

	/**
	 * 该回调用于，当聊天界面来新的消息，会让MessageBodyView里正在显示的PopupWindow消失
	 * 
	 * @author Administrator
	 * 
	 */
	public static interface CommonUpdateMessageBodyPopupWindowInterface {

		public void updateMessageBodyPopupWindow(MessageBodyView view);
	}

	/**
	 * 该回调用于，当群文件界面有文件操作时，通知聊天界面做相应更新
	 * 
	 * @author Administrator
	 *
	 */
	public static interface CommonUpdateCrowdFileStateInterface {

		public void updateCrowdFileState(String fileID, VMessage vm, CrowdFileExeType type);
	}

	/**
	 * 该回调用于，当群文件界面有文件操作时，通知聊天界面做相应更新
	 * 
	 * @author Administrator
	 *
	 */
	public static interface CommonNotifyCrowdDetailNewMessage {

		public void notifyCrowdDetailNewMessage();
	}

	/**
	 * 该回调用于，离线消息中如果有二进制数据(图片和音频)，则在收到onRecvBinary回调后，会回调聊天界面将等待的图片替换
	 * 
	 * @author Administrator
	 *
	 */
	public static interface CommonNotifyChatInterToReplace {

		public void notifyChatInterToReplace(VMessage vm);
	}

	/**
	 * 该回调用于，当发生can't use recycled bitmap 时回调发生的地方从新获取
	 * 
	 * @author Administrator
	 *
	 */
	public static interface CommonNotifyV2ImageUpdate {

		public void notifyV2ImageUpdate();
	}

	/**
	 * V2CrowdGroupRequest 回调 JNIservie ， 用于区分自己邀请别人入群和别人申请加入你的群
	 * 
	 * @author Administrator
	 *
	 */
	public static interface CommonCrowdRequestNotifyJniService {

		public void crowdRequestNotifyJniService(long msgID);
	}

	public enum CrowdFileExeType {
		DELETE_FILE, UPDATE_FILE;
	}
}
