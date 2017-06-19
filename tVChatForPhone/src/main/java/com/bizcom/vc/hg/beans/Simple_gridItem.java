package com.bizcom.vc.hg.beans;

import java.io.Serializable;

public class Simple_gridItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -653201803171676508L;
	
	private int rid;
	private String text;
	public Simple_gridItem(int rid, String text) {
		super();
		this.rid = rid;
		this.text = text;
	}
	public int getRid() {
		return rid;
	}
	public void setRid(int rid) {
		this.rid = rid;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	

}
