package com.config;

public class PublicIntent {

	public static String DEFAULT_CATEGORY = "";
	
	//======================================================================================
	//                            for P2P conversation
	//======================================================================================
	/**
	 * Used to start conversation UI
	 */
	public static final String START_CONVERSACTION_ACTIVITY = "com.v2tech.start_conversation_activity";

	public static final String START_P2P_CONVERSACTION_ACTIVITY = "com.v2tech.start_p2p_conversation_activity";

	public static final String START_VIDEO_IMAGE_GALLERY = "com.v2tech.image_gallery";
	
	public static final String START_LOGION_ACTIVITY = "com.bizcom.vc.hg.ui.HomeActivity";

	/**
	 * Start conference create activity<br>
	 * key uid: pre-selected user id
	 * key gid: pre-selected group id
	 */
	public static final String START_CONFERENCE_CREATE_ACTIVITY = "com.v2tech.start_conference_create_activity";

	public static final String START_ABOUT_ACTIVITY = "com.v2tech.start_about_activity";

	public static final String START_SETTING_ACTIVITY = "com.v2tech.start_setting_activity";
	
	//======================================================================================
	//                            for conference
	//======================================================================================

	public static final String START_GROUP_CREATE_ACTIVITY = "com.v2tech.start_group_create_activity";
	
	public static final String NOTIFY_CONFERENCE_ACTIVITY = "com.v2tech.notify_conference_activity";
	
	//======================================================================================
	//                            for crowd
	//======================================================================================
	
	public static final String START_CROWD_FILES_ACTIVITY = "com.v2tech.start_crowd_files_activity";

	public static final String SHOW_CROWD_CONTENT_ACTIVITY = "com.v2tech.crowd_content_activity";

	public static final String SHOW_CROWD_DETAIL_ACTIVITY = "com.v2tech.crowd_detail_activity";


	//======================================================================================
	//                            for discussion board
	//======================================================================================

	/**
	 * for DiscussionBoardCreateActivity <br>
	 * Intent parameters:<br>
	 * mode : true means in invitation mode, otherwise in create mode
	 */
	public static final String START_DISCUSSION_BOARD_CREATE_ACTIVITY = "com.v2tech.discussion_board_create_activity";
	
	/**
	 * for DiscussionBoardDetailActivity<br>
	 * Intent key:<br>
	 *      cid  : discussion board id<br>
	 */
	public static final String SHOW_DISCUSSION_BOARD_DETAIL_ACTIVITY = "com.v2tech.discussion_board_detail_activity";
	
	/**
	 * for DiscussionBoardTopicUpdateActivity<br>
	 * Intent key:<br>
	 *      cid  : discussion board id<br>
	 */
	public static final String SHOW_DISCUSSION_BOARD_TOPIC_ACTIVITY = "com.v2tech.discussion_board_topic_activity";
	

	/**
	 * key : crowd : object of crowd
	 * key : authdisable : disable crowd authentication even through crowd need authentication
	 */
	public static final String SHOW_CROWD_APPLICATION_ACTIVITY = "com.v2tech.crowd_application_activity";
	
	/**
	 * Start searching activity
	 * key : type 0 crowd   1: member
	 */
	public static final String START_SEARCH_ACTIVITY = "com.v2tech.start_search_activity";
	
	
	
	// ===========================Broadcast==============================
	// 
	//
	/**
	 * extras key: obj value:
	 * {@link com.bizcom.bo.ConversationNotificationObject}
	 */
	public static final String REQUEST_UPDATE_CONVERSATION = "com.v2tech.request_update_conversation";
	
	public static final String CHAT_SYNC_MESSAGE_INTERFACE = "com.v2tech.chat_sync_message_interface";

	public static final String FINISH_APPLICATION = "com.v2tech.finish_application";

	public static final String PREPARE_FINISH_APPLICATION = "com.v2tech.prepare_finish_application";
	
	/**
	 * broadcast for new crowd notification. if current logged in user created, this broadcast will be sent<br>
	 * key : crowd : crowd id 
	 */
	public static final String BROADCAST_NEW_CROWD_NOTIFICATION = "com.v2tech.jni.broadcast.new_crowd_notification";
	

	
	/**
	 * Broadcast for user update the comment name
	 * key: no
	 */
	public static final String BROADCAST_USER_COMMENT_NAME_NOTIFICATION = "com.v2tech.broadcast.user_comment_name_notification";
	
	/** Broadcast for new conference. This is only for conference is created by self
	 * extra key: newGid : group id
	 * we can get conference object from GlobalHolder
	 * 
	 */
	public static final String BROADCAST_NEW_CONFERENCE_NOTIFICATION = "com.v2tech.jni.broadcast.new_conference_notification";
	
	
	/**
	 * 
	 */
	public static final String BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP = "com.v2tech.broadcast.update_contacts_group";
	
	
	/**
	 * Broadcast for all type group was deleted 
	 * key: destGroupId
	 */
	public static final String BROADCAST_GROUP_DELETED_NOTIFICATION = "com.v2tech.broadcast.group_deleted_notification";
	
	/**
	 * Broadcast for contact group updated 
	 * key: userId 
	 * key: srcGroupId 
	 * key: destGroupId
	 */
	public static final String BROADCAST_CONTACT_GROUP_UPDATED_NOTIFICATION = "com.v2tech.broadcast.contact_group_notification";
	
	/**
	 *  Broadcast for user quit discussion board
	 * key: gid 
	 */
	public static final String BROADCAST_DISCUSSION_DELETED_NOTIFICATION = "com.v2tech.broadcast.discussion_deleted_notification";
	
	
	/**
	 * Broadcast for user joined conference, to inform that quit P2P conversation
	 * key: confid conference if 
	 */
	public static final String BROADCAST_JOINED_CONFERENCE_NOTIFICATION = "com.v2tech.broadcast.joined_conference_notification";
	
	/**
	 * Broadcast for current user is in waitting for other person agree to become friend
	 */
	public static final String BROADCAST_ADD_OTHER_FRIEND_WAITING_NOTIFICATION = "com.v2tech.broadcast.add_other_friend_waiting_notification";
	
	/**
	 * Broadcast for CrowdFileActivity notify ConversationP2PTEXTActivity
	 */
	public static final String BROADCAST_CROWD_FILE_ACTIVITY_SEND_NOTIFICATION = "com.v2tech.broadcast.add_other_friend_waiting_notification";
	
	/**
	 * Broadcast for MessageAuthenticationActivity notify ConversationsTabFragment
	 */
	public static final String BROADCAST_AUTHENTIC_TO_CONVERSATIONS_TAB_FRAGMENT_NOTIFICATION = "com.v2tech.broadcast.authentic_to_conversations_tab_fragment_notification";
	
	/**
	 * Broadcast for RegistActivity notify LoginActivity
	 */
	public static final String BROADCAST_REGIST_NOTIFY_TO_LOGIN = "com.v2tech.broadcast.regist_notify_to_login";
	
	/**
	 * Broadcast for LoginActivity notify RegistActivity finish 
	 */
	public static final String BROADCAST_LOGIN_NOTIFY_TO_FINISH_REGIST = "com.v2tech.broadcast.login_notify_to_finish_regist";
}
