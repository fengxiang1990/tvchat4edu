package com.bizcom.vo;

import com.config.V2GlobalConstants;

public class FriendGroup extends Group {
	public FriendGroup(long mGId, String mName) {
		super(mGId, V2GlobalConstants.GROUP_TYPE_CONTACT, mName, null, null);
	}

	@Override
	public String toXml() {
		return null;
	}

}
