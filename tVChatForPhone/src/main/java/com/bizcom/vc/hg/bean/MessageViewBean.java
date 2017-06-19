package com.bizcom.vc.hg.bean;

import android.widget.ImageView;
import android.widget.TextView;

public class MessageViewBean {
	private ImageView ivPhoto;
	private TextView tvPhotoChat;
	private TextView tvName;
	private TextView tvDate;
	private TextView tvDescribe;

	public ImageView getIvPhoto() {
		return ivPhoto;
	}

	public void setIvPhoto(ImageView ivPhoto) {
		this.ivPhoto = ivPhoto;
	}

	public TextView getTvPhotoChat() {
		return tvPhotoChat;
	}

	public void setTvPhotoChat(TextView tvPhotoChat) {
		this.tvPhotoChat = tvPhotoChat;
	}

	public TextView getTvName() {
		return tvName;
	}

	public void setTvName(TextView tvName) {
		this.tvName = tvName;
	}

	public TextView getTvDate() {
		return tvDate;
	}

	public void setTvDate(TextView tvDate) {
		this.tvDate = tvDate;
	}

	public TextView getTvDescribe() {
		return tvDescribe;
	}

	public void setTvDescribe(TextView tvDescribe) {
		this.tvDescribe = tvDescribe;
	}

}
