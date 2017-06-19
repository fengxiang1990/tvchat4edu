package com.bizcom.vo;

import com.config.GlobalHolder;

import android.graphics.Bitmap;

public class ContactConversation extends Conversation {

	public ContactConversation(long userID) {
		super();
		this.mConversationID = userID;
		this.mConversationType = TYPE_CONTACT;
	}

	@Override
	public String getName() {
		return GlobalHolder.getInstance().getUser(mConversationID).getDisplayName();
	}

	@Override
	public CharSequence getMsg() {
		if (msg != null) {
			return msg;
		}
		return super.getMsg();
	}

	public void setMsg(CharSequence msg) {
		this.msg = msg;
	}

	public Bitmap getAvatar() {
		User user = GlobalHolder.getInstance().getUser(mConversationID);
		return user.getAvatarBitmap();
	}

	public long getUserID() {
		return mConversationID;
	}
	
	public User getUser(){
		return GlobalHolder.getInstance().getUser(mConversationID);
	}
}
