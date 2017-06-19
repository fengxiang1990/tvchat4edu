package com.bizcom.vo;

import java.util.Date;

import com.bizcom.util.DateUtil;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import android.text.TextUtils;

public class CrowdConversation extends Conversation {

	private Group crowd;
	private User lastSendUser;
	private boolean showContact;

	public CrowdConversation(Group crowd) {
		super();
		if (crowd == null) {
			throw new NullPointerException(
					" Build CrowdConversation failed! Given group is null");
		}
		if (crowd.getGroupType() != V2GlobalConstants.GROUP_TYPE_CROWD) {
			throw new IllegalArgumentException(
					" Build CrowdConversation failed! group type is not GroupType.CHATING");
		}
		this.crowd = crowd;
		mConversationID = crowd.getGroupID();
		mConversationType = TYPE_GROUP;
	}

	@Override
	public String getName() {
		if (crowd != null) {
			return crowd.getName();
		}
		return super.getName();
	}

	@Override
	public CharSequence getMsg() {
		if (showContact)
			return msg;
		else {
			if (crowd != null) {
				User u = crowd.getOwnerUser();
				if (u != null) {
					if (TextUtils.isEmpty(u.getDisplayName())) {
						u = GlobalHolder.getInstance().getUser(u.getmUserId());
						crowd.setOwnerUser(u);
					}
					return u.getDisplayName();
				}
			}
			return msg;
		}
	}

	@Override
	public Date getDate() {
		if (crowd != null) {
			return crowd.getCreateDate();
		}
		return null;
	}

	@Override
	public String getDateString() {
		if (showContact) {
			return super.getDateString();
		} else {
			if (crowd != null) {
				return crowd.getStrCreateDate();
			}
			return super.getDateString();
		}
	}

	@Override
	public String[] getDateCovString() {
		if (showContact) {
			return super.getDateCovString();
		} else {
			if (crowd != null) {
				return crowd.getStrCovFormatDate();
			}
		}
		return super.getDateCovString();
	}
	
	@Override
	public long getDateLong() {
		if (showContact) {
			return super.getDateLong();
		} else {
			if (crowd != null && crowd.getCreateDate() != null) {
				return crowd.getCreateDate().getTime();
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

	public boolean isShowContact() {
		return showContact;
	}

	public void setShowContact(boolean showContact) {
		this.showContact = showContact;
	}

	public Group getGroup() {
		return crowd;
	}

	public void setGroup(Group group) {
		this.crowd = group;
	}

    public void setConversationType(int conversationType){
        this.mConversationType = conversationType;
    }

}
