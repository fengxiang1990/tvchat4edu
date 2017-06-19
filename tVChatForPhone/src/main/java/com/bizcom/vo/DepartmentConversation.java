package com.bizcom.vo;

import java.util.Date;

import com.config.V2GlobalConstants;

public class DepartmentConversation extends Conversation {

	private Group departmentGroup;
	private User lastSendUser;
	private boolean showContact;

	public DepartmentConversation(Group departmentGroup) {
		super();
		if (departmentGroup == null)
			throw new NullPointerException(
					" Build CrowdConversation failed! Given department group is null");

		if (departmentGroup.getGroupType() != V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
			throw new IllegalArgumentException(
					" Build CrowdConversation failed! group type is not GroupType.ORG");
		}
		this.departmentGroup = departmentGroup;
		super.mConversationID = departmentGroup.getGroupID();
		super.mConversationType = V2GlobalConstants.GROUP_TYPE_DEPARTMENT;
	}

	@Override
	public String getName() {
		if (departmentGroup != null) {
			return departmentGroup.getName();
		}
		return super.getName();
	}

	@Override
	public CharSequence getMsg() {
		if (showContact)
			return msg;
		else {
			if (departmentGroup != null) {
				User u = departmentGroup.getOwnerUser();
				return u == null ? "" : u.getDisplayName();
			}
			return msg;
		}
	}

	@Override
	public Date getDate() {
		if (departmentGroup != null) {
			return departmentGroup.getCreateDate();
		}
		return null;
	}

	@Override
	public String getDateString() {
		if (showContact) {
			return super.getDateString();
		} else {
			if (departmentGroup != null) {
				return departmentGroup.getStrCreateDate();
			}
			return null;
		}
	}
	
	@Override
	public String[] getDateCovString() {
		if (showContact) {
			return super.getDateCovString();
		} else {
			if (departmentGroup != null) {
				return departmentGroup.getStrCovFormatDate();
			}
		}
		return super.getDateCovString();
	}

	@Override
	public long getDateLong() {
		if (showContact) {
			return super.getDateLong();
		} else {
			if (departmentGroup != null && departmentGroup.getCreateDate() != null) {
				return departmentGroup.getCreateDate().getTime();
			}
			return 0;
		}
	}

	public User getLastSendUser() {
		return lastSendUser;
	}

	public void setLastSendUser(User lastSendUser) {
		this.lastSendUser = lastSendUser;
	}

	public Group getGroup() {
		return departmentGroup;
	}

	public void setGroup(Group departmentGroup) {
		this.departmentGroup = departmentGroup;
	}

	public boolean isShowContact() {
		return showContact;
	}

	public void setShowContact(boolean showContact) {
		this.showContact = showContact;
	}
}
