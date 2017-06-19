package com.bizcom.vo;

import java.util.Date;

import com.bizcom.util.DateUtil;
import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

public class ConferenceConversation extends Conversation {

	private Group conference;
	// 该会话是否是会议邀请的类型
	private boolean isInviteType;
	private User mInviteUser;
	private Date mInviteDate;

	public ConferenceConversation(Group conference, boolean isInviteType) {
		super();
		if (conference == null) {
			throw new NullPointerException(" Build ConferenceConversation failed! Given conference is null");
		}
		this.conference = conference;
		this.isInviteType = isInviteType;
		mConversationID = conference.getGroupID();
		mConversationType = TYPE_CONFERNECE;
	}

	@Override
	public String getName() {
		if (conference != null) {
			return conference.getName();
		}
		return super.getName();
	}

	@Override
	public CharSequence getMsg() {
		if (conference != null) {
			User creator = conference.getOwnerUser();
			if (isInviteType) {
				return mInviteUser == null ? ""
						: mInviteUser.getDisplayName()
								+ GlobalConfig.APPLICATION_CONTEXT.getString(R.string.conversation_attend_the_meeting)
								+ " < " + conference.getName() + " >";
			} else {
				return creator == null ? "" : creator.getDisplayName();
			}
		}
		return super.getMsg();
	}

	@Override
	public Date getDate() {
		if (isInviteType) {
			return mInviteDate;
		} else {
			if (conference != null) {
				return conference.getCreateDate();
			}
		}
		return null;
	}

	@Override
	public String getDateString() {
		if (isInviteType) {
			return DateUtil.getStringDate(mInviteDate.getTime());
		} else {
			if (conference != null) {
				return conference.getStrCreateDate();
			}
		}
		return super.getDateString();
	}
	
	@Override
	public String[] getDateCovString() {
		if (isInviteType) {
			return DateUtil.getDateForTabFragmentConversation(mInviteDate.getTime());
		} else {
			if (conference != null) {
				return conference.getStrCovFormatDate();
			}
		}
		return super.getDateCovString();
	}

	@Override
	public long getDateLong() {
		if (isInviteType) {
			return mInviteDate.getTime();
		} else {
			if (conference != null && conference.getCreateDate() != null) {
				return conference.getCreateDate().getTime();
			}
		}
		return 0;
	}

	public Group getGroup() {
		return conference;
	}

	public void setGroup(Group conference) {
		this.conference = conference;
	}

	public User getInviteUser() {
		return mInviteUser;
	}

	public void setInviteUser(User mInviteUser) {
		this.mInviteUser = mInviteUser;
	}

	public Date getInviteDate() {
		return mInviteDate;
	}

	public void setInviteDate(Date mInviteDate) {
		this.mInviteDate = mInviteDate;
	}
}
