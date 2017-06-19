package com.bizcom.vo;

import java.util.Date;

import com.config.V2GlobalConstants;

public class DiscussionConversation extends Conversation {

	private Group discussionGroup;
	private boolean showContact;

	public DiscussionConversation(Group discussion) {
		super();
		if (discussion == null)
			throw new NullPointerException(
					" Build CrowdConversation failed! Given Discussion Group is null ... please check!");

		if (discussion.getGroupType() != V2GlobalConstants.GROUP_TYPE_DISCUSSION) {
			throw new IllegalArgumentException(
					" Build CrowdConversation failed! Given The type of Group Object is not GroupType.DISCUSSION ... please check!");
		}

		this.discussionGroup = discussion;
		super.mConversationID = discussionGroup.getGroupID();
		super.mConversationType = V2GlobalConstants.GROUP_TYPE_DISCUSSION;
	}

	@Override
	public String getName() {
		if (discussionGroup != null) {
			return discussionGroup.getName();
		}
		return null;
	}

	@Override
	public Date getDate() {
		if (discussionGroup != null) {
			return discussionGroup.getCreateDate();
		}
		return null;
	}

	@Override
	public String getDateString() {
		if (showContact) {
			return super.getDateString();
		} else {
			if (discussionGroup != null) {
				return discussionGroup.getStrCreateDate();
			}
			return null;
		}
	}
	
	@Override
	public String[] getDateCovString() {
		if (showContact) {
			return super.getDateCovString();
		} else {
			if (discussionGroup != null) {
				return discussionGroup.getStrCovFormatDate();
			}
		}
		return super.getDateCovString();
	}

	@Override
	public long getDateLong() {
		if (showContact) {
			return super.getDateLong();
		} else {
			if (discussionGroup != null && discussionGroup.getCreateDate() != null) {
				return discussionGroup.getCreateDate().getTime();
			}
			return 0;
		}
	}

	public Group getGroup() {
		return discussionGroup;
	}

	public void setGroup(Group discussionGroup) {
		this.discussionGroup = discussionGroup;
	}

	public boolean isShowContact() {
		return showContact;
	}

	public void setShowContact(boolean showContact) {
		this.showContact = showContact;
	}

}
