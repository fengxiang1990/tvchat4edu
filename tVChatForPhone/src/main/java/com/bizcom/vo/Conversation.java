package com.bizcom.vo;

import java.util.Date;

import com.bizcom.util.DateUtil;
import com.config.V2GlobalConstants;

public abstract class Conversation implements Comparable<Conversation> {

	public static final int TYPE_CONFERNECE = V2GlobalConstants.GROUP_TYPE_CONFERENCE;
	public static final int TYPE_CONTACT = V2GlobalConstants.GROUP_TYPE_USER;
	public static final int TYPE_GROUP = V2GlobalConstants.GROUP_TYPE_CROWD;
	public static final int TYPE_DEPARTMENT = V2GlobalConstants.GROUP_TYPE_DEPARTMENT;
	public static final int TYPE_DISCUSSION = V2GlobalConstants.GROUP_TYPE_DISCUSSION;
	public static final int TYPE_VOICE_MESSAGE = 7;
	public static final int TYPE_VERIFICATION_MESSAGE = 8;
	public static final int TYPE_CROWD_VERIFICATION_MESSAGE = 9;
    public static final int TYPE_SEARCH_TAB = 10;
    public static final int TYPE_SEARCH_NORMAL = 11;
    public static final int TYPE_SEARCH_MORE = 12;

	public static final int SPECIFIC_VERIFICATION_ID = -2;
	public static final int SPECIFIC_VOICE_ID = -1;

	protected int mConversationType;

    //此标记用于区别搜索聊天消息内容时构造的Conversation类型
    protected int mSearchLocalMsgCovType = -1;
	protected long mConversationID;
	protected int conversationReadState = V2GlobalConstants.READ_STATE_READ;

	protected Date date;
	protected String name;
	protected CharSequence msg;

	protected boolean isFirst; // this field created for specific item voiceItem
								// or verificationItem
	protected boolean ishouldAdd;
	protected boolean isAddedItem; // this field created for specific item
									// voiceItem or verificationItem

	public Conversation() {
	}

	public Conversation(int mConversationType, long mConversationID) {
		this(mConversationType, mConversationID, V2GlobalConstants.READ_STATE_READ);
	}

	public Conversation(int mConversationType, long mConversationID, int conversationReadState) {
		super();
		this.mConversationType = mConversationType;
		this.mConversationID = mConversationID;
		this.conversationReadState = conversationReadState;
	}

	public Date getDate() {
		return date;
	}

	public String getDateString() {
		if (date != null)
			return DateUtil.getStringDate(date.getTime());
		else
			return null;
	}

	public String[] getDateCovString() {
		if (date != null) {
			return DateUtil.getDateForTabFragmentConversation(date.getTime());
		} else
			return null;
	}

	public long getDateLong() {
		if (date != null)
			return date.getTime();
		else
			return 0;
	}

	public String getName() {
		return name;
	}

	public CharSequence getMsg() {
		return msg;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMsg(CharSequence msg) {
		this.msg = msg;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getReadFlag() {
		return conversationReadState;
	}

	public void setReadFlag(int readFlag) {
		this.conversationReadState = readFlag;
	}

    public void setConversationType(int conversationType){
        this.mConversationType = conversationType;
    }

    public int getSearchLocalMsgCovType() {
        return mSearchLocalMsgCovType;
    }

    public void setSearchLocalMsgCovType(int mSearchLocalMsgCovType) {
        this.mSearchLocalMsgCovType = mSearchLocalMsgCovType;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mConversationID ^ (mConversationID >>> 32));
		// result = prime * result + ((mType == null) ? 0 : mType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Conversation other = (Conversation) obj;
		if (mConversationID != other.mConversationID)
			return false;
		// if (mType == null) {
		// if (other.mType != null)
		// return false;
		// }
		// else if (!mType.equals(other.mType))
		// return false;
		return true;
	}

	public int getType() {
		return mConversationType;
	}

	public void setType(int type) {
		this.mConversationType = type;
	}

	public long getExtId() {
		return mConversationID;
	}

	public void setExtId(long extId) {
		this.mConversationID = extId;
	}

	public boolean isFirst() {
		return isFirst;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public boolean isAddedItem() {
		return isAddedItem;
	}

	public void setAddedItem(boolean isAddedItem) {
		this.isAddedItem = isAddedItem;
	}

	public boolean isIshouldAdd() {
		return ishouldAdd;
	}

	public void setIshouldAdd(boolean ishouldAdd) {
		this.ishouldAdd = ishouldAdd;
	}

	@Override
	public int compareTo(Conversation another) {
		Date date = this.getDate();
		boolean localDate = date == null;
		boolean remoteDate = another.getDate() == null;
		if (localDate && remoteDate)
			return 0;
		else if (!localDate && remoteDate)
			return -1;
		else if (localDate && !remoteDate)
			return 1;

		Long localTime = date.getTime();
		Long remoteTime = another.getDate().getTime();
		if (localTime == remoteTime)
			return 0;
		else if (localTime < remoteTime)
			return 1;
		else
			return -1;
	}

}
