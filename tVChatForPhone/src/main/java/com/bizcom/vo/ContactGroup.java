package com.bizcom.vo;

import com.config.V2GlobalConstants;

public class ContactGroup extends Group {
	
	private boolean mIsDefault;
	
	public ContactGroup(long mGId, String mName) {
		super(mGId, V2GlobalConstants.GROUP_TYPE_CONTACT, mName, null, null);
	}

	
	public String toXml() {
		return "<friendgroup "+(this.mGId == 0? "" : "id=\""+mGId+"\"")+"  name=\""+this.mName+"\" />";
	}


	public boolean isDefault() {
		return mIsDefault;
	}


	public void setDefault(boolean mIsDefault) {
		this.mIsDefault = mIsDefault;
	}

	
}
