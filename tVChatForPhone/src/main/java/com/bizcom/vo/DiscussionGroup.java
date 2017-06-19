package com.bizcom.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bizcom.request.util.EscapedcharactersProcessing;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import android.text.TextUtils;

public class DiscussionGroup extends Group {

	private boolean isCreatorExist;

	public DiscussionGroup(long gId, String name, User owner, Date createDate) {
		super(gId, V2GlobalConstants.GROUP_TYPE_DISCUSSION, name, owner, createDate);
		isCreatorExist = true;
	}

	public DiscussionGroup(long gId, String name, User owner) {
		super(gId, V2GlobalConstants.GROUP_TYPE_DISCUSSION, name, owner);
		isCreatorExist = true;
	}

	@Override
	public String getName() {
		if (!TextUtils.isEmpty(mName)) {
			return mName;
		}

		boolean isAddOwner = true;
		List<User> users = getUsers();
		if(users == null){
			return null;
		}
		
		boolean isAdded = false;
		StringBuilder sb = new StringBuilder();
		List<CompareObject> compareUsers = new ArrayList<>();
		if (mOwnerUser != null && isCreatorExist) {
			if (TextUtils.isEmpty(mOwnerUser.getDisplayName())){
				mOwnerUser = GlobalHolder.getInstance().getUser(mOwnerUser.getmUserId());
			}
			sb.append(mOwnerUser.getDisplayName());
			isAdded = true;
			for (int i = 0; i < users.size(); i++) {
				if (users.get(i).getmUserId() == this.mOwnerUser.getmUserId()){
					continue;
				}
				compareUsers.add(new CompareObject(users.get(i).getDisplayName()));
			}
		} else {
			for (int i = 0; i < users.size(); i++) {
				if (users.get(i).getmUserId() == GlobalHolder.getInstance().getCurrentUserId())
					isAddOwner = false;
				
				compareUsers.add(new CompareObject(users.get(i).getDisplayName()));
			}
			
			if (isAddOwner) {
				User currentUser = GlobalHolder.getInstance().getCurrentUser();
				this.addUserToGroup(GlobalHolder.getInstance().getCurrentUser());
				compareUsers.add(new CompareObject(currentUser.getDisplayName()));
			}
		}
		
		Collections.sort(compareUsers);
		for (int i = 0; i < compareUsers.size(); i++) {
			if(isAdded){
				sb.append(" ").append(compareUsers.get(i).mName);
			} else {
				if (i == 0) {
					sb.append(compareUsers.get(i).mName);
				} else {
					sb.append(" ").append(compareUsers.get(i).mName);
				}
			}
			if (sb.toString().length() >= 30)
				break;
		}
		compareUsers.clear();
		return sb.toString();

	}

	@Override
	public String toXml() {
		StringBuilder xml = new StringBuilder();
		xml.append("<discussion " + (this.mGId > 0 ? (" id='" + this.mGId + "' ") : "") + " creatoruserid='"
				+ this.getOwnerUser().getmUserId() + "' name='" + EscapedcharactersProcessing.convert(this.mName)
				+ "'/>");
		return xml.toString();
	}

	public boolean isCreatorExist() {
		return isCreatorExist;
	}

	public void setCreatorExist(boolean isCreatorExist) {
		this.isCreatorExist = isCreatorExist;
	}
}
