package com.bizcom.vo;

import com.config.V2GlobalConstants;

public class OrgGroup extends Group {

	public OrgGroup(long mGId, String mName) {
		super(mGId, V2GlobalConstants.GROUP_TYPE_DEPARTMENT, mName, null, null);
	}

	@Override
	public String toXml() {
		return "";
	}
}
