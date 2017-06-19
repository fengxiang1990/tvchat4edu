package com.bizcom.vc.hg.bean;

import android.widget.ImageView;
import android.widget.TextView;

public class DeviceLayoutBean {
	private ImageView ivPortrait;
	private TextView tvPortraitChat;
	private TextView tvNick;
	private TextView tvNum;

	public ImageView getIvPortrait() {
		return ivPortrait;
	}

	public void setIvPortrait(ImageView ivPortrait) {
		this.ivPortrait = ivPortrait;
	}

	public TextView getTvNick() {
		return tvNick;
	}

	public void setTvNick(TextView tvNick) {
		this.tvNick = tvNick;
	}

	public TextView getTvNum() {
		return tvNum;
	}

	public void setTvNum(TextView tvNum) {
		this.tvNum = tvNum;
	}

	public TextView getTvPortraitChat() {
		return tvPortraitChat;
	}

	public void setTvPortraitChat(TextView tvPortraitChat) {
		this.tvPortraitChat = tvPortraitChat;
	}
}
