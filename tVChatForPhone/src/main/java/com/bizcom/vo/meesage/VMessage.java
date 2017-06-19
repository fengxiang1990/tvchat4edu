package com.bizcom.vo.meesage;

import android.text.TextUtils;

import com.bizcom.util.DateUtil;
import com.bizcom.util.V2Log;
import com.bizcom.vc.activity.conversation.MessageBodyView;
import com.bizcom.vo.User;
import com.config.GlobalHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VMessage {

	protected long id;
	protected User mFromUser;
	protected User mToUser;
	protected Date mDate;

	protected String mUUID;
	protected long mGroupId;
	protected boolean isLocal;
	protected int mState;
	protected String mXmlDatas;

    //for search chat content
    protected String mPlainText;

	/**
	 * This flag only use to resend VMessage
	 */
	protected String mOldUUID;

	/**
	 * The flag Decides VMessage Object type
	 * 
	 * @see com.config.V2GlobalConstants --> GroupType
	 */
	protected int mMsgType;

	/**
	 * The flag decide that Whether should to display the Time View
	 * 
	 * @see MessageBodyView --> timeTV variable
	 */
	protected boolean isShowTime;

	/**
	 * This flag indicates that this VMessage Object is from resend or not , so
	 * as to decide whether to display Sending Icon View
	 * 
	 * @see MessageBodyView --> sendingIcon variable
	 */
	protected boolean isResendMessage;

	/**
	 * This flag indicates that this VMessage Object is autio reply or not
	 */
	protected boolean isAutoReply;

	protected int readState;
	public boolean isUpdateDate;
	public boolean isUpdateAvatar;

	protected List<VMessageAbstractItem> itemList;
	protected List<VMessageImageItem> imageItems;
	protected List<VMessageAudioItem> audioItems;
	protected List<VMessageFileItem> fileItems;

	public String currentReplaceImageID = "";

    /**
     * 用于缓存OnSendTextResultCallback返回的结果，然后等到binary回调来一起判断
     */
    public int mRecvState = -1;

	public VMessage(int groupType, long groupId, User fromUser, Date date) {
		this(groupType, groupId, fromUser, null, UUID.randomUUID().toString(), date);
	}

	public VMessage(int groupType, long groupId, User fromUser, User toUser, Date date) {
		this(groupType, groupId, fromUser, toUser, UUID.randomUUID().toString(), date);
	}

	public VMessage(int groupType, long groupId, User fromUser, User toUser, String uuid, Date date) {
		this.mGroupId = groupId;
		this.mFromUser = fromUser;
		this.mToUser = toUser;
		this.mDate = date;
		this.mUUID = uuid;
		this.mMsgType = groupType;
		this.readState = VMessageAbstractItem.STATE_READED;

		itemList = new ArrayList<>();
		imageItems = new ArrayList<>();
		audioItems = new ArrayList<>();
		fileItems = new ArrayList<>();
	}

	public String getmOldUUID() {
		return mOldUUID;
	}

	public void setmOldUUID(String mOldUUID) {
		this.mOldUUID = mOldUUID;
	}

	public long getmDateLong() {
		return mDate.getTime();
	}

	public String getmXmlDatas() {
		return mXmlDatas;
	}

	public void setmXmlDatas(String mXmlDatas) {
		this.mXmlDatas = mXmlDatas;
	}

	public int getMsgCode() {
		return this.mMsgType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getFromUser() {
		return mFromUser;
	}

	public void setFromUser(User fromUser) {
		this.mFromUser = fromUser;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		this.mDate = date;
	}

	public long getGroupId() {
		return this.mGroupId;
	}

	public int getState() {
		return mState;
	}

	public void setState(int state) {
		this.mState = state;
	}

	public void setUUID(String UUID) {
		this.mUUID = UUID;
	}

	public void setReadState(int readState) {
		this.readState = readState;
	}

	public boolean isShowTime() {
		return isShowTime;
	}

	public void setShowTime(boolean isShowTime) {
		this.isShowTime = isShowTime;
	}

	public boolean isAutoReply() {
		return isAutoReply;
	}

	public void setAutoReply(boolean isAutoReply) {
		this.isAutoReply = isAutoReply;
	}

    public String getPlainText() {
        return mPlainText;
    }

    public void setPlainText(String mPlainText) {
        this.mPlainText = mPlainText;
    }

	public boolean isLocal() {
		return (mFromUser != null && GlobalHolder.getInstance().getCurrentUserId() == this.mFromUser.getmUserId());
	}

	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}

	public boolean isResendMessage() {
		return isResendMessage;
	}

	public void setResendMessage(boolean isResendMessage) {
		this.isResendMessage = isResendMessage;
	}

	public void setAudioItems(List<VMessageAudioItem> audioItems) {
		VMessageAudioItem target = audioItems.get(0);
		for (int j = 0; j < this.itemList.size(); j++) {
			VMessageAbstractItem temp = this.itemList.get(j);
			if (temp.getType() == VMessageAbstractItem.ITEM_TYPE_AUDIO) {
				VMessageAudioItem oldAudio = (VMessageAudioItem) temp;
				if (oldAudio.getUuid().equals(target.getUuid())) {
					this.itemList.remove(j);
					this.itemList.add(j, target);
					break;
				}
			}
		}
		this.audioItems.clear();
		this.audioItems.addAll(audioItems);
	}

	public void setImageItems(List<VMessageImageItem> imageItems) {
		for (int i = 0; i < this.imageItems.size(); i++) {
			this.imageItems.get(i).recycleAll();
			VMessageImageItem newImage = imageItems.get(i);
			for (int j = 0; j < this.itemList.size(); j++) {
				VMessageAbstractItem temp = this.itemList.get(j);
				if (temp.getType() == VMessageAbstractItem.ITEM_TYPE_IMAGE) {
					VMessageImageItem oldImage = (VMessageImageItem) temp;
					if (oldImage.getUuid().equals(newImage.getUuid())) {
						this.itemList.remove(j);
						this.itemList.add(j, newImage);
						break;
					}
				}
			}
		}
		this.imageItems.clear();
		this.imageItems.addAll(imageItems);
	}

	public void addItem(VMessageAbstractItem item) {
		if (item.getType() != VMessageAbstractItem.ITEM_TYPE_FACE) {
			for (int i = 0; i < this.itemList.size(); i++) {
				VMessageAbstractItem vMessageAbstractItem = this.itemList.get(i);
				if (vMessageAbstractItem.getType() != VMessageAbstractItem.ITEM_TYPE_FACE) {
					if (vMessageAbstractItem.getType() == item.getType()
							&& vMessageAbstractItem.getUuid().equals(item.getUuid())) {
						return;
					}
				}
			}
		}
		this.itemList.add(item);
		if (item.getType() == VMessageAbstractItem.ITEM_TYPE_IMAGE) {
			imageItems.add((VMessageImageItem) item);
		} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_AUDIO) {
			audioItems.add((VMessageAudioItem) item);
		} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_FILE) {
			fileItems.add((VMessageFileItem) item);
		}
	}

	public List<VMessageImageItem> getImageItems() {
		return imageItems;
	}

	public List<VMessageAudioItem> getAudioItems() {
		return audioItems;
	}

	public List<VMessageFileItem> getFileItems() {
		return fileItems;
	}

	public List<VMessageLinkTextItem> getLinkItems() {
		List<VMessageLinkTextItem> linkItems = new ArrayList<VMessageLinkTextItem>();
		for (VMessageAbstractItem item : itemList) {
			if (item.getType() == VMessageAbstractItem.ITEM_TYPE_LINK_TEXT) {
				linkItems.add((VMessageLinkTextItem) item);
			}
		}
		return linkItems;
	}

	public String getUUID() {
		return mUUID;
	}

	public String getStandFormatDate() {
		if (mDate != null) {
			return DateUtil.getStandardDate(mDate);
		}
		return null;
	}

	public String getStringDate() {
		if (mDate != null) {
			return DateUtil.getDateForChatMsg(mDate.getTime());
		}
		return null;
	}

	public User getToUser() {
		return this.mToUser;
	}

	public List<VMessageAbstractItem> getItems() {
		return this.itemList;
	}

	public String getTextContent() {

		StringBuilder sb = new StringBuilder();
		for (VMessageAbstractItem item : itemList) {
			if (item.getType() == VMessageAbstractItem.ITEM_TYPE_TEXT) {
				if (item.isNewLine() && sb.length() != 0) {
					sb.append("\n");
				}

				VMessageTextItem textItem = (VMessageTextItem) item;
				if (!TextUtils.isEmpty(textItem.getText()))
					sb.append(textItem.getText());
			}
		}
		return sb.toString();
	}

	public void recycleAllImageMessage() {
		for (VMessageAbstractItem item : itemList) {
			if (item.getType() == VMessageAbstractItem.ITEM_TYPE_IMAGE) {
				((VMessageImageItem) item).recycleAll();
			}
		}
	}

	/**
	 * Color user BGR
	 * 
	 * @return
	 */
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
				.append("<TChatData IsAutoReply=\"" + isAutoReply + "\" MessageID=\"" + this.mUUID + "\">\n");

		sb.append("<FontList>\n");
		sb.append("<TChatFont Color=\"0\" Name=\"Tahoma\" Size=\"9\" Style=\"\"/>");
		for (VMessageAbstractItem item : itemList) {
			if (item.getType() == VMessageAbstractItem.ITEM_TYPE_LINK_TEXT) {
				sb.append("<TChatFont Color=\"14127617\" Name=\"Tahoma\" Size=\"9\" Style=\"fsUnderline\"/>");
			}
		}
		sb.append("</FontList>\n").append("<ItemList>\n");

		for (VMessageAbstractItem item : itemList) {
			sb.append(item.toXmlItem());
		}
		sb.append("    </ItemList>");
		sb.append("</TChatData>");
		V2Log.d(sb.toString());
		return sb.toString();
	}
}
