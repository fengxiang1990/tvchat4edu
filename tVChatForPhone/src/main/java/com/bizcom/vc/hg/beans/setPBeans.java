package com.bizcom.vc.hg.beans;

import java.io.Serializable;


public class setPBeans implements Serializable{

	private static final long serialVersionUID = 6372408845676362302L;
	private int id;
	private int malv;
	private int zhenlv;
	private int wid;
	private int hei;
	private int f1;
	private int f2;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMalv() {
		return malv;
	}
	public void setMalv(int malv) {
		this.malv = malv;
	}
	public int getZhenlv() {
		return zhenlv;
	}
	public void setZhenlv(int zhenlv) {
		this.zhenlv = zhenlv;
	}
	public int getWid() {
		return wid;
	}
	public void setWid(int wid) {
		this.wid = wid;
	}
	public int getHei() {
		return hei;
	}
	public void setHei(int hei) {
		this.hei = hei;
	}
	public int getF1() {
		return f1;
	}
	public void setF1(int f1) {
		this.f1 = f1;
	}
	public int getF2() {
		return f2;
	}
	public void setF2(int f2) {
		this.f2 = f2;
	}
	public setPBeans(int malv, int zhenlv, int wid, int hei, int f1, int f2) {
		super();
		this.malv = malv;
		this.zhenlv = zhenlv;
		this.wid = wid;
		this.hei = hei;
		this.f1 = f1;
		this.f2 = f2;
	}
	
	


}
