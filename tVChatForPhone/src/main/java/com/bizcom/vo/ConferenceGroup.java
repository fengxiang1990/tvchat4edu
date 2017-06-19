package com.bizcom.vo;

import java.util.Date;

import com.config.V2GlobalConstants;

public class ConferenceGroup extends Group {
	public static final int EXTRA_FLAG_INVITATION = 0X0001;
	public static final int EXTRA_FLAG_SYNC = 0X0002;
	
	private User mChairMan;
	private boolean isSyn;
	private boolean isVoiceActivation;
	private boolean isCanInvitation = true;

	public ConferenceGroup(long mGId, String mName, User mOwner,
			Date createDate, User chairMan) {
		super(mGId, V2GlobalConstants.GROUP_TYPE_CONFERENCE, mName, mOwner, createDate);
		this.setmChairMan(chairMan);
	}
	
	public void setVoiceActivation(boolean isVoiceActivation) {
		this.isVoiceActivation = isVoiceActivation;
	}

	public boolean isVoiceActivation() {
		return isVoiceActivation;
	}

	public boolean isSyn() {
		return isSyn;
	}

	public void setSyn(boolean isSyn) {
		this.isSyn = isSyn;
	}

	public boolean isCanInvitation() {
		return isCanInvitation;
	}

	public void setCanInvitation(boolean isCanInvitation) {
		this.isCanInvitation = isCanInvitation;
	}

	public User getmChairMan() {
		return mChairMan;
	}

	public void setmChairMan(User mChairMan) {
		this.mChairMan = mChairMan;
	}
	
	@Override
	public int compareTo(Group g) {
		if (g.getCreateDate() == null) {
			return 1;
		}
		if (this.getCreateDate() == null) {
			return -1;
		}
		if (this.getCreateDate().before(g.getCreateDate())) {
			return -1;
		}

		return 1;
	}

	/**
	 * 
	 * @param g
	 * @param xml
	 * @return
	 */
	public static int extraAttrFromXml(ConferenceGroup g, String xml) {
		int flag = 0;
		String val = extraValue(xml, "inviteuser='");
		if (val != null) {
			if (val.equals("1")) {
				g.setCanInvitation(true);
			} else {
				g.setCanInvitation(false);
			}
			flag |= EXTRA_FLAG_INVITATION;
		}

		String syncVal = extraValue(xml, "syncdesktop='");
		if (syncVal != null) {
			if (syncVal.equals("1")) {
				g.setSyn(true);
			} else {
				g.setSyn(false);
			}
			flag |= EXTRA_FLAG_SYNC;
		}
		return flag;
	}

	private static String extraValue(String xml, String str) {
		int start = xml.indexOf(str);
		if (start != -1) {
			int end = xml.indexOf("'", start + str.length());
			if (end != -1) {
				return xml.substring(start + str.length(), end);
			}
		}
		return null;
	}

	@Override
	public String toXml() {
		return null;
	}
}
