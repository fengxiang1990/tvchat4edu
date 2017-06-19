package com.bizcom.vo;

public class CompareObject implements Comparable<CompareObject> {

	String mName;
	public Object obj;

	public CompareObject(String mName, Object obj) {
		super();
		this.mName = mName;
		this.obj = obj;
	}

	public CompareObject(String mName) {
		super();
		this.mName = mName;
	}

	@Override
	public int compareTo(CompareObject arg0) {
		if(arg0 == null){
			return 1;
		}
		return mName.compareTo(arg0.mName);
	}
}
