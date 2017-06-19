package com.bizcom.vc.hg.beans;

import java.io.Serializable;

public class VideoItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -653201803171676508L;
	
	private String imgeUrl;
	private String text1;
	private String text2;
	private String text3;
	public String getImgeUrl() {
		return imgeUrl;
	}
	public void setImgeUrl(String imgeUrl) {
		this.imgeUrl = imgeUrl;
	}
	public String getText1() {
		return text1;
	}
	public void setText1(String text1) {
		this.text1 = text1;
	}
	public String getText2() {
		return text2;
	}
	public void setText2(String text2) {
		this.text2 = text2;
	}
	public String getText3() {
		return text3;
	}
	public void setText3(String text3) {
		this.text3 = text3;
	}
	public VideoItem(String imgeUrl, String text1, String text2, String text3) {
		super();
		this.imgeUrl = imgeUrl;
		this.text1 = text1;
		this.text2 = text2;
		this.text3 = text3;
	}
	

}
